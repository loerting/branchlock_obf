# Frequently Asked Questions

## What Does Branchlock Offer?

Branchlock is a sophisticated SaaS-technology designed to enhance the security of your Java and Android projects. It operates by encrypting the compiled code of your projects, rendering it virtually impossible to restore to its original form. This is a significant upgrade from conventional obfuscators, which typically only rename methods, fields, and class names, leaving constants like API keys and private URLs unchanged. These can be easily viewed by decompilers or scanned by automated bots, posing a security risk.

Branchlock takes this a step further by encrypting class pool constants such as strings, numbers, and even references. This provides robust protection for your Java projects, ensuring that the original code structure cannot be recovered at all. Furthermore, Branchlock offers different encryption modes, each with varying effects on the security and performance of your application.

## What Types of Projects Can Branchlock Obfuscate?

We provide support for a wide range of projects, including:

- Java Standalone Applications
- Java Libraries
- Java Applets
- Spring Boot Applications
- Android Applications
- Java Virtual Machine Languages (Kotlin, Groovy, Scala, etc.)
- JavaFX and Swing Applications (GUI)
- Java Web Applications (Servlets, JSP, JSF, etc.)
- and more.

If you are unsure whether your project is supported, feel free to try our demo or reach out to us.

## How Robust is the Security Provided by Branchlock?

Branchlock is designed to make it extremely difficult for attackers to reverse engineer your project. While total protection is not feasible, the complexity and time-consuming nature of the obfuscation process created by Branchlock means that many hours would be wasted attempting to decipher the code.

Cracking, or removing access restrictions in your project, also becomes more challenging with Branchlock. This is due to the use of integrity checks and "crashers" - exploits that crash reverse engineering tools - to protect your code as much as possible.

## Can Multiple Obfuscators Be Used Together?

While it's technically possible to use multiple obfuscators, we do not recommend it. Branchlock is a highly powerful obfuscator that already provides robust security. Using additional obfuscators could lead to unnecessary performance costs without adding any additional security benefits.

## How Does Branchlock Ensure Confidentiality?

When you upload code to our Services, we collect and temporarily store it. We guarantee that it will never be shared with third parties. User data protection is our top priority. Depending on the settings, user input files are either deleted manually or automatically once the process is complete. In cases where users opt not to automatically delete files (to prevent re-uploading for multiple obfuscation tasks, debugging, testing etc.), the maximum storage duration is 6 hours. Beyond this period, all files are permanently and irreversibly deleted, without the option for recovery.

## Which Java and Android Versions Does Branchlock Support?

Branchlock supports all Java versions from Java 5 to the latest version.
For Android, we support all projects with AGP 8 and above.
If a new version of Java or Android is released, we will add support for it as soon as possible.

## What Should I Do If My Project Does Not Work Correctly After Obfuscation?

If your project does not work correctly after obfuscation, the first step is to carefully review the documentation. If you're still encountering issues, don't hesitate to reach out to us. We are committed to providing assistance and ensuring your project works as expected.

## Can You Reverse Branchlock's Obfuscation?

Reversing obfuscation is mostly impossible.
Our obfuscation is designed to work in a way that makes it extremely difficult to reverse.
Please make sure to back up your source code before using our service.

## Where Can ToS Abuse Be Reported?

If you found out malware or similar has used our service for obfuscation, you can report it to us via email.
We will try our best to take action, but we do not apply any identification methods to our output files.
This means that we cannot guarantee that we can identify the user who used our service for obfuscation.

## Who Develops Branchlock?

Branchlock is developed by a dedicated team based in Europe with extensive experience in Java bytecode. Our mission is to revolutionize Java obfuscation and create a modern, profitable product that is accessible to everyone.
