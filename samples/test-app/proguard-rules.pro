-dontobfuscate

# Workaround for shrinked R class in test code: https://github.com/slackhq/keeper/issues/22
# Consuming the same rules from ui-testing-core is not enough. We need them in a regular APK not a test one.
-keep class com.google.android.material.R$id { *; }
