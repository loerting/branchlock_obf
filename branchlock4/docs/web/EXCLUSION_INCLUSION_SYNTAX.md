# Class Exclusion and Inclusion

Class exclusion and inclusion is a feature that allows you to specify which parts of your project should be obfuscated and which should not. 
Generally, it is not recommended to obfuscate the whole project, as it can cause performance issues, problems with reflection, and other issues.

## Classes 

To exclude a specific class from obfuscation, you need to input the package and class name separated by slashes (/) into the exclusion box. For example, if you want to exclude the class "foo.bar.Main", the input should be:

foo/bar/Main

Exclusion can also be done with matching. To exclude a whole package and its subpackages, use the double asterisks (**). For example, to exclude the package "foo.bar" and all its subpackages, the input should be:

foo/bar/**

To exclude a whole package without subpackages, use a single asterisk (*). For example, to exclude classes in the package "foo.bar" but not in "foo.bar.qux", the input should be:

foo/bar/*

## Child Classes

To exclude classes that extend a certain class, prepend a question mark (?) to the front. For example, to exclude all classes extending "foo.bar.Base", the input should be:

?foo/bar/Base

This can also be combined with package exclusion. For example, to exclude all classes extending classes in the "foo.bar" package, the input should be:

?foo/bar/*

## Methods

To exclude a method in a class, use the class matching string followed by a hash (#) and the method name. For example, to exclude the method "main" in "foo.bar.GUI", the input should be:

foo/bar/GUI#main

Method names can also be matched. To exclude all methods starting with "init" in "foo.bar.GUI", the input should be:

foo/bar/GUI#init*

This can be extended further. For example, to exclude all "run" methods of classes extending "foo.bar.Base", the input should be:

?foo/bar/Base#run

To exclude constructors, use "const", and for static initializers, use "static".

## Annotated Members

To exclude all members annotated with a certain annotation, prepend an at sign (@) to the end. For example, to exclude all classes in the package "foo.bar" annotated with "@Deprecated", the input should be:

foo/bar/**@Deprecated

Matching annotations and child annotations are not supported.

## Fields

To exclude a field in a class, use the class matching string followed by a hash (#) and the field name. For example, to exclude the field "name" in "foo.bar.Person", the input should be:

foo/bar/Person#name

Field names can also be matched. To exclude all fields starting with "name" in "foo.bar.Person", the input should be:

foo/bar/Person#*Date

## (Re-)Inclusion

Classes that have been excluded can be re-included by putting the matching string in the Re-include box. For example, if you only want to obfuscate the class "foo.bar.Main" in your project, the input should be:

Exclude: **
Include: foo/bar/Main

Method matching strings or class hierarchy matching can also be used with inclusion.

## Recommended Usage

For most projects, it is recommended to exclude all classes in the project and then include only the classes that you want to obfuscate.
Then, slowly add more classes to the inclusion list until you are satisfied with the obfuscation.

Exclude: **
Include: <your classes>

## Important Considerations

If you want to include a certain method in an excluded class, you should exclude only the methods of the class and then include the specific method. For example:

### Bad example

Exclude: foo/bar/GUI (excludes the whole GUI class)
Include: foo/bar/GUI#main (this will not work)

### Good example

Exclude: foo/bar/GUI#* (excludes only the methods of the GUI class)
Include: foo/bar/GUI#main (now this will work)

Exclusion is always processed before inclusion.
