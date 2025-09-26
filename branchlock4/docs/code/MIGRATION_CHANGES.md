# Changes to note from Branchlock 3 to Branchlock 4

Always prefer to use BClass and BMethod over ClassNode and MethodNode. Ignoring this rule can result in a lot of
problems.
When destroying a ClassNode or MethodNode, also make sure that the BClass or BMethod is destroyed as well, or their
nodes updated. Don't forget to update the link tree as well (equivalence sets and class tree).