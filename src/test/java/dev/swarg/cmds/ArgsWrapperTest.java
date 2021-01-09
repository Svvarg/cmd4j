package dev.swarg.cmds;

import dev.swarg.cmds.ArgsWrapper;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 08-01-21
 * @author Swarg
 */
public class ArgsWrapperTest
{

    public ArgsWrapperTest() {
    }


    // ---------     Exapmle     ----------------
    @Test
    public void usageExample() {
        String input = "cmd jvm uptime";
        String[] args = input.split(" ");

        ArgsWrapper w = new ArgsWrapper(args);
        
        if (w.isCmd("cmd")) {
            if (w.isCmd("jvm")) {
                cmdJvm(w);
                //что-то делать с полученным результатом работы обработчика команды обёрнутой в argsWrapper
                String msg = (String)w.pull(String.class);
                assertEquals("work result", msg);

                return; //System.out.println(msg); | toSender(msg) | return cmdJvm(w)
            }
        }
        
        fail("it should not be entered here");
    }

    public boolean cmdJvm(ArgsWrapper w) {
        if (w.isHelpCmd()) {
            return w.push("help and usage");
        }
        else if (w.isCmd("uptime", "u")) {
            //do something
            return w.push("work result");
        }
        else if (w.isCmd("histo", "h")) {
            //..
            return w.push("class histogram");
        }
        //
        return true;
    }
    // --- END ----    Exapmle     --------------------\\


    
    /**
     * Test of setArgs method, of class ArgsWrapperImpl.
     */
    @Test
    public void testSetArgs() {
        System.out.println("setArgs");
        String[] args = "1 2 3 4".split(" ");
        ArgsWrapper w = new ArgsWrapper(args);
        w.setArgs(args);
        assertEquals(4, w.argsCount());
        for (int i = 0; i < 4; i++) {
            int ival = w.argI(w.ai++);
            assertEquals(i+1, ival);
        }
        assertArrayEquals(args, w.getArgs());

        w.setArgs("5 6 7 8 9".split(" "));
        for (int i = 0; i < 4; i++) {
            int ival = w.argI(w.ai++);
            assertEquals(i+5, ival);
        }
    }

    /**
     * Test of push method, of class ArgsWrapperImpl.
     */
    @Test
    public void testPushPull() {
        System.out.println("push-pull");

        ArgsWrapper w = new ArgsWrapper(null);
        Object ans = "thing";
        w.push(ans);
        assertEquals(ans, (String)w.pull(String.class));

        w.push(16);
        assertEquals(null, (String)w.pull(String.class));

        assertEquals(16, (Number)w.pull(Number.class));
    }


    /**
     * Test of argsCount method, of class ArgsWrapperImpl.
     */
    @Test
    public void testArgsCount_0args() {
        System.out.println("argsCount");
        ArgsWrapper w = new ArgsWrapper("a b c d e f".split(" "));
        
        assertEquals(6, w.argsCount());
        assertEquals(6, w.argsRemain());
        assertEquals(5, w.inc().argsRemain());
        assertEquals(4, w.inc().argsRemain());
    }

    /**
     * Test of noArgs method, of class ArgsWrapperImpl.
     */
    @Test
    public void testNoArgs_int() {
        System.out.println("noArgs");
        int i = 0;
        ArgsWrapper w = new ArgsWrapper(null);
        assertEquals(true, w.noArgs());
        w.setArgs("1 2 3".split(" "));
        assertEquals(3, w.argsCount() );

        assertEquals("1", w.arg(w.ai++));
        assertEquals(3, w.argsCount()); //всего аргументов независимо от ai
        assertEquals(false, w.noArgs() );

        assertEquals(2, w.argsRemain()); //всего аргументов независимо от ai

        for (int j = 2; j < 4; j++) {
            assertEquals(j, w.argI(w.ai++));
        }
        assertEquals(true, w.noArgs());

    }

    /**
     * Test of noArgs method, of class ArgsWrapperImpl.
     */
    @Test
    public void testNoArgs_0args() {
        System.out.println("noArgs");
        ArgsWrapper w = new ArgsWrapper();
        assertTrue( w.setArgs(new String[]{}).noArgs() );
        assertEquals(false, w.setArgs(new String[]{null}).noArgs() );
        assertEquals(null, w.arg(w.ai));
        assertEquals(false, w.setArgs(new String[]{""}).noArgs() ); //ai++
        assertEquals("", w.arg(w.ai));

        assertEquals(false, w.setArgs(new String[]{""}).noArgs() );
        
        assertTrue( w.setArgs(new String[]{"1", ""}).inc().inc().noArgs() );

    }

