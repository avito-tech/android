package com.avito.android.lint

import com.avito.slack.model.SlackChannelId
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class LintReportExtension @Inject constructor(objects: ObjectFactory) {

    // todo some global slack settings?
    val slackToken = objects.property<String>()
    val slackWorkspace = objects.property<String>()
    val slackChannelToReportLintBugs = objects.property<SlackChannelId>()
}
