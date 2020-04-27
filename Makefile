test_build_type?=debug
infra?=
ci?=true
log_level?=-q

localFilter?=
includePrefix?=
includeAnnotation?=

params?=

ifdef localFilter
params +=-PlocalFilter=$(localFilter)
endif

ifdef includePrefix
params +=-PincludePrefix=$(includePrefix)
endif

ifdef includeAnnotation
params +=-PincludeAnnotation=$(includeAnnotation)
endif

ifdef infra
params +=-PinfraVersion=$(infra)
endif

params +=-PtestBuildType=$(test_build_type)
params +=-Pci=$(ci)

params +=$(log_level)

filter_report_id?=

test_app_help:
	./gradlew s:android-test:test-app:help -PinfraVersion=local $(log_level)

publish_to_maven_local:
	./gradlew publishToMavenLocal -PprojectVersion=local $(log_level)

test_app_instrumentation_gradle_debug:
	./gradlew subprojects\:android-test\:test-app\:instrumentationUi -Dorg.gradle.jvmargs='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=y' --no-daemon -PinfraVersion=local -Pci=true -PtestBuildType=$(test_build_type) $(log_level)

test_app_instrumentation:
	./gradlew subprojects\:android-test\:test-app\:instrumentationUi -PinfraVersion=local -Pci=true -PtestBuildType=$(test_build_type) -PlocalFilter=$(local_filter) -PincludeAnnotation=$(includeAnnotation) -PincludePrefix=$(includePrefix) $(log_level)

test_app_instrumentation_local:
	./gradlew subprojects\:android-test\:test-app\:instrumentationLocal $(params)

test_app_instrumentation_android_debug:
	./gradlew :subprojects:android-test:test-app:instrumentationUiDebug -PinfraVersion=local -Pci=true -PkubernetesContext=beta -PtestBuildType=$(test_build_type) -PlocalFilter=$(local_filter) -PincludeAnnotation=$(includeAnnotation) -PincludePrefix=$(includePrefix) $(log_level)

dynamic_properties:
	$(eval keepFailedTestsFromReport?=)
	$(eval skipSucceedTestsFromPreviousRun=true)
	$(eval testFilter?=empty)
	$(eval dynamicPrefixFilter?=)

test_app_instrumentation_dynamic: dynamic_properties
	./gradlew subprojects\:android-test\:test-app\:instrumentationDynamic -PinfraVersion=local -Pci=true -PtestBuildType=$(test_build_type) -PdynamicTarget22=true -Pinstrumentation.dynamic.testFilter=$(testFilter) -Pinstrumentation.dynamic.keepFailedTestsFromReport=$(keepFailedTestsFromReport) -Pinstrumentation.dynamic.skipSucceedTestsFromPreviousRun=$(skipSucceedTestsFromPreviousRun) -PdynamicPrefixFilter=$(skipTestsWithPrefix) $(log_level)

unit_tests:
	./gradlew test $(log_level)

clear_k8s_deployments_by_namespaces:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:clearByNamespaces -PteamcityApiPassword=$(teamcityApiPassword) -Pci=true $(log_level)

clear_k8s_deployments_by_names:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:deleteByNames -Pci=true $(log_level)
