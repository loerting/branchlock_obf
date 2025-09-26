# New exclusion handling

In Branchlock 4, exclusion handling is done a bit differently. Classes or methods will not have a field indicating whether they are excluded or not. 
Instead, you have to supply IExclusionHandler implementations for each Drive. These implementations are responsible for determining whether a class or method is excluded or not.
There will be a default implementation for task exclusion and general exclusion, but you can also create your own.