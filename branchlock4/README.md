A clean rewrite of branchlock.

TODO: Write test for inputting cyclic dependencies.
TODO: Integrate Method equivalence into BMethodContainer instead of computing it using MemberLink factory.


To consider when porting task from bl3 to bl4:

BClass.node().methods.add() is not allowed anymore. Use BClass.addMethod() instead.
dataProvider.getClasses().add() is not allowed anymore. Use dataProvider.addClass() instead.
BClass.node().fields.add() is not allowed anymore. Use BClass.addField() instead.


TODO: consider case where