---
title: Android infrastructure
type: docs
---

# Avito android infrastructure on github

Monorepo of all tooling to continuously test and deliver apps to users.

## Modules

### Gradle plugins

To use plugins in your project apply them in `build.gradle`:

```groovy
buildscript {
    dependencies {
        classpath("com.avito.android:instrumentation-tests:$avitoToolsVersion")   
    }
    repositories {
        jcenter()   
    }
}

apply("com.avito.android.instrumentation-tests")
```

Or use [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block): 

```groovy
plugins {
    id("com.avito.android.instrumentation-tests")
}
```

`settings.gradle`:

```groovy
pluginManagement {
    repositories {
        jcenter()
    }
    resolutionStrategy {
        eachPlugin {
            String pluginId = requested.id.id
            if (pluginId.startsWith("com.avito.android")) {
                def artifact = pluginId.replace("com.avito.android.", "")
                useModule("com.avito.android:$artifact:$avitoToolsVersion")
            }
        }
    }
}
```

Plugins:

- `:artifactory-app-backup` - Gradle plugin to back up build artifacts in [artifactory](https://jfrog.com/artifactory/)
- `:build-metrics` - Gradle plugin for gathering build metrics and deliver them to [grafana](https://grafana.com/)
- `:build-properties` - Gradle plugin to deliver custom build parameters to Android assets
- `:buildchecks` - Gradle plugin to early detection of build problems
- [`:cd`]({{< ref "/docs/ci/CIGradlePlugin.md" >}})
- `:dependencies-lint` - Gradle plugin to detect unused Gradle dependencies
- `:design-screenshots` - Gradle plugin, extended tasks to support screenshot testing on top of our `:instrumentation` plugin
- `:feature-toggles` - Gradle plugin to extract feature toggles values from code and report it as build artifact
- `:impact`, `:impact-shared` - Gradle plugin to search parts of the project we can avoid testing based on diff. 
- `:instrumentation-tests` - Gradle plugin to set up and run instrumentation tests on Android
- `:instrumentation-test-impact-analysis`, `:ui-test-bytecode-analyser` - Gradle plugin to search ui tests we can avoid based on `impact-plugin` analysis
- `:kotlin-root` - Gradle plugin to configure kotlin tasks for internal project
- `:lint-report` - Gradle plugin merging lint reports from different modules
- `:module-types` - Gradle plugin to prevent modules go to wrong configurations (android-test module as an app's implementation dependency for example) 
- `:code-ownership` - Gradle plugin to prevent dependency on other team's private modules
- `:performance` - Gradle plugin, extended tasks to support performance testing on top of our `:instrumentation` plugin
- `:prosector` - Gradle plugin and client for security service
- `:qapps` - Gradle plugin to deliver apps to internal distribution service, see [QApps]({{< ref "/docs/cd/QApps.md" >}})
- `:robolectric`- Gradle plugin to configure [robolectrtic](http://robolectric.org/) for internal project
- `:room-config` - Gradle plugin to configure [room](https://developer.android.com/topic/libraries/architecture/room) for internal project
- `:signer` - Gradle plugin for internal app signer

### Buildscript dependencies

- `:android` - Android Gradle plugin extensions, and Android SDK wrapper // todo separate
- `:bitbucket` - Bitbucket client to deliver checks results right into pull request context
via [code insights](https://www.atlassian.com/blog/bitbucket/bitbucket-server-code-insights) and comments
- `:docker` - docker client to work with docker daemon from Gradle
- `:files` - utils to work with files and directories
- `:git` - git client to work within Gradle
See [impact analysis]({{< ref "/docs/ci/ImpactAnalysis.md" >}})
- `:kotlin-dsl-support` - Gradle api extensions //todo rename
- `:kubernetes` - kubernetes credentials config extension
- `:logging` - custom logger to serialize for Gradle workers //todo no longer a problem, remove
- `:pre-build` - extensions to add tasks to the early stages of build
- `:process` - utils to execute external commands from Gradle
- `:runner:client`, `:runner:service`, `:runner:shared`, `:runner:shared-test` - instrumentation tests runner
- `:sentry-config` - [sentry](https://sentry.io/) client config extension
- `:slack` - [slack](https://slack.com/) client to work within Gradle plugins
- `:statsd-config` - [statsd](https://github.com/statsd/statsd) client config extension
- `:teamcity` - wrapper for [teamcity](https://www.jetbrains.com/ru-ru/teamcity/) [client](https://github.com/JetBrains/teamcity-rest-client)
and [service messages]((https://www.jetbrains.com/help/teamcity/build-script-interaction-with-teamcity.html#BuildScriptInteractionwithTeamCity-ServiceMessages))
- `:test-project` - [Gradle Test Kit](https://docs.gradle.org/current/userguide/test_kit.html) project generator and utilities
- `:test-summary` - test suite summary writer
- `:trace-event` - client for [trace event format](https://docs.google.com/document/d/1CvAClvFfyA5R-PhYUmn5OOQtYMH4h6I0nSsKchNAySU/preview)
- `:upload-cd-build-result` - client for internal "Apps release dashboard" service
- `:upload-to-googleplay` - wrapper for google publishing api
- `:utils` - //todo remove 

### Android testing modules

Code that goes in `androidTestImplementation` configuration and runs on emulators.

- `:junit-utils` - //todo move to common
- `:mockito-utils` - //todo move to common
- `:resource-manager-exceptions` - //todo remove
- `:test-annotations` - annotations to supply meta information for reports and [test management system]({{< ref "/docs/test/TestManagementSystem.md" >}})
- `:test-app` - app we are using to test `:ui-testing-` libraries
- `:test-inhouse-runner` - custom [android junit runner](https://developer.android.com/reference/android/support/test/runner/AndroidJUnitRunner.html)
- `:test-report` - client to gather test runtime information for reporting
- `:ui-testing-core` - main ui testing library, based on [espresso](https://developer.android.com/training/testing/espresso)
- `:ui-testing-maps` - addon for main library to test google maps scenarios
- `:websocket-reporter` - client to gather websocket info for reporting

### Android libraries

- [`:proxy-toast`]({{< ref "/docs/test/Toast.md" >}}) - helps with testing toasts

### Common modules

Shared modules between android-test and Gradle.

- `:file-storage` - client for internal file storage client, used to store screenshots, videos and other binary stuff
- `:okhttp` - okhttp extensions
- `:sentry` - [sentry]((https://sentry.io/)) client
- `:statsd` - [statsd]((https://github.com/statsd/statsd)) client
- `:test-okhttp` - wrapper for [okhttpmockwebserver](https://github.com/square/okhttp/tree/master/mockwebserver)
- `:time` - simple time api 

## Publishing

{{<avito section>}}

### Publishing a new release

All releases are published to [bintray](https://bintray.com/avito-tech/maven/avito-android).

1. Checkout a release branch with a name equals to `projectVersion`. For example, `2020.3.1`.\
This branch must be persistent. It will be used for automation.
1. Make sure integration tests on release branch passed full integration checks [CI integration tests against avito]({{<relref "#ci-integration-tests-against-avito">}})
1. Manually run [Github publish configuration (internal)](http://links.k.avito.ru/releaseAvitoTools) on the release branch.
1. Make a PR to internal avito repository with the new version of infrastructure
1. Checkout a new branch and make a PR to github repository:
    - Use the new version in `infraVersion` property
    - Bump up a `projectVersion` property to the next version
1. Create a new [release](https://help.github.com/en/github/administering-a-repository/managing-releases-in-a-repository) against the release branch.\
You can use a draft release to prepare a description in advance.

### Local integration tests against avito

1. Run `./gradlew publishToMavenLocal -PprojectVersion=local` in github repository.
1. Run integration tests of your choice in avito with specified test version

### CI integration tests against avito

1 Choose a necessary configuration

- [fast check configuration (internal)](http://links.k.avito.ru/fastCheck) - pull request builds
- [full check configuration (internal)](http://links.k.avito.ru/fullCheck) - a full set of checks (without a release chain)

2 Run custom build

If you need to test unmerged code, select a custom build branch.\
You will see branches from both repositories:

![](https://user-images.githubusercontent.com/1104540/75977180-e5dd4d80-5eec-11ea-80d3-2f9abd7efd36.png)
 
- By default, build uses develop from github against develop from avito
- If you pick a branch from avito, it will run against develop on github
- If you pick a branch from github, it will run against develop on avito
- (UNTESTED) To build both projects of special branch, they should have the same name
