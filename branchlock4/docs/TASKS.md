# Tasks

This document describes all tasks that are available in Branchlock 4.

Performance costs: ```CLOSE_TO_ZERO``` < ```MINIMAL``` < ```NOTICABLE```.

## Stacktrace encryption

- ids: ```stacktrace-encryption, encrypt-stacktrace```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```CLOSE_TO_ZERO```
- full_version_only: true

Encrypts the debug information present in stacktraces using a block cipher.
Stacktraces can be decrypted using a secret key on our website.
This task requires source file and line number attributes.

### Settings

- "key": The key to use for encryption.

## Runtime class wrapper

- ids: ```runtime-wrapping, cover-instantiation```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```MINIMAL```

Encapsulates Java runtime classes within wrapper classes to obfuscate them.

### Settings

- "no_static_cover" (default: false): Do not cover static method calls to runtime classes.
- "no_metafactory_cover" (default: false): Do not cover lambdas.

## Control flow obfuscation

- ids: ```flow```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```NOTICEABLE```
- full_version_only: true

Rewrites the control flow to make it more difficult for humans to understand and reverse engineer.
As a result, decompilers may struggle to find a suitable control flow graph.

### Settings

- "coverage": Percentage (0.0 to 1.0) of how intense the obfuscation should be.

## Logic scrambler

- ids: ```scramble```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```MINIMAL```

Converts characters into bit operations and turns logical operations and lambdas into more complex code.

### Settings

/

## Debug information remover

- ids: ```debug-info-remover, debug-remover```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```CLOSE_TO_ZERO```

Removes debug attributes that are not essential for the JVM to run the application.
Makes decompilation more challenging and reduces the application's size.

### Settings

- "keep_stacktrace_info" (default: false): Keep stacktrace information.
- "remove_annotations" (default: false): Remove all annotations.
- "keep_local_vars" (default: false): Keep local variable information.
- "keep_signatures" (default: false): Keep debug information about generic signatures.
- "remove_kotlin" (default: false): Remove Kotlin metadata.

## Member shuffler

- ids: ```shuffle, shuffle-members```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```CLOSE_TO_ZERO```

Rearranges the members of classes to make the code less intuitive.

### Settings

/

## Unify member visibility

- ids: ```unify-visibility, generalize-access, generalize-visibility```
- compatibility: ```DESKTOP```
- performance_cost: ```CLOSE_TO_ZERO```

Changes the visibility of class members to public.

### Settings

/

## Root detection

- ids: ```anti-root, root-checker```
- compatibility: ```ANDROID```
- performance_cost: ```CLOSE_TO_ZERO```

Checks if the android device is rooted and terminates the application if it is.

### Settings

/

## Debug detection

- ids: ```anti-debug, debug-checker, debug-detection```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```CLOSE_TO_ZERO```

Checks if the application is being debugged and terminates the application if it is.

### Settings

- "check_noverify" (default: false): Check if the application is started with the -noverify flag.

## Number encryption

- ids: ```numbers```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```MINIMAL```

Converts integer and non-integer numbers into complex arithmetic operations.

### Settings

/

## String encryption

- ids: ```strings```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```MINIMAL```

Encrypts string constants.
Please note that the demo version does not encrypt any potentially malicious strings such as URLs and file paths.

### Settings

- "min_length" (default: 0): Minimum length of strings to encrypt.
- "max_length" (default: /): Maximum length of strings to encrypt.

## Salting

- ids: ```salting, generify```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```MINIMAL```
- full_version_only: true

Protects the application from static decryption methods by using dynamic encryption keys which are passed through the application at runtime.
When combined with other encryption tasks, it enhances the overall protection.

### Settings

/

## Reference encryption

- ids: ```references```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```NOTICABLE```
- full_version_only: true

Encrypts method calls and field accesses.
This task makes it extremely challenging to reverse engineer the application.

### Settings

- "only_local" (default: false): Only encrypt references to local (non-runtime) classes.
- "exclude_fields" (default: false): Exclude field accesses from encryption.

## Member renamer

- ids: ```rename```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```CLOSE_TO_ZERO```

Renames members of classes to random names.
This shrinks the application's size and makes the code more difficult to understand.
It is virtually impossible the recover the original names through reverse engineering.

### Settings

- "disable_reflection_detection" (default: false): Disable reflection detection.
- "keep_class_names" (default: false): Keep original class names.
- "keep_field_names" (default: false): Keep original field names.
- "keep_method_names" (default: false): Keep original method names.
- "null_byte_trick" (default: false): Prepend a null byte to class names to trick file archivers.
- "keep_local_var_names" (default: false): Keep original local variable names.
- "create_packages" (default: true): Rearrange classes into new packages.
- "disable_resource_update" (default: false): Keep resource files unchanged.

## Code trimmer

- ids: ```trimmer```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```CLOSE_TO_ZERO```

Removes unused methods and fields from classes by analyzing the application's call graph.

### Settings

- "disable_reflection_detection" (default: false): Disable reflection detection.
- "entry_points_require_annotation" (default: false): Only consider methods annotated with @EntryPoint as entry points.
- "keep_unused_classes" (default: false): Keep unused classes (but still remove their unused members).
- "keep_unused_fields" (default: false): Keep unused fields.
- "error_replacement" (default: "false"): Instead of removing unused members completely, replace them with an error message.

## Crash reverse engineering tools

- ids: ```crasher```
- compatibility: ```DESKTOP```
- performance_cost: ```CLOSE_TO_ZERO```
- full_version_only: true

Crashes reverse engineering tools using various tricks.
This should not be used as a standalone protection.
Do not use it on applications that are dynamically loaded.

### Settings

- "folder_trick" (default: false): Trick file archivers into handling classes as folders.

## Method merger

- ids: ```method-merger```
- compatibility: ```DESKTOP, ANDROID```
- performance_cost: ```MINIMAL```
- full_version_only: true

Merges static methods into one method.

### Settings

/
