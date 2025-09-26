# Goals of Branchlock 4 versus Branchlock 3.

Here is a list of goals for Branchlock 4. 
The focus is on improving the codebase, making it easier to maintain and extend, while also improving the performance of the codebase.

## Independence of ASM's MethodNode and ClassNode
Only the decorator classes should be used. The ASM classes should not be used directly.
This has the following advantages:
- We can build the link tree only once and update it when needed, without worrying about the ASM classes.
- More utility methods can be added to the decorator classes.

## Ordered class tree and method equivalence sets (thoughts)

The ordered class tree is a tree of classes. 
Each class can have one superclass, and multiple superinterfaces.
Each class can have multiple subclasses (including subinterfaces).
From this definition, we can build a class tree, where each class has a unique path to the root class, Object.
This is the ordered class tree.

### Building the ordered class tree
If we build the tree partially for each class, it will take longer than building the tree for all classes at once.

Each class has information about its superclass and superinterfaces, but not about its subclasses.
Therefore, finding the subclasses of a class will take O(n) time, where n is the number of classes.
If we build the tree for all classes at once, we can find the subclasses of a class in O(1) time.
