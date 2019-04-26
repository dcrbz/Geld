package bz.dcr.geld.logging;

import java.util.ArrayList;
import java.util.List;

public class LogUtils {

    public static List<String> getCallers(StackTraceElement[] ste) {
        final List<String> callers = new ArrayList<>();
        int current = 0;
        for (StackTraceElement e : ste) {
            if (current == 4)
                break;

            callers.add(e.getClassName());
            current++;
        }

        return callers;
    }

}
