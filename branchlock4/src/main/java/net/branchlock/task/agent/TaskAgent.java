package net.branchlock.task.agent;

import net.branchlock.structure.provider.DataProvider;
import net.branchlock.task.Task;
import net.branchlock.task.driver.IDriver;

import java.util.List;

/**
 * Responsible for executing tasks.
 */
public class TaskAgent {
  private final Task task;
  private final DataProvider dataProvider;

  public TaskAgent(Task task, DataProvider dataProvider) {
    this.task = task;
    this.dataProvider = dataProvider;
  }

  public void execute() {
    task.preExecute();
    List<IDriver<?>> drivers = task.getDrivers();
    if (drivers == null) throw new IllegalStateException("Task " + task.getMetadata().name() + " returned null drivers");

    for (IDriver<?> driver : drivers) {
      if (!new DriverRunner(dataProvider).runDriver(driver)) break;
    }
    task.postExecute();
  }
}
