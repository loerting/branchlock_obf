package net.branchlock.layout.android;

import java.io.File;
import java.lang.reflect.Method;

@SuppressWarnings("ALL")
public class ChecksAndroid {
  public static void checkDebug() {
    try {
      Outer:
      while (true) {
        Class<?> aClass = Class.forName("android.os.Debug");
        for (Method m : aClass.getDeclaredMethods()) {
          int i = m.getName().hashCode();
          if (i == -1619124258 || i == 847715197) { //  "isDebuggerConnected", "waitingForDebugger"
            if ((boolean) m.invoke(null)) {
              continue Outer;
            }
          }
        }
        break;
      }
    } catch (Throwable ignored) {
    }
  }

  public static void checkRoot(Object activity) {
    String[] paths = {"/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
      "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
    for (String path : paths) {
      if (new File(path).exists()) {
        try {
          Class<?> aClass = Class.forName("android.widget.Toast");
          for (Method m : aClass.getDeclaredMethods()) {
            if (m.getName().hashCode() == 40507451 && m.getParameterTypes()[1] != int.class) { // "makeText"
              Object toast = m.invoke(null, activity, "This application can't run on a rooted android device", 1);
              for (Method m2 : toast.getClass().getMethods()) {
                if (m2.getName().hashCode() == 3529469) { //"show"
                  m2.invoke(toast);
                }
              }
            }
          }
          for (Method m : activity.getClass().getMethods()) {
            if (m.getName().hashCode() == 1392170715) { // "finishAffinity"
              m.invoke(activity);
              return;
            }
          }
          //noinspection InfiniteLoopStatement,StatementWithEmptyBody
          while (true) {
          }
        } catch (Throwable ignored) {
        }
      }
    }
  }


}
