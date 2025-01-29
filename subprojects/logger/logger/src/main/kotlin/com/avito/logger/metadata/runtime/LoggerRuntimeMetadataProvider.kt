package com.avito.logger.metadata.runtime

/**
 * Be cautious, [provide] could be called concurrently from many [Thread]s
 *
 * Implement decent version that will work correctly
 */
public interface LoggerRuntimeMetadataProvider {
    public fun provide(): LoggerRuntimeMetadata
}

public object NoOpLoggerRuntimeMetadataProvider : LoggerRuntimeMetadataProvider {
    public object EmptyMetadata : LoggerRuntimeMetadata {
        override fun asMap(): Map<String, String> = emptyMap()
    }

    override fun provide(): LoggerRuntimeMetadata = EmptyMetadata
}
