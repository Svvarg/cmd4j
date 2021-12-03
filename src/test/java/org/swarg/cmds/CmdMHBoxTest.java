package org.swarg.cmds;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 02-12-21
 * @author Swarg
 */
public class CmdMHBoxTest {

    @Test
    public void test_CreateCommandMHContainers_Simply() {
        System.out.println("CreateCommandMHContainers Simply");
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        String mn = "cmdSumWithoutAnnotation";
        CmdMHBox c = CmdMHBox.of(lookup, Model.class, mn, false);
        assertNotNull("found", c);
        assertEquals(mn, c.name);
        assertNotNull(c.getMethodHandle());
    }

    @Test
    public void test_CreateCommandMHContainers_Ann() throws NoSuchMethodException {
        System.out.println("CreateCommandMHContainers Ann");
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        String mn = "cmdSum";//@ACmd(name="sum", desc="sum of two ints", usage="sum (int) (int)")
        Method m = Model.class.getMethod(mn, new Class[]{IArgsWrapper.class});
        CmdMHBox c = CmdMHBox.of(lookup, m);
        assertNotNull("found", c);
        assertEquals("sum", c.name());
        assertEquals(null, c.sname());
        assertEquals("sum of two ints", c.desc());
        assertNotNull(c.getMethodHandle());
    }

    @Test
    public void test_Example() throws NoSuchMethodException {
        System.out.println("Example");
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        String mn = "cmdSum";//@ACmd(name="sum", desc="sum of two ints", usage="sum (int) (int)")
        Method m = Model.class.getMethod(mn, new Class[]{IArgsWrapper.class});
        CmdMHBox c = CmdMHBox.of(lookup, m);

        //reg
        Map<String, CmdMHBox> map = new HashMap<>();
        map.put(c.name, c);

        //usage
        String[] args = new String[]{"sum", "8", "16"};
        ArgsWrapper w = new ArgsWrapper(args);

        String cmd = w.arg(w.ai++);
        CmdMHBox box = map.get(cmd);
        Throwable t = box.perform(w);
        Object res = w.pull(Object.class);
        assertEquals(24, res);
        //System.out.println("Res:"+res);

    }
}
