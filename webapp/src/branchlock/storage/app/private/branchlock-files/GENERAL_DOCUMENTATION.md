# Introduction To Branchlock

Branchlock offers obfuscation for binary files of Java desktop applications, Android applications, and Java Virtual Machine languages like Kotlin and Groovy. 
The demo version of Branchlock provides a small overview of the service, but it doesn't offer the full range of features. 
Obfuscation can be performed on different projects:

## Supported Project Types

We provide support for a wide range of projects, including:

- Java Standalone Applications
- Java Libraries
- Java Applets
- J2ME and Jakarta EE Applications
- Android SDK Applications (Java & Kotlin only, no cross-platform app tools)
- Compose Desktop / Android Applications
- Spring Boot Applications
- Java Virtual Machine Languages (Kotlin, Groovy, Scala, etc.)
- JavaFX and Swing Applications (GUI)
- Java Web Applications (Servlets, JSP, JSF, etc.)
- and more.

Branchlock supports all Java versions from Java 5 to the latest version. 
For Android, we support all projects with AGP 7 and above. 
If a new version of Java or Android is released, we will add support for it as soon as possible.
Note that some project types require some configuration work to be obfuscated correctly.

## Obfuscating Java Archives, Desktop Apps, Server Apps, Class Files

To obfuscate Java archives (JAR files), open the web app and create a new project. Upload the Java binary you wish to obfuscate. 
Our service will perform obfuscation on your application and return an obfuscated output JAR file.
Branchlock supports most JVM languages like Kotlin and Groovy.

## Obfuscating Android Applications

Obfuscation in Android operates similarly to its counterpart in Java, though there are distinctions in the tasks available. 
Certain tasks are exclusive to Android projects, whereas others are unique to Java. 
To initiate obfuscation for an Android project, begin by creating a new project and choose the "Android" option. 
Branchlock provides an Android Gradle plugin for application obfuscation. 
Detailed setup instructions for the Gradle plugin can be found within the input section of each Android project.
Also take a look at the "Branchlock Gradle Plugin" section for more information.

# Why Obfuscation Is Necessary

In the realm of software development, obfuscation plays a crucial role in enhancing the security and intellectual property protection of applications. 

## Understanding Java Bytecode

Java bytecode is an intermediate representation of computer programs written in the Java programming language or similar languages. 
It serves as a platform-independent instruction set that runs on the Java Virtual Machine (JVM). 
Unlike native binaries, which are compiled directly into machine code for a specific hardware architecture, Java bytecode is designed to run across different platforms without modification. 
This portability comes at the cost of security, as bytecode is more susceptible to manipulation and reverse engineering.

## The Security Concerns of Java Bytecode

The primary reason Java bytecode is considered less secure than native binaries lies in its design philosophy. 
While the goal of bytecode is to promote cross-platform compatibility, it inadvertently introduces several security risks:

- Decompilation Vulnerabilities: Bytecode can be easily decompiled back into readable Java source code. This makes it easier for attackers to understand the application's logic and find potential vulnerabilities.
- Manipulation Risks: Since bytecode operates at a higher level of abstraction than native code, it is more prone to manipulation. Attackers can modify the bytecode to alter the behavior of the application, introducing malicious functionalities or bypassing security controls.
- Lack of Direct Hardware Access: Native binaries have direct access to system resources and hardware, allowing them to implement security measures that are difficult to replicate in bytecode. This includes hardware-based encryption and secure memory management features.

## The Role of Obfuscation in Enhancing Security

Given these security concerns, obfuscation becomes a vital tool for protecting Java applications. 
Obfuscation transforms the bytecode into a form that is difficult to understand and reverse engineer, thereby increasing the barrier to entry for attackers. 
Key benefits of obfuscation include:

- Enhanced Code Complexity: By transforming simple constructs into complex equivalents, obfuscation increases the cognitive load required to understand the application's functionality, making reverse engineering more challenging.
- Protection Against Decompilation: Obfuscated bytecode is harder to decompile accurately, as the transformations applied during obfuscation obscure the original structure of the code.
- Prevention of Manipulation: Obfuscation can make it harder for attackers to modify the bytecode without breaking the application's functionality. This helps protect against tampering and unauthorized modifications.

By adopting Branchlock, developers can significantly bolster the security posture of their projects, aligning with the evolving threat landscape and ensuring the integrity and confidentiality of their applications.

# Licenses

