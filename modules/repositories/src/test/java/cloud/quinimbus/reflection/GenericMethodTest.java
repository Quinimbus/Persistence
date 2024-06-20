package cloud.quinimbus.reflection;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class GenericMethodTest {

    public static interface Generic<T1, T2> {
        String getString();

        T1 get1();

        T2 get2();

        void setString(String s);

        void set1(T1 t);

        void set2(T2 t);
    }

    public static interface StringImpl extends Generic<String, Number> {}

    @Test
    public void testGetActualReturnType() throws NoSuchMethodException {
        var getString = new GenericMethod(StringImpl.class, StringImpl.class.getMethod("getString"));
        var get1 = new GenericMethod(StringImpl.class, StringImpl.class.getMethod("get1"));
        var get2 = new GenericMethod(StringImpl.class, StringImpl.class.getMethod("get2"));

        var setString = new GenericMethod(
                StringImpl.class,
                Arrays.stream(StringImpl.class.getMethods())
                        .filter(m -> m.getName().equals("setString"))
                        .findFirst()
                        .get());
        var set1 = new GenericMethod(
                StringImpl.class,
                Arrays.stream(StringImpl.class.getMethods())
                        .filter(m -> m.getName().equals("set1"))
                        .findFirst()
                        .get());
        var set2 = new GenericMethod(
                StringImpl.class,
                Arrays.stream(StringImpl.class.getMethods())
                        .filter(m -> m.getName().equals("set2"))
                        .findFirst()
                        .get());

        assertEquals(String.class, getString.getActualReturnType());
        assertEquals(String.class, get1.getActualReturnType());
        assertEquals(Number.class, get2.getActualReturnType());
        assertEquals(String.class, setString.getActualParameterType(0));
        assertEquals(String.class, set1.getActualParameterType(0));
        assertEquals(Number.class, set2.getActualParameterType(0));
    }
}