    /**
     * Test of hasArg method, of class ArgsWrapperImpl.
     */
    @Test
    public void testHasArg_int() {
        System.out.println("hasArg");
        int i = 0;
        ArgsWrapper w = new ArgsWrapper();
        assertEquals(false, w.hasArg());
        assertEquals(0, w.argsCount());
        assertEquals(0, w.argsRemain());

        w.setArgs("0 1".split(" "));
        assertEquals(false, w.hasArg(-1));
        assertEquals(true, w.hasArg(0));
        assertEquals(true, w.hasArg(1));
        assertEquals(false, w.hasArg(2));
    }

    @Test
    public void testToString() {
        System.out.println("toString");

        ArgsWrapper w = new ArgsWrapper();
            //      0  1 2 3
        w.setArgs("cmd A B C".split(" "));
        w.ai = 1;

        assertEquals("Args:[cmd |A| B C] AI:1 Response: null", w.toString());
        assertEquals("Args:[cmd A |B| C] AI:2 Response: null", w.inc().toString());
        if (!w.isCmd("B")) {
            fail("exp B");
        }
        assertEquals(3, w.ai);//auto ai++ in isCmd
        w.push("response");

        assertEquals("Args:[cmd A B |C|] AI:3 Response: String.length:8", w.toString());
        String response = (String) w.pull(String.class);
        assertEquals("response", response);
        assertEquals(response, w.pull(null));//pull "не забивает" значение, оно остаётся на месте внутри обёртки
    }

    @Test
    public void testArg_int_String() {
        System.out.println("arg");
        
        String def = "def";
        ArgsWrapper w = new ArgsWrapper();
        w.setArgs("cmd do".split(" "));
        assertEquals("cmd", w.arg(w.ai++, def));
        assertEquals("do",  w.arg(w.ai++, def));
        assertEquals("def", w.arg(w.ai++, def));
    }

    @Test
    public void testArgB_int() {
        System.out.println("argB");

        ArgsWrapper w = new ArgsWrapper();
        w.setArgs("true yes on + false -".split(" "));
        assertEquals(true, w.argB(w.ai++));
        assertEquals(true, w.argB(w.ai++));
        assertEquals(true, w.argB(w.ai++));
        assertEquals(true, w.argB(w.ai++));//+
        assertEquals(false, w.argB(w.ai++));
        assertEquals(false, w.argB(w.ai++));
    }

    @Test
    public void testArgB_int_boolean() {
        System.out.println("argB");
                
        ArgsWrapper w = new ArgsWrapper();
        assertEquals(true, w.argB(w.ai, true));
        w.setArgs("cmd do if".split(" "));
        w.ai = 3;
        assertEquals(true, w.argB(w.ai, true));
        //         0    1  2  3
        w.setArgs("cmd do if false".split(" "));
        w.ai = 3;
        assertEquals(false, w.argB(w.ai, true));
    }

    /**
     * Test of argI method, of class ArgsWrapperImpl.
     */
    @Test
    public void testArgI_int_int() {
        System.out.println("argI");

        ArgsWrapper w = new ArgsWrapper();
        assertEquals(true, w.argB(w.ai, true));
        w.setArgs("cmd iter x".split(" "));
        w.ai = 2;
        int def = 16;
        assertEquals(def, w.argI(w.ai, def));

        w.setArgs("set 888".split(" "));
        w.ai = 1;
        assertEquals(888, w.argI(w.ai, def));
    }

    @Test
    public void testArgL() {
        System.out.println("argL");

        long def = 32L;
        ArgsWrapper w = new ArgsWrapper();
        w.setArgs(new String[]{ ""+Long.MIN_VALUE, ""+Long.MAX_VALUE, "777", "-777", "--x" });

        assertEquals(Long.MIN_VALUE, w.argL(w.ai++, def));//0
        assertEquals(Long.MAX_VALUE, w.argL(w.ai++, def));
        assertEquals(777, w.argL(w.ai++, def));
        assertEquals(-777, w.argL(w.ai++, def));//4
        assertEquals(def, w.argL(w.ai++, def));//4
    }


