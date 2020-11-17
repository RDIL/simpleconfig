# Changelog

## v2.1.0

```diff
+ Add uploadArchives task to Gradle, update Gradle to 6.3 to 6.7
+ Rename internal field cfg to jsonObject
+ Switch from deprecated JsonParser#parse to JsonParser#parseString
+ Add support for equals(Object) and hashCode()
+ Add unregister(Class)
+ Add getConfigObjs()
+ Add getFile()
+ Add @Documented to @Option annotation
```

## v2.0.0

```diff
+ Rename ConfigHandler to ConfigurationSystem
+ Rename Configuration to Option
- Remove Config class, any Object can now be registered
```

## v1.0.1

```diff
+ Change build target from Java 11 to Java 8
```

## v1.0.0

This is the first release of the library, and as such has no changelog.
