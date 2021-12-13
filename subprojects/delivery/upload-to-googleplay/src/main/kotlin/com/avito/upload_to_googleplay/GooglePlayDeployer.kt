package com.avito.upload_to_googleplay

import org.slf4j.Logger
import java.io.File

public data class GooglePlayDeploy(
    val binaryType: BinaryType,
    val track: String,
    val applicationId: String,
    val binary: File,
    val mapping: File
) {
    public enum class BinaryType { APK, BUNDLE }
}

/**
 * Код подсмотрен в https://github.com/Triple-T/gradle-play-publisher
 * https://developers.google.com/android-publisher/api-ref/
 * @see [com.avito.cd.getCdBuildConfig] валидация, которая гарантирует,
 * что с [GooglePlayDeploy.applicationId] ассоциирован один [GooglePlayDeploy]
 */
public interface GooglePlayDeployer {

    public fun deploy(deploys: List<GooglePlayDeploy>)
}

public fun createGooglePlayDeployer(jsonKey: File, logger: Logger): GooglePlayDeployer {
   return GooglePlayDeployerImpl(jsonKey, logger)
}