    /**
     * Test of argD method, of class ArgsWrapperImpl.
     */
    @Test
    public void testArgD() {
        System.out.println("argD");
        double def = 32.32D;
        double delta = 0.001D;

        ArgsWrapper w = new ArgsWrapper();
        w.setArgs(new String[]{ ""+Double.MAX_VALUE, ""+Double.MIN_VALUE, "7.77", "-7.77", "--x" });

        assertEquals(Double.MAX_VALUE, w.argD(w.ai++, def), delta);//0
        assertEquals(Double.MIN_VALUE, w.argD(w.ai++, def), delta );
        assertEquals(7.77,  w.argD(w.ai++, def), delta );
        assertEquals(-7.77, w.argD(w.ai++, def), delta );
        assertEquals(def,   w.argD(w.ai++, def), delta );
    }

    /**
     * Test of argClass method, of class ArgsWrapperImpl.
     */
    @Test
    public void testArgClass() {
        System.out.println("argClass");
        int i = 0;

        ArgsWrapper w = new ArgsWrapper();
        w.setArgs(new String[]{"java.lang.String", "java.net.HttpCookie"});
        boolean canLoaded = true;
        Class clazz = w.argClass(0, canLoaded);
        assertEquals(java.lang.String.class, clazz);

        Class clazz2 = w.argClass(1, false);//false - выдавать классы если они уже загружены в класслоадером
        assertEquals(null, clazz2);

        java.net.HttpCookie cookie = new java.net.HttpCookie("k","v");//load Class
        Class clazz3 = w.argClass(1, false);
        assertEquals(java.net.HttpCookie.class, clazz3);
    }

    @Test
    public void test_argMethod() {
        System.out.println("argMethod");

        ArgsWrapper w = new ArgsWrapper();
        //java.lang.String//public static String valueOf(Object obj) {
        //java.lang.Object
        String[] input = "valueOf ( java.lang.Object )".split(" ");
        w.setArgs(input);
        Object m = w.argMethod(java.lang.String.class);
        assertNotEquals(null, m);

        //primitives
        w.setArgs("valueOf ( int )".split(" ")).argMethod(java.lang.String.class);
        assertNotEquals(null, m);

        w.setArgs("valueOf ( double )".split(" ")).argMethod(java.lang.String.class);
        assertNotEquals(null, m);

        Object m2 = w.setArgs("setComment ( java.lang.String )".split(" ")).argMethod(java.net.HttpCookie.class);
        assertNotEquals(null, m2);

        Object m3 = w.setArgs("getComment ( )".split(" ")).argMethod(java.net.HttpCookie.class);
        assertNotEquals(null, m3);

        Object m4 = w.setArgs("getComment ()".split(" ")).argMethod(java.net.HttpCookie.class);
        assertNotEquals(null, m4);
    }

    /**
     * Test of argUUID method, of class ArgsWrapperImpl.
     */
    @Test
    public void testArgUUID() {
        System.out.println("argUUID");
        int i = 0;
        UUID uuid = UUID.randomUUID();
        String [] args = {uuid.toString(),"---82C87-7AfB-4024-BA57-13D2C99CAE77"};//"41C82C87-7AfB-4024-BA57-13D2C99CAE77"
        ArgsWrapper w = new ArgsWrapper(args);

        assertEquals(uuid, w.argUUID(0));
        assertEquals(null, w.argUUID(1));
    }


