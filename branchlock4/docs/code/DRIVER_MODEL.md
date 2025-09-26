# Driver model for task execution.

In Branchlock 4, task execution is done a bit differently. Instead of a "perform" method,
you have to supply IDriver implementations. 
These implementations are responsible for executing certain things on either classes or methods.

## IDriver<T>

The IDriver interface is the base interface for all drivers.
Drivers are passed through the method `Task.getDrivers`, and are executed in the order they are returned.

