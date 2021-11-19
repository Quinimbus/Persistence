package cloud.quinimbus.persistence.common;

import java.util.Locale;

public class Records {
    
    public static String idFromClassName(String name) {
        return name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
    }
}