    /**
     * Test of indexEqualArg method, of class ArgsWrapperImpl.
     */
    @Test
    public void testIndexEqualArg() {
        System.out.println("indexEqualArg");
        ArgsWrapper w = new ArgsWrapper();
        w.setArgs("ab cde fg".split(" "));
                          //0           1       2       3     4      5
        String[] find = {"variants", "for", "search", "fg", "ab", "cde"};
        assertEquals(4, w.indexOfAnyEqualsArg(w.ai++, find));//for ab
        assertEquals(5, w.indexOfAnyEqualsArg(w.ai++, find));//for cde
        assertEquals(3, w.indexOfAnyEqualsArg(w.ai++, find));//for fg
    }
    /**
     * Test of argEqual method, of class ArgsWrapperImpl.
     */
    @Test
    public void testIsArgEqual() {
        System.out.println("argEqual");
        ArgsWrapper w = new ArgsWrapper();
                          //0           1       2       3     4      5   6    7
        String[] args = {"variants", "for", "search", "fg", "ab", "cde", "", null};
        w.setArgs(args);
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            assertEquals("#i"+i, true, w.isArgEqual(i, arg));
        }
    }


    /**
     * Test of isCmd method, of class ArgsWrapperImpl.
     */
    @Test
    public void testIsCmd_3args() {
        System.out.println("isCmd");
        ArgsWrapper w = new ArgsWrapper();
                          //0       1            2          3     4      5   6    7
        String[] args = {"cmd", "sub-cmd", "subX2cmd", "fg", "ab", "cde", "", null};
        w.setArgs(args);

        assertEquals(true,  w.isCmd(0, "cmd", "v"));
        assertEquals(false, w.isCmd(0, "err", "e"));
        assertEquals(true,  w.isCmd(1, "sub-cmd", "sc"));
        assertEquals(true,  w.isCmd(2, "subX2cmd", "s2xc"));
        assertEquals(true,  w.isCmd(3, "fg", "f"));
        assertEquals(true,  w.isCmd(4, "ab", "a"));
        assertEquals(true,  w.isCmd(5, "cde", "c"));
        assertEquals(true,  w.isCmd(5, "cde", "c"));
    }

    /**
     * Test of isCmd method, of class ArgsWrapperImpl.
     */
    @Test
    public void testIsCmd_int_String() {
        System.out.println("isCmd");
        ArgsWrapper w = new ArgsWrapper();
                          //0       1            2          3     4      5   6    7
        String[] args = {"cmd", "sub-cmd", "sub-sub-cmd", "fg", "ab", "cde", "", null};
        w.setArgs(args);
        for (int i = 0; i < 6; i++) {
            String arg = args[i];
            assertEquals("#i"+i, true, w.isCmd(w.ai++, arg ));
        }
        assertEquals(false, w.isCmd(w.ai++, "")); //6
        assertEquals(false, w.isCmd(w.ai++, null)); //7

        assertEquals(false, w.isCmd(w.ai++, "?" ));
        assertEquals(false, w.isCmd(0, "?" ));
        assertEquals(true, w.isCmd(4, "ab" ));

        w.setArgs(new String[]{"cmd", "do"});
        assertEquals(0, w.ai);
        assertEquals(true, w.isCmd("cmd")); //auto ai++ inside
        assertEquals(1, w.ai);
        assertEquals("do", w.arg(w.ai));
    }

    /**
     * Test of isCmd method, of class ArgsWrapperImpl.
     */
    @Test
    public void testIsCmd_String() {
        System.out.println("isCmd");

        ArgsWrapper w = new ArgsWrapper();
                          //0     1       2       3     4      5   6    7
        String[] args = {"cmd", "sub", "search", "fg", "ab", "cde", "", null};
        w.setArgs(args);
        for (int i = 0; i < 6; i++) {
            String arg = args[i];
            assertEquals("#i"+i, true, w.isCmd( arg ));//auto ai++ inside
        }
    }


    /**
     * Test of isHelpCmd method, of class ArgsWrapperImpl.
     */
    @Test
    public void testIsHelpCmd_boolean() {
        System.out.println("isHelpCmd");
        ArgsWrapper w = new ArgsWrapper(new String[]{"help", "-help", "-h"});
        for (int i = 0; i < 3; i++) {
            assertEquals("#i"+i, true, w.isHelpCmd());//auto ai++ inside
        }
    }

    /**
     * Test of isHelpCmdOrNoArgs method, of class ArgsWrapperImpl.
     */
    @Test
    public void testIsHelpCmdOrNoArgs() {
        System.out.println("isHelpCmdOrNoArgs");
        ArgsWrapper w = new ArgsWrapper(new String[]{"help", "-help", "-h"});
        for (int i = 0; i < 4; i++) { //4! на не существующий индекс
            assertEquals("#i"+i, true, w.isHelpCmdOrNoArgs());//auto ai++ inside
        }

        w.setArgs(new String[]{"cmd"});
        assertEquals(true, w.inc().isHelpCmdOrNoArgs());
    }
    /**
     * Проверка на целочисленное число
     */
    @Test
    public void testIsIntArg() {
        System.out.println("IsIntArg");

        int cap = 10;
        String[][] l = new String[cap][];
        boolean[][] e = new boolean[cap][];//onlypositive
        boolean[][] e2 = new boolean[cap][];
        int n = 0;
        final boolean Y = true, N = false;
        /*00*/l[n] =         "cmd 1 2 -1 --1 -l2 16 z -7 1.25 -7.43".split(" ");
        e[n] =  new boolean[]{N,  Y,Y, N,  N, N, Y, N, N, N,  N};
        e2[n] = new boolean[]{N,  Y,Y, Y,  N, N, Y, N, Y, N,  N};
        n++;

        ArgsWrapper w = new ArgsWrapper();
        for (int i = 0; i < n; i++) {
            w.setArgs(l[i]);
            int sz = w.argsCount();
            for (int j = 0; j < sz; j++) {
                boolean onlyPositive = true;
                boolean isInt = w.isIntArg(j, onlyPositive);
                boolean exp = e[i][j];
                assertEquals("#i"+i+"@j"+j, exp, isInt);

                boolean isInt2 = w.isIntArg(j, false);
                boolean exp2 = e2[i][j];
                assertEquals("#i"+i+"@j"+j, exp2, isInt2);

                boolean isInt3 = w.isIntArg(j); // Тоже самое что isIntArg(j,false)
                assertEquals("#i"+i+"@j"+j+"_", exp2, isInt3);
            }
        }

        assertEquals(true, w.setArgs(new String[]{""+Integer.MAX_VALUE}).isIntArg(0));
        assertEquals(true, w.setArgs(new String[]{""+Long.MAX_VALUE}).isIntArg(0));
        assertEquals(true, w.setArgs(new String[]{"9999999999999999999989999999999999"}).isIntArg(0)); //todo max length
    }

    /**
     * Test of isDoubleArg method, of class ArgsWrapperImpl.
     */
    @Test
    public void testIsDoubleArg() {
        System.out.println("isDoubleArg");
        
        ArgsWrapper w = new ArgsWrapper();
        assertEquals(true, w.setArgs(new String[]{""+Double.MAX_VALUE}).isDoubleArg(0)); //1.7976931348623157E308
        assertEquals(true, w.setArgs(new String[]{""+Double.MIN_VALUE}).isDoubleArg(0));
        assertEquals(true, w.setArgs(new String[]{"0"}).isDoubleArg(0));
        assertEquals(true, w.setArgs(new String[]{"1"}).isDoubleArg(0));
        assertEquals(true, w.setArgs(new String[]{"1.1"}).isDoubleArg(0));
        assertEquals(true, w.setArgs(new String[]{"-7.16"}).isDoubleArg(0));
        assertEquals(true, w.setArgs(new String[]{"4.9E-324"}).isDoubleArg(0)); //min
        assertEquals(true, w.setArgs(new String[]{"1.7E308"}).isDoubleArg(0)); //max
    }


    /**
     * Test of isOptKey method, of class ArgsWrapperImpl.
     */
    @Test
    public void testIsOptKey() {
        System.out.println("isOptKey");
        final boolean Y = true, N = false;
        ArgsWrapper w = new ArgsWrapper();
        //          0    1    2    3      4    5    6    7       8
        w.setArgs(        "cmd -go -path north -speed 10 -mode -verbose true".split(" "));
        boolean[] exps =  { N,  Y,   Y,   N,     Y,    N,  Y,     Y,      N};
        for (int i = 0; i < exps.length; i++) {
            boolean exp = exps[i];
            assertEquals(exp, w.isOptKey(i));
        }
    }

    /**
     * Test of optIndex method, of class ArgsWrapperImpl.
     */
    @Test
    public void testOptIndex() {
        System.out.println("optIndex");

        ArgsWrapper w = new ArgsWrapper();
        //          0    1    2    3      4    5    6    7       8
        w.setArgs("cmd -go -path north -speed 10 -mode -verbose true".split(" "));

        assertEquals(-1, w.optIndex("-x"));
        assertEquals(-1, w.optIndex("cmd"));
        assertEquals(-1, w.optIndex("true"));

        assertEquals(1, w.optIndex("-go"));
        assertEquals(2, w.optIndex("-path"));
        assertEquals(4, w.optIndex("-speed"));
        assertEquals(6, w.optIndex("-mode"));
        assertEquals(7, w.optIndex("-verbose","-v"));
    }

    /**
     * Test of hasOpt method, of class ArgsWrapperImpl.
     */
    @Test
    public void testHasOpt() {
        System.out.println("hasOpt");
        ArgsWrapper w = new ArgsWrapper();
        //          0    1    2    3      4    5    6    7       8
        w.setArgs("cmd -go -path north -speed 10 -mode -verbose true".split(" "));

        assertEquals(false, w.hasOpt("-x"));
        assertEquals(false, w.hasOpt("cmd"));
        assertEquals(false, w.hasOpt("true"));

        assertEquals(true, w.hasOpt("-go"));
        assertEquals(true, w.hasOpt("-path"));
        assertEquals(true, w.hasOpt("-speed"));
        assertEquals(true, w.hasOpt("-mode"));
        assertEquals(true, w.hasOpt("-verbose","-v"));
    }

    /**
     * Test of optValue method, of class ArgsWrapperImpl.
     */
    @Test
    public void testOptValue() {
        System.out.println("optValue");
        ArgsWrapper w = new ArgsWrapper();
        //          0    1    2    3      4    5    6    7       8
        w.setArgs("cmd -go -path north -speed 10 -mode -verbose true".split(" "));

        assertEquals(null, w.optValue("-x"));
        assertEquals(null, w.optValue("cmd"));
        assertEquals(null, w.optValue("true"));

        assertEquals(null,    w.optValue("-go"));
        assertEquals("north", w.optValue("-path"));
        assertEquals("10",    w.optValue("-speed"));
        assertEquals(null,    w.optValue("-mode"));
        assertEquals("true",  w.optValue("-verbose","-v"));
    }

    /**
     * Test of optValueOrDef method, of class ArgsWrapperImpl.
     */
    @Test
    public void testOptValueOrDef() {
        System.out.println("optValueOrDef");
        String def = "DEF";

        ArgsWrapper w = new ArgsWrapper();
        w.setArgs("cmd -k0 0 -k1 1 -k2 2 -k3 3 -k4 -k5".split(" "));

        for (int i = 0; i < 3; i++) {
            assertEquals("#i"+i,  String.valueOf(i), w.optValueOrDef(def, "-k"+i));
        }
        assertEquals(def, w.optValueOrDef(def, "-k4"));
        assertEquals(def, w.optValueOrDef(def, "-k5"));
        assertEquals(def, w.optValueOrDef(def, "cmd"));
        assertEquals(null, w.optValue(def, "cmd"));
        assertEquals(def,  w.optValueOrDef(def, "-k99"));
        assertEquals(null, w.optValue(def, "-k99"));
    }

    /**
     * Test of optValueLongOrDef method, of class ArgsWrapperImpl.
     */
    @Test
    public void testOptValueLongOrDef() {
        System.out.println("optValueLongOrDef");

        ArgsWrapper w = new ArgsWrapper();
        w.setArgs("cmd -k0 0 -k1 1 -k2 2 -k3 3 -k4 -k5 -k6 not-digit".split(" "));
        long def = 777;
        for (int i = 0; i < 3; i++) {
            assertEquals("#i"+i,  i, w.optValueLongOrDef(def, "-k"+i));
        }
        assertEquals(def, w.optValueLongOrDef(def, "-k4"));
        assertEquals(def, w.optValueLongOrDef(def, "-k5"));
        assertEquals(def, w.optValueLongOrDef(def, "-k6"));
        assertEquals(def, w.optValueLongOrDef(def, "zzz"));

    }

    /**
     * Test of optValueDoubleOrDef method, of class ArgsWrapperImpl.
     */
    @Test
    public void testOptValueDoubleOrDef() {
        System.out.println("optValueDoubleOrDef");
        ArgsWrapper w = new ArgsWrapper();
        w.setArgs("cmd -k0 0.1 -k1 1.2 -k2 2.3 -k3 3.4 -k4 -k5 -k6 not-digit".split(" "));
        double def = 7.77, delta = 0.001;
        double val = 0.1D;
        for (int i = 0; i < 3; i++) {
            assertEquals("#i"+i,  val, w.optValueDoubleOrDef(def, "-k"+i), delta );
            val += 1.1D;
        }
        assertEquals(def,  w.optValueDoubleOrDef(def, "-k5"), delta );
        assertEquals(def,  w.optValueDoubleOrDef(def, "-k6"), delta );
        assertEquals(def,  w.optValueDoubleOrDef(def, "error"), delta );
        assertEquals(def,  w.optValueDoubleOrDef(def, "cmd"), delta );
    }

}