Branchlock provides various licensing options tailored to different needs. 
These licenses are available on a monthly or annual basis, with payments made upfront and not recurring automatically. 
Upon expiration of the subscription period, the license reverts to a demo version, requiring a fresh purchase for continued access. 
The demo version serves as a complimentary introduction to the service, offering limited functionality at no cost.
To find out more about the available licenses, head to the licenses page.

# Project Configuration

Branchlock offers a wide range of configuration options.
The configuration is specified in the project settings.

## Tasks

Tasks are different obfuscation techniques with different goals. 
They can be used in combination with each other to achieve the best results.
Each task affects the runtime and security of the obfuscated application. 

Let's take a look at the "String Encryption" task.
This task encrypts all strings in the application, which makes it harder to understand the application's code:

```java
    public class VerySecureLogin {
        public static void main(String[] args) {
            Scanner s = new Scanner(System.in);
            if (s.nextLine().equals("password")) {
                System.out.println("Correct!");
            }
        }
    }
```

After using String encryption, the output could look like this:

```java
    public class VerySecureLogin {
        public static void main(String[] args) {
            Scanner s = new Scanner(System.in);
            if (s.nextLine().equals(自[1 << 9184])) {
              System.out.println(自[0x280000 >>> 10163]);
            }
        }
    }
```

Some tasks should be used sparingly, while others should be used on the whole application. 
By hovering over a task, you can see a description of what it does and how it affects the performance of the application.
Some tasks have internal settings that can also be specified.
Note that Android projects have a different set of tasks than Java archives.

## Ranges

With "Obfuscation Ranges", you can specify what should be obfuscated in your application, and what shouldn't be. 
Excluding a class means that it will remain untouched by a certain task.
Including a class means that if it has been excluded before, the exclusion will be removed.
Be aware that excluding a class does not mean that it remains unchanged.
Renamed reference names or signatures, for example, have to be updated in all classes.
For more details, refer to the Ranges documentation.

## Handling JVM Languages

