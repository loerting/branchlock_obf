# Changelog

## 4.1.7

- Demo version now supports up to 1500 included classes and all tasks.
- Server backend updates and maintenance.

## 4.1.6

- Branchlock now supports Java up to version 24.
- New member name generation mode: RTL & LTR
- General maintenance and fixes.

## 4.1.5

- Branchlock now supports Java up to version 23.
- Updated documentation.
- Hardened number encryption.

## 4.1.4

- Fixed root checker throwing exception in some cases.
- Improved root checker and debug checker task.
- Added some default exclusions for Android.

## 4.1.3

- Fixed Renamer renaming excluded methods in some cases.
- Renamer now doesn't rename volatile fields by default.
- Fixed IncompatibleClassChangeError ("overrides final method") caused by Renamer. 
- Fixed Debug information remover not handling SourceDebugExtension annotation correctly.
- Classes with native methods will now keep their name by default in Renamer.
- Improved duplicate class handling.

## 4.1.2

- Debug information remover now removes information within Kotlin intrinsics.
- Fixed root checker task throwing an exception.

## 4.1.1

- Missing references from library classes are not printed anymore.
- The amount of references to a missing reference class is now counted.
- Better format for printing missing references.
- Fixed a "Malformed class name" error on certain JVMs caused by Renamer.

## 4.1.0

- New member name generation mode: Ideographs (豈 更 車).
- Fixed multiple differently caused IllegalAccessError exceptions in Renamer.
- Debug information remover now handles @SourceDebugExtension annotations of kotlin classes.
- Various optimizations.

## 4.0.10

- Fixed an IllegalArgumentException when using Method Merger.
- Ensured backward compatibility up to Java 11 for the Gradle plugin.
- build.gradle.kts compatibility for the Gradle plugin.

## 4.0.9

- Fixed Crash RE-Tools breaking some applications.
- Fixed a bug in the Gradle plugin where compileClasspath was not used for libraries.
- Better output copying mechanism for the Gradle plugin.
- Improved some log messages.
- Documentation update.

## 4.0.8

- Multiple fixes to ensure Spring-Boot compatibility.
- Retain metadata information of JAR file entries.
- JAR entries that used STORE compression are now exported with STORE compression.
- Fix visibility of classes not updated in various tasks when only referenced by a class constant.
- Fixed debug detection not working when there are no entry points.

## 4.0.7

- Updated Branchlock annotations. They are now available as a maven dependency.
- Support classes nested inside folders (e.g. Spring-Boot classes in BOOT-INF/classes).
- Added a setting to read nested jar files inside input files as libraries.
- Faster loading of input files.
- Fixed an equivalence class bug in Trimmer.
- Fixed a StackOverflowError in Trimmer on huge projects.
- Fixed a reflection detection race condition exception.

## 4.0.6

- Fixed generated classes being affected by Ranges.
- Fixed an IllegalAccessException when using Reference encryption and Renamer together.
- Fixed a different IllegalAccessException when using Reference encryption only.
- Print information about classes that failed to load.

## 4.0.5

- More information about generated classes when logged.
- Fixed class export error not being logged.
- Fixed equivalence class incomplete after remapping a single class.
- Lowered maximum container size for method merger to prevent exceeding the JVM limit.
- New Gradle desktop plugin for creating obfuscated jars automatically when running "jar" task. (Experimental)

## 4.0.4

- New Android Gradle plugin which supports AGP 8.0.0+ (Experimental).
- Fixed null byte class names in Renamer causing corrupted resources.
- Fixed a "Bad access to protected data" verify error caused by Renamer.
- Improved reflection detection by handling more cases and being more specific.
- Fix Debug information remover not removing signatures of fields.

## 4.0.3

- Renamer now updates class names in various resource types: XML, YAML, .properties, .MF

## 4.0.2

- Improved reflection detection.
- Fixed Logic Scrambler breaking annotations.
- Fixed Salting breaking annotations.

## 4.0.1

- Improved keyword name generation.
- Added a setting for null byte class names to Renamer.
- Added a setting for package creation to Renamer.
- Branchlock annotations are now removed from the output.

## 4.0.0

### What's new

- Complete backend rewrite using new java features.
- New frontend / webapp.
- Multiple resolution bugs causing various issues have been fixed.
- Various fixes and improvements.
- Flow obfuscation improvements.
- String encryption strength and efficiency improvements.
- Fixed a few resolution bugs in Renamer and Reference encryption.
- Patched a denial of service vulnerability.
- Added support for encrypting dynamic string concatenation in modern java versions.
- Annotations which can be used to control obfuscation.
- A seed can now be specified for deterministic obfuscation.
- Kotlin debug information removal.
- Better Serialization support.
- New task: Method merger which merges static methods into one.

### Good to know for users

- Old stacktrace decryption has been removed.
- Automatic method splitting has been removed due to instability. Users are now responsible for managing big methods.
- The config format generally stays the same, but some settings have been deprecated or slightly changed.
- Reference encryption will not encrypt unresolved references anymore. Make sure to upload all your dependencies.
- Classes that are provided as a library but are also present in the input file will be excluded automatically.




