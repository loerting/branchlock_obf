package net.branchlock.commons.os;

public class TimeLimiter {

  public static void withLimit(int ms, Runnable toLimit, Runnable onTimeout) {
    Thread limitableThread = new Thread(toLimit);
    limitableThread.start();

    Thread timeLimiterThread = new Thread(() -> {
      try {
        Thread.sleep(ms);
        onTimeout.run();
        limitableThread.interrupt();
        // limitableThread.stop(); deprecated
      } catch (InterruptedException e) {
        // thread finished before time limit -> nothing to interrupt or do!
      }
    });
    timeLimiterThread.start();

    try {
      limitableThread.join();

    } catch (InterruptedException e) {
      // limitable thread was suspended
      return;
    }
    // limitable thread ended before time limit, stop interrupter thread
    timeLimiterThread.interrupt();
  }

}