Branchlock supports most JVM languages like Kotlin and Groovy.
To obfuscate, make sure to include the runtime classes of your language as a library (if they aren't already included).
For Kotlin, this would be the Kotlin Standard Library.

## Branchlock Annotations

Branchlock offers a set of annotations that can be used to control the obfuscation process.
These annotations can be used to exclude classes, methods, and fields from obfuscation more easily.

Here is a small example of how the annotations can be used:

```java
    import net.branchlock.annotations.*;

    @ForceRename /* Ensures that the class name is changed, even if Java Reflection usage is detected */
    public class MyClass {
        
        @RetainSignature /* Ensures that the method name is not changed */
        @EntryPoint /* Mark this method as a possible entry point for the Trimmer task */
        public void dynamicMethod() {
            ...
        }
        
        @CallerSensitive /* Ensures that the references to this method are not encrypted */
        public void specialMethod() {
            var calledBy = new Throwable().getStackTrace()[0].getMethodName();
            ...
        }
    }
```


## Handling Missing Reference Errors

After obfuscation, a list of missing references is provided.
These classes were neither found in the input nor in the libraries, but were needed by Branchlock to obfuscate the application.
Not providing these classes can lead to unwanted errors.
If the application works as expected, you can ignore these warnings.
If you are using dynamic libraries make sure to provide extra Java archives that provide those classes.

## Good To Know

- Spring applications may have to use the "No compression" setting.
- For total anonymity, licensed users can disable the watermark.
- If you are obfuscating a Java ME or Jakarta EE application, make sure to add the runtime classes of your application server as a library.

# Achieving Correct And Secure Obfuscation

Achieving a good obfuscation is not easy and has a lot to do with trial-and-error. 
You will need some time to find the best configuration for your project.

## Determining Ranges

First, consider which parts of your application need good security protection, and which don't. Tasks that are expensive on runtime should only be used on classes that need extra protection. If you are obfuscating a fat-jar (Java application that includes both your runtime classes and all library classes), make sure to exclude all library classes. 
This is achieved by excluding all classes first, and then including your runtime classes.

## Configuration Testing

It is very essential to test the obfuscated application after obfuscation.
Features may break easily through a bad configuration.
To make sure everything has been obfuscated correctly and is secure, we recommend using a decompiler to view the obfuscated output.

## Debugging Broken Obfuscation

If the output does not work as expected, then try to approach it in a slow and methodical way.
Start by only using one task, and then slowly increase the obfuscation range and the amount of tasks used.
Just because the output does not work on the first try, it doesn't mean that the obfuscator does not work correctly.
In most cases, the config is faulty.
Also read the obfuscator log to find out what could have gone wrong.
This also applies to the Gradle plugin for Android.

## Target Java Versions

Make sure to provide the correct target Java version to ensure compatibility.
The obfuscator will use different techniques based on the target Java version.

## Java Reflection Usage

Obfuscation and Reflection don't harmonize with each other. 
Make sure to be careful with tasks that can be broken by Reflection usage. 
For example: Renaming a class that uses the getResource() method to retrieve a resource file in the package. 
It is best to exclude classes that use Reflection from obfuscation.
To make things easier for you, Branchlock detects some cases of Reflection automatically:

```java
    public class MyClass {
        public void myMethod() {
            // If Foo is renamed, this String would be different and cause a change in behavior. 
            // Branchlock handles this case automatically by ensuring the class FooBar is not renamed.
            String otherClassName = FooBar.class.getName();
            if (otherClassName.startsWith("Foo")) {
              ...
            }
            // Branchlock does NOT handle this case automatically, as the parameter passed to Class.forName() in getPackage() is not fully known.
            // The class my/cool/Bar could get renamed and break the application.
            String otherClassPackage = getPackage("my/cool/Bar".substring(3));
            ...
        }
        
        public String getPackage(String className) {
            return Class.forName(className).getPackageName();
        }
    }
```

## Layering Obfuscation

While it's technically possible to use multiple obfuscators, we do not recommend it. 
Using additional obfuscators could lead to unnecessary performance costs without adding any additional security benefits.

## Salting Task For Security

If you are looking for the best security, always use the "Salting" task in combination with other tasks. 
It will protect your application from static analysis and decryption methods.

To understand how the salting task works, let's take a look at the following real-world example, which was obfuscated using Salting and Reference Encryption:

```java
    public static void registerCustomDefaults(Object var0, int var1 /* Inserted by Salting */) {
        Object[] var3 = Np;
        // These are encrypted references:
        if (x.qz(Laf.class, var3, var1 ^ 54) == null) {  
            x.rA(new ArrayList(), Laf.class, var3, var1 ^ 73);
        }
        x.cA((List)(x.qz(Laf.class, var3, var1 ^ 12)), var0, var3, var1 ^ 121);
    }
```

If you were to decrypt the encrypted references (which is already difficult), the parameter `var1` would be needed, which was inserted by the Salting task.
This parameter is passed onwards through multiple functions and modified in the process.
To trace the value of `var1`, you would need to know the original references (or else you wouldn't know where calls come from or lead to), which is not possible without decrypting all encrypted references.
Those two tasks converge extremely well, and now force a potential attacker to simulate a custom virtual machine.

# Branchlock Gradle Plugin / IDE Integration

The Branchlock Gradle plugin is an integration plugin for Gradle.
It can be used for obfuscating desktop applications and Android applications, directly from the Gradle build system.
The setup is specified in each project.
Branchlock does not offer a plugin for other build systems like Maven.

## For Android Projects

To use the Branchlock Gradle plugin for Android, your Android project will have to use at least AGP 7.
Our plugin is also compatible with AGP 8.
The Android plugin directly hooks onto the build process of the Android Gradle plugin. 
By building a release container, the plugin will obfuscate the application using our API.

## For Desktop Gradle Projects

The Desktop gradle plugin hooks onto every "jar" task.
This means you can e.g. also use the "shadow" plugin to create a fat-jar, which is then automatically obfuscated.
It will create an obfuscated jar file in the build directory.

## Troubleshooting

If you are experiencing problems with the Gradle plugin, make sure to check the log output.
The log output can be found in the Gradle console or in the Gradle log file.
Clearing the Gradle cache or rebuilding / cleaning the project can also help.

# Stacktrace Decryption

Stacktrace encryption is implemented using the Triple DES encryption algorithm, is implemented using the Triple DES encryption algorithm, which is a secure method for encrypting stacktraces and provides minimal size overhead.
To decrypt a stacktrace which has been encrypted using the "Stacktrace Encryption" task, head to the "Stacktrace" page in the web application.
Paste the stacktrace into the input field and enter the decryption key.
The decrypted stacktrace will be displayed in the output field.

# Reporting Abuse

If you found out malware or similar has used our service for obfuscation, you can report it to us. 
We will try our best to take action.
We do not apply any identification methods to our output files.
