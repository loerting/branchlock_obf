# Task calling and linking

## Old method

Prior to Branchlock 4, tasks had metadata that was provided using a TaskMetadata annotation. This annotation was used to
provide the task with a name, description, and a list of parameters. This metadata was used to generate the task's help
text, and to validate the parameters passed to the task.
Tasks were instantiated via reflection, and there was one class that linked a task-id to a task class. This class was
called TaskRegistry.

## New method

Tasks are now instantiated using a TaskFactory. The TaskFactory is responsible for linking a task-id to a task class,
and then instantiating the task class.