package com.avito.instrumentation.suite.dex.check

import com.avito.instrumentation.suite.dex.AnnotationData

class AllChecks(
    override val onViolation: (String) -> Unit = { message ->
        throw IllegalStateException(message)
    }
) : TestSignatureCheck {

    private val testSignatureChecks: List<TestSignatureCheck> = listOf(
        ClassAndMethodDuplicateAnnotationCheck(onViolation),
        ExternalIdDuplicateCheck(onViolation),
        DataSetDuplicateCheck(onViolation)
    )

    override fun onNewMethodFound(
        className: String,
        methodName: String,
        classAnnotations: List<AnnotationData>,
        methodAnnotations: List<AnnotationData>
    ) {
        testSignatureChecks.forEach {
            it.onNewMethodFound(
                className,
                methodName,
                classAnnotations,
                methodAnnotations
            )
        }
    }
}
