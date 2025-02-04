package com.avito.graph

import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.DirectedWeightedMultigraph

/**
 * @operations nodes describing a DAG
 */
public class ShortestPath<T : Operation>(private val operations: Set<T>) {

    private val operationByKey: Map<String, Operation> = operations.associateBy { it.id }
    private val syntheticSource = SimpleOperation("source", duration = 0.toDouble())
    private val syntheticSink = SimpleOperation("sink", duration = 0.toDouble())
    private val uniquePredecessors = operations.flatMap { it.predecessors }.toSet()

    public fun find(): OperationsPath<T> {
        if (operations.size <= 1) return OperationsPath(operations.toList())

        val graph = build()
        val path = graph.shortestPath()
        @Suppress("UNCHECKED_CAST")
        val operations = path.vertexList
            .filterNot { it.isSynthetic() } as List<T>

        return OperationsPath(operations)
    }

    /***
     * Builds a graph to find the shortest path.
     * Let's see by example. We have operations:
     *
     * - `A(duration = 1, predecessors = ())`
     * - `B(duration = 2, predecessors = (A))`
     *
     * To calculate the shortest path these operations will be represented as graph:
     *
     * ```
     * vertex SYNTHETIC SOURCE <--- vertex A <---(edge weight = 1)--- vertex B <---(edge weight = 2)--- vertex SYNTHETIC SINK
     * ```
     *
     * Operation A and B become vertexes in graph.
     * Durations of operations become edges weights.
     *
     * Synthetic vertexes for source and sink are needed mostly due to performance reasons.
     * Graph can contain multiple sources and sinks. It's cheaper to calculate only one path from single source to single sink.
     *
     * Let's see on more complex case:
     *
     * ```
     * operation A(duration = 1) <--------------┐
     *                                          │
     * operation B(duration = 2) <--- operation D(duration = 4) <--- operation E(duration = 1)
     *
     * operation F(duration = 3) <--- operation G(duration = 2)
     * ```
     *
     * Sources: A, B, F
     * Sinks: E, G
     *
     * These operations will be represented as graph:
     *
     * ```
     *        ┌--------- vertex A <--(edge weight = 1)---------┐
     *        ↓                                                │
     * SYNTHETIC SOURCE <---- vertex B <--(edge weight = 2)-- vertex D <--(edge weight = 4)-- vertex E <--(edge weight = 1)-- SYNTHETIC SINK
     *        ↑                                                                                                                │
     *        └--------- vertex F <--(edge weight = 3)------------- vertex G <--(edge weight = 2)------------------------------┘
     * ```
     */
    private fun build(): Graph<Operation, DefaultWeightedEdge> {
        val graph: Graph<Operation, DefaultWeightedEdge> =
            DirectedWeightedMultigraph(DefaultWeightedEdge::class.java)

        addNodes(graph)
        addSource(graph)
        addSink(graph)
        checkGraphStructure(graph)

        return graph
    }

    private fun Graph<Operation, DefaultWeightedEdge>.shortestPath(): GraphPath<Operation, DefaultWeightedEdge> {
        // We could use negative weights to find the shortest path as solution for the longest path in DAG
        // Bellman–Ford algorithm supports negative weights but Dijkstra doesn't.
        // TODO: try topological sorting, it will be more efficient - O(V+E) instead of O(V*E)
        return BellmanFordShortestPath(this)
            .getPaths(syntheticSource)
            .getPath(syntheticSink)
    }

    private fun checkGraphStructure(graph: Graph<Operation, DefaultWeightedEdge>) {
        checkSinkLinks(graph)
        checkSourceLinks(graph)
    }

    private fun checkSinkLinks(graph: Graph<Operation, DefaultWeightedEdge>) {
        graph.incomingEdgesOf(syntheticSink).forEach { edge ->
            val sinkNode = graph.getEdgeSource(edge)

            check(sinkNode.isSink() && !sinkNode.isSynthetic()) {
                "Expected structure: ... <-- node <-- node <-- sink <-- synthetic sink"
            }
        }
    }

    private fun checkSourceLinks(graph: Graph<Operation, DefaultWeightedEdge>) {
        graph.outgoingEdgesOf(syntheticSource).forEach { edge ->
            val sourceNode = graph.getEdgeTarget(edge)

            check(sourceNode.isSource() && !sourceNode.isSynthetic()) {
                "Expected structure: synthetic source <-- source <-- node <-- node <-- ..."
            }
        }
    }

    private fun addNodes(graph: Graph<Operation, DefaultWeightedEdge>) {
        operations.forEach { node ->
            graph.addVertex(node)
        }
        operations.forEach { node ->
            node.findDependencies().forEach { dependency ->
                graph.addWeightedEdge(dependency, node)
            }
        }
    }

    private fun addSource(graph: Graph<Operation, DefaultWeightedEdge>) {
        graph.addVertex(syntheticSource)

        operations
            .filter { it.isSource() }
            .forEach { source ->
                graph.addWeightedEdge(syntheticSource, source)
            }
    }

    private fun addSink(graph: Graph<Operation, DefaultWeightedEdge>) {
        graph.addVertex(syntheticSink)

        operations
            .filter { it.isSink() }
            .forEach { sink ->
                graph.addWeightedEdge(sink, syntheticSink)
            }
    }

    private fun Graph<Operation, DefaultWeightedEdge>.addWeightedEdge(
        from: Operation,
        to: Operation
    ) {
        addEdge(from, to)
        setEdgeWeight(from, to, from.duration)
    }

    private fun Operation.findDependencies(): List<Operation> {
        // Skip missing nodes
        return predecessors.mapNotNull { dependencyKey ->
            operationByKey[dependencyKey]
        }
    }

    private fun Operation.isSink(): Boolean {
        return !uniquePredecessors.contains(this.id)
    }

    private fun Operation.isSource() =
        predecessors.isEmpty()

    /**
     * Synthetically added operation
     */
    private fun Operation.isSynthetic(): Boolean =
        this == syntheticSource || this == syntheticSink
}
