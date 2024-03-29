package org.swarg.cmds;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import org.junit.*;
import org.junit.Test;
import org.swarg.cmds.CmdUtil;
import static org.junit.Assert.*;
import static org.swarg.cmds.CmdUtil.*;

/**
 * 08-01-21
 * @author Swarg
 */
public class CmdUtilTest
{
    private Random rnd;

    public CmdUtilTest() {
        rnd = new Random();
    }



    /**
     * Test of argsCount method, of class CmdUtils.
     */
    @Test
    public void testArgsCount() {
        System.out.println("argsCount");
        int expResult = 4;
        String[] args = new String[expResult];
        int result = CmdUtil.argsCount(args);
        assertEquals(expResult, result);
    }

    /**
     * Test of arg method, of class CmdUtils.
     */
    @Test
    public void testArg() {
        System.out.println("arg");
        String[] args = {"1","2","3"};
        int i = 0;
        assertEquals("1", CmdUtil.arg(args, i++));
        assertEquals("2", CmdUtil.arg(args, i++));
        assertEquals("3", CmdUtil.arg(args, i++));
        assertEquals(null,CmdUtil.arg(args, i++));
    }

    /**
     * Test of argI method, of class CmdUtils.
     */
    @Test
    public void testArgI() {
        System.out.println("argI");
        String[] args = {"123", "-777", "-7.89"};
        int i = 0;
        assertEquals(123,  CmdUtil.argI(args, i++));
        assertEquals(-777, CmdUtil.argI(args, i++));
        assertEquals(0,    CmdUtil.argI(args, i++));
        assertEquals(0,    CmdUtil.argI(args, i++));
    }

    /**
     * Test of argL method, of class CmdUtils.
     */
    @Test
    public void testArgL() {
        System.out.println("argL");
        String[] args = {"123","-777", "-7.89"};
        int i = 0;
        assertEquals(123L,  CmdUtil.argL(args, i++, 0));
        assertEquals(-777L, CmdUtil.argL(args, i++, 0));
        assertEquals(0L,    CmdUtil.argL(args, i++, 0));
        assertEquals(0L,    CmdUtil.argL(args, i++, 0));
    }

    /**
     * Test of argEqual method, of class CmdUtils.
     */
    @Test
    public void testArgEqual() {
        System.out.println("argEqual");
        String[] args = {"123","-777", "-7.89"};

        assertEquals(true, CmdUtil.argEqual(args, 0, "123"));
        assertEquals(true, CmdUtil.argEqual(args, 1, "-777"));
        assertEquals(false, CmdUtil.argEqual(args, 3, "x"));
    }

    /**
     * Test of argIndexEquals method, of class CmdUtils.
     */
    @Test
    public void testArgIndexEquals() {
        System.out.println("argIndexEquals");
        String[] args = {"123","-777", "-7.89"};

        assertEquals(0, CmdUtil.IndexOfAnyEqualsArg(args, 2, new String[]{"-7.89","X"}));
    }

    /**
     * Test of hasOpt method, of class CmdUtils.
     */
    @Test
    public void testHasOpt() {
        System.out.println("hasOpt");
        String[] args = {"-here", "-block", "-no", "-1"};
        assertEquals(false, hasOpt(args, "-any"));
        assertEquals(true,  hasOpt(args, "-here"));
        assertEquals(true,  hasOpt(args, "-block"));
        assertEquals(true,  hasOpt(args, "-no"));
        assertEquals(false, hasOpt(args, "-XYZ"));
        //отрицательные числа не считаються ключами
        assertEquals(false, hasOpt(args, "-1"));

        assertEquals(false, hasOpt("cmd do -1abc".split(" "), "-1abc"));
        assertEquals(true,  hasOpt("cmd do -abc1".split(" "),  "-abc1"));

        assertEquals(false, hasOpt("cmd do -".split(" "),     "-"));
        assertEquals(true,  hasOpt("cmd do -z *".split(" "),  "-z"));
        assertEquals(false, hasOpt("cmd do -1 x-".split(" "), "-1"));
        assertEquals(false, hasOpt("cmd do -1 x-".split(" "), "x-"));
        //??
        assertEquals(true, hasOpt("cmd do --".split(" "),     "--"));

    }

    /**
     * Test of isCmd method, of class CmdUtils.
     */
    @Test
    public void testIsCmd_3args() {
        System.out.println("isCmd");

        assertEquals(false, CmdUtil.isCmd("input", "output", "o"));
        assertEquals(true, CmdUtil.isCmd("input", "input", "i"));
        assertEquals(true, CmdUtil.isCmd("i", "intput", "i"));
    }

    /**
     * Test of isCmd method, of class CmdUtils.
     */
    @Test
    public void testIsCmd_String_String() {
        System.out.println("isCmd");
        assertEquals(false, CmdUtil.isCmd("input", "output"));
        assertEquals(true, CmdUtil.isCmd("input", "input"));
        assertEquals(true, CmdUtil.isCmd("i", "i"));
    }

    @Test
    public void test_isIntNum() {
        System.out.println("isIntNum");

        int cap = 12;
        String[] l = new String[cap];
        boolean[] p = new boolean[cap];
        boolean[] e = new boolean[cap];
        int n = 0;
        final boolean N = false;
        final boolean Y = true;
        /*00*/l[n] = "12345"; p[n] = N; e[n++] = Y;
        /*01*/l[n] = "-2345"; p[n] = N; e[n++] = Y;
        /*02*/l[n] = "-2345"; p[n] = Y; e[n++] = N;
        /*03*/l[n] = "1.245"; p[n] = N; e[n++] = N;
        /*04*/l[n] = "00000"; p[n] = N; e[n++] = Y;
        /*05*/l[n] = "0000a"; p[n] = N; e[n++] = N;
        /*06*/l[n] = "0000."; p[n] = N; e[n++] = N;
        /*07*/l[n] = "1";     p[n] = N; e[n++] = Y;
        /*08*/l[n] = "-1";    p[n] = N; e[n++] = Y;
        /*09*/l[n] = "--1";   p[n] = N; e[n++] = N;
        /*10*/l[n] = "-1A";   p[n] = N; e[n++] = N;

        for (int i = 0; i < n; i++) {
            String line = l[i];
            boolean exp = e[i];
            boolean positiveOnly = p[i];
            assertEquals("line"+i, exp, isIntNum(line, positiveOnly));
        }
    }
    /**
     * Test of isDouble method, of class CmdUtils.
     */
    @Test
    public void testIsDouble() {
        System.out.println("isDouble");
        int cap = 10;
        String[] l = new String[cap];
        boolean[] e = new boolean[cap];
        int n = 0;
        final boolean N = false;
        final boolean Y = true;
        /*00*/l[n] = "12345";    e[n++] = Y;
        /*01*/l[n] = "1234.5";   e[n++] = Y;
        /*02*/l[n] = "-1234.5";  e[n++] = Y;
        /*03*/l[n] = "--1234.5"; e[n++] = N;
        /*04*/l[n] = "1234.5-";  e[n++] = N;

        for (int i = 0; i < n; i++) {
            String line = l[i];
            boolean exp = e[i];
            assertEquals("line"+i, exp, isDouble(line));
        }
    }

    /**
     * Test of subArgsFrom method, of class CmdUtils.
     */
    @Test
    public void testSubArgsFrom() {
        System.out.println("subArgsFrom");
        String[] args = {"1a","2b","33","444"};
        int i = 1;
        String[] expResult = {"2b","33","444"};
        String[] result = CmdUtil.subArgsFrom(args, i);
        assertArrayEquals(expResult, result);
    }
    @Test
    public void test_SubArgsFrom() {
        System.out.println("SubArgsFrom");

        int cap = 10;
        String[] l = new String[cap];
        int[] ai = new int[cap];
        String[][] e = new String[cap][];
        int n = 0;
                    // 0   1     2   3  4
        /*00*/l[n] = null;                  ai[n] = 0;  e[n++] = null;
        /*00*/l[n] = null;                  ai[n] =-1;  e[n++] = null;
        /*00*/l[n] = "cmd subcmd a1 a2 a3"; ai[n] =-2;  e[n++] = "cmd subcmd a1 a2 a3".split(" ");
        /*00*/l[n] = "cmd subcmd a1 a2 a3"; ai[n] = 2;  e[n++] = "a1 a2 a3".split(" ");
        /*00*/l[n] = "cmd subcmd a1 a2 a3"; ai[n] = 4;  e[n++] = new String[]{"a3"};
        /*00*/l[n] = "cmd subcmd a1 a2 a3"; ai[n] =21;  e[n++] = new String[0];

        for (int i = 0; i < n; i++) {
            String line = l[i];
            String[] args = line == null ? null : line.split(" ");
            String[] res = CmdUtil.subArgsFrom(args, ai[i]);
            String[] exp = e[i];
            assertArrayEquals("line#" + i, exp, res);
        }
    }


    @Test
    public void test_optIndex() {
        System.out.println("optIndex");

        int cap = 10;
        String[] l = new String[cap];
        String[] k = new String[cap];
        int[] e = new int[cap];
        int n = 0;
                    // 0   1    2      3
        /*00*/l[n] = "cmd -opt value -opt2"; k[n] = "-opt -o";  e[n++] = 1;
        /*01*/l[n] = "cmd -opt value -opt2"; k[n] = "-o";       e[n++] = -1;
        /*02*/l[n] = "cmd -opt value -opt2"; k[n] = "cmd";      e[n++] = -1;
        /*03*/l[n] = "cmd -opt value -opt2"; k[n] = "value";    e[n++] = -1;
        /*04*/l[n] = "cmd -opt value -opt2"; k[n] = "-opt2";    e[n++] =  3;
        /*05*/l[n] = "cmd -opt value -opt2"; k[n] = "-opt3";    e[n++] = -1;
        /*06*/l[n] = "cmd -1 value -opt2";   k[n] = "-1";       e[n++] = -1; //не является ключем
        /*07*/l[n] = "cmd -1X value -opt2";  k[n] = "-1X";      e[n++] = -1; //не является ключем
        /*08*/l[n] = "cmd -X1 value -opt2";  k[n] = "-X1";      e[n++] = 1;

        for (int i = 0; i < n; i++) {
            String line = l[i];
            String[] args = line.split(" ");
            String[] optNames = k[i].split(" ");
            int res = CmdUtil.optIndex(args, optNames);
            int exp = e[i];
            assertEquals("line:#"+i, exp, res);
        }
    }

    @Test
    public void test_optValue() {
        System.out.println("optValue");

        int cap = 10;
        String[] l = new String[cap];
        String[] k = new String[cap];
        String[] e = new String[cap];
        String def = "def";
        int n = 0;
        /*00*/l[n] = "cmd -opt value -opt2"; k[n] = "-opt";  e[n++] = "value";
        /*01*/l[n] = "cmd -opt -x -opt2";    k[n] = "-opt";  e[n++] = "def";
        /*02*/l[n] = "cmd -opt -x -opt2";    k[n] = "-Z";    e[n++] = null;
        /*03*/l[n] = "cmd -opt -opt2";       k[n] = "-opt2"; e[n++] = def;
        /*04*/l[n] = "cmd -opt -opt21";      k[n] = "-opt2"; e[n++] = null;
        /*05*/l[n] = "cmd -opt -opt2 w";     k[n] = "-opt2"; e[n++] = "w";
        /*06*/l[n] = "cmd -opt opt2 w";      k[n] = "opt2";  e[n++] = null;
        /*07*/l[n] = "cmd";                  k[n] = "opt";   e[n++] = null;
        /*08*/l[n] = "cmd -fc name";         k[n] = "-find-class -fc";  e[n++] = "name";
        /*09*/l[n] = "cmd -find-class name"; k[n] = "-find-class -fc";  e[n++] = "name";

        for (int i = 0; i < n; i++) {
            String line = l[i];
            String[] args = line.split(" ");
            String[] optNames = k[i].split(" ");
            String res = CmdUtil.optValue(args, def, optNames);
            String exp = e[i];
            assertEquals("line:#"+i, exp, res);
        }
    }

    @Test
    public void test_optValueLongOrDef() {
        System.out.println("optValueLongOrDef");

        int cap = 10;
        String[] l = new String[cap];
        String[] k = new String[cap];
        long[] e = new long[cap];
        int n = 0;
        final long def = 0;
        l[n] = "cmd -key 777";   k[n] = "-key -k";  e[n++] = 777;
        l[n] = "cmd -key -777";  k[n] = "-key -k";  e[n++] = -777;
        l[n] = "cmd -key -7.77"; k[n] = "-key -k";  e[n++] = def;
        l[n] = "cmd -key -x";    k[n] = "-key -k";  e[n++] = def;
        l[n] = "cmd -key";       k[n] = "-key -k";  e[n++] = def;
        l[n] = "cmd key 123";    k[n] = "-key -k";  e[n++] = def;
        l[n] = "cmd";            k[n] = "-key -k";  e[n++] = def;

        l[n] = "cmd -a -b -c -k 7";  k[n]="-key -k";  e[n++] = 7;
        l[n] = "cmd -a -b -c -k 7z"; k[n]="-key -k";  e[n++] = def;

        for (int i = 0; i < n; i++) {
            String line = l[i];
            String[] args = line.split(" ");
            String[] keyNames = k[i].split(" ");
            long res = CmdUtil.optValueLongOrDef(args, def, keyNames);
            long exp = e[i];
            assertEquals("#"+i, exp, res);
        }
    }

    @Test
    public void test_optValueDoubleOrDef() {
        System.out.println("optValueDoubleOrDef");

        int cap = 10;
        String[] l = new String[cap];
        String[] k = new String[cap];
        double[] e = new double[cap];
        int n = 0;
        final double def = 0.0D;
        l[n] = "cmd -key 777";   k[n] = "-key -k";  e[n++] = 777D;
        l[n] = "cmd -key -777";  k[n] = "-key -k";  e[n++] = -777D;
        l[n] = "cmd -key -7.77"; k[n] = "-key -k";  e[n++] = -7.77D;
        l[n] = "cmd -key -x";    k[n] = "-key -k";  e[n++] = def;
        l[n] = "cmd -key";       k[n] = "-key -k";  e[n++] = def;
        l[n] = "cmd key 123";    k[n] = "-key -k";  e[n++] = def;
        l[n] = "cmd";            k[n] = "-key -k";  e[n++] = def;

        l[n] = "cmd -a -b -c -k 0.7";  k[n]="-key -k";  e[n++] = 0.7D;
        l[n] = "cmd -a -b -c -k 7z";   k[n]="-key -k";  e[n++] = def;

        for (int i = 0; i < n; i++) {
            String line = l[i];
            String[] args = line.split(" ");
            String[] keyNames = k[i].split(" ");
            double res = CmdUtil.optValueDoubleOrDef(args, def, keyNames);
            double exp = e[i];
            assertEquals("#"+i, exp, res, 0.0001);
        }
    }
    @Test
    public void test_MathCeil() {
        System.out.println("MathCeil");

        int cap = 10;
        int[] l = new int[cap];
        int[] e = new int[cap];
        int n = 0;
        /*00*/l[n] =   0; e[n++] = 1;
        /*01*/l[n] =   1; e[n++] = 1;
        /*02*/l[n] =  50; e[n++] = 1;
        /*03*/l[n] =  99; e[n++] = 1;
        /*04*/l[n] = 100; e[n++] = 2;
        /*05*/l[n] = 101; e[n++] = 2;
        /*06*/l[n] = 199; e[n++] = 2;

        for (int i = 0; i < n; i++) {
            int v = l[i];
            int res = (int)Math.ceil((v+1) / 100.0D);
            int exp = e[i];
            assertEquals("#"+i, exp, res);
        }
    }

    @Test
    public void test_getMatchedForInput() {
        System.out.println("getMatchedForInput");

        int cap = 20;
        Object[][] a = new Object[cap][];
        String[] r = new String[cap];
        Object[] e = new Object[cap];
        //int[] e = new int[cap];
        int n = 0;
        Object[] a1 =  "status register unregister list set-active report clear".split(" ");
        //Object[]{"EventBean","TrWrap","TraceEntry", "ChunkEventHook"};
        /*00*/a[n] = a1; r[n] = "s";      e[n++] = a1[0];
        /*01*/a[n] = a1; r[n] = "status"; e[n++] = a1[0];
        /*02*/a[n] = a1; r[n] = "r";      e[n++] = a1[1];
        /*03*/a[n] = a1; r[n] = "u";      e[n++] = a1[2];
        /*04*/a[n] = a1; r[n] = "l";      e[n++] = a1[3];
        /*05*/a[n] = a1; r[n] = "sa";     e[n++] = a1[4];
        /*06*/a[n] = a1; r[n] = "r";      e[n++] = a1[1];//!!! conflict register & report
        /*07*/a[n] = a1; r[n] = "c";      e[n++] = a1[6];

        Object[] a2 = "CheckSpawn PotentialSpawns EnteringChunk SpecialSpawn EntityJoinWorldEvent LivingPackSizeEvent AllowDespawn".split(" ");
        /*08*/a[n] = a2; r[n] = "cs";      e[n++] = a2[0];
        /*09*/a[n] = a2; r[n] = "ps";      e[n++] = a2[1];
        /*10*/a[n] = a2; r[n] = "ec";      e[n++] = a2[2];
        /*11*/a[n] = a2; r[n] = "ss";      e[n++] = a2[3];
        /*12*/a[n] = a2; r[n] = "ejwe";    e[n++] = a2[4];
        /*13*/a[n] = a2; r[n] = "lpse";    e[n++] = a2[5];
        /*14*/a[n] = a2; r[n] = "ad";      e[n++] = a2[6];

        for (int i = 0; i < n; i++) {
            String input = r[i];
            Object[] vars = a[i];
            Object res = getMatchedForInput(vars, input);
            Object exp = e[i];
            assertEquals("#"+i, true, exp==res);
        }
    }
    @Test
    public void test_isShortHandMatched() {
        System.out.println("isShortHandMatched");

        int cap = 30;
        String[] n = new String[cap];
        String[] s = new String[cap];
        boolean[] e = new boolean[cap];
        int k = 0;
        final boolean N = false;
        final boolean Y = true;

        /*00*/n[k] = "EntityJoinWorldEvent"; s[k] = "ejwe"; e[k++] = Y;
        /*01*/n[k] = "EntityJoinWorldEvent"; s[k] = "EJWE"; e[k++] = Y;
        /*02*/n[k] = "EntityJoinWorldEvent"; s[k] = "ejw";  e[k++] = N;
        /*03*/n[k] = "EntityJoinWorldEvent"; s[k] = "Ejw";  e[k++] = N;
        /*04*/n[k] = "EntityJoinWorldEvent"; s[k] = "ejwE"; e[k++] = Y;
        /*05*/n[k] = "entity-join-world";    s[k] = "ejw";  e[k++] = Y;
        /*06*/n[k] = "LivingPackSizeEvent";  s[k] = "lpse"; e[k++] = Y;
        /*07*/n[k] = "ABCde";                s[k] = "abc";  e[k++] = Y;
        /*08*/n[k] = "Aa-Bc-Def-G";          s[k] = "abdg"; e[k++] = Y;
        /*09*/n[k] = "A";                    s[k] = "a";    e[k++] = Y;
        /*10*/n[k] = "Entity";               s[k] = "e";    e[k++] = Y;
        /*11*/n[k] = "EntityWorldObj";       s[k] = "ewo";  e[k++] = Y;
        /*12*/n[k] = "EntityWorldObj";       s[k] = "wo";   e[k++] = N;
        /*13*/n[k] = "-entity-world-Obj";    s[k] = "ewo";  e[k++] = N;
        /*14*/n[k] = "-entity-world-Obj";    s[k] = "-ewo"; e[k++] = Y;
        /*15*/n[k] = "-All";                 s[k] = "a";    e[k++] = N;
        /*16*/n[k] = "-All";                 s[k] = "-a";   e[k++] = Y;
        /*17*/n[k] = "-All-Things";          s[k] = "-a";   e[k++] = N;
        /*18*/n[k] = "-All-Things";          s[k] = "-at";  e[k++] = Y;
        /*19*/n[k] = "--A";                  s[k] = "--a";  e[k++] = Y;
        /*20*/n[k] = "--AliveEntity";        s[k] = "--ae"; e[k++] = Y;
        /*21*/n[k] = "--";                   s[k] = "--";   e[k++] = Y;
        /*22*/n[k] = null;                   s[k] = null;   e[k++] = N;
        /*23*/n[k] = "";                     s[k] = "";     e[k++] = N;
        /*24*/n[k] = "-";                    s[k] = "-";    e[k++] = Y;
        /*25*/n[k] = "abc";                  s[k] ="AcoBaCo";e[k++] = N;
        /*26*/n[k] = "AcoBaCo";              s[k] = "abc"   ;e[k++] = Y;
        /*27*/n[k] = "global-world-event";   s[k] = "gwe"   ;e[k++] = Y;
        /*28*/n[k] = "word";                 s[k] = "w"     ;e[k++] = Y;
        /*29*/n[k] = "server";               s[k] = "s"     ;e[k++] = Y;

        for (int i = 0; i < k; i++) {
            String name = n[i];
            String shorthand = s[i];
            assertEquals("#"+i+" "+ name, e[i], CmdUtil.isShortHandMatched(name, shorthand));
        }
    }
    @Test
    public void test_getCharCount() {
        System.out.println("getCharCount");

        int cap = 10;
        String[] l = new String[cap];
        char[] r = new char[cap];
        int[] e = new int[cap];
        int n = 0;
        l[n] = "a123a421aa23a4a2a"; r[n] = 'a';  e[n++] = 7;
        l[n] = "aaa"; r[n] = 'a';  e[n++] = 3;
        l[n] = "ccc"; r[n] = 'a';  e[n++] = 0;
        l[n] = "";    r[n] = 'a';  e[n++] = 0;
        l[n] = null;  r[n] = 'a';  e[n++] = 0;

        for (int i = 0; i < n; i++) {
            String line = l[i];
            int res = getCharCount(line, r[i]);
            int exp = e[i];
            assertEquals("#"+i, exp, res);
        }
    }

    @Test
    public void test_parseAsInt() {
        System.out.println("parseAsInt");
        String s0 = "12:34:56-78:91:a0";
                   //012345678902345678
        assertEquals(12, CmdUtil.parseAsInt2(s0, 0, -1));
        assertEquals( 2, CmdUtil.parseAsInt2(s0, 1, -1));//"2:"
        assertEquals(34, CmdUtil.parseAsInt2(s0, 3, -1));
        assertEquals(56, CmdUtil.parseAsInt2(s0, 6, -1));
        assertEquals(78, CmdUtil.parseAsInt2(s0, 9, -1));
        assertEquals(91, CmdUtil.parseAsInt2(s0, 12, -1));
        assertEquals(-1, CmdUtil.parseAsInt2(s0, 16, -1));
        
        int i = 0;
        assertTrue(isDigit(s0, i++));
        assertTrue(isDigit(s0, i++));
        assertFalse(isDigit(s0, i++));
        assertTrue(isDigit(s0, i++));
        assertTrue(isDigit(s0, i++));
        assertFalse(isDigit(s0, i++));
        assertTrue(isDigit(s0, i++));
        assertTrue(isDigit(s0, i++));
    }

    /*Первые шаги*/
    //@Test
    //public void test_parseTimeDateVariants() {
    //    System.out.println("parseTimeDateVariants");
    //
    //    int cap = 20;
    //    String[] l = new String[cap];
    //    String[] e = new String[cap];
    //    int n = 0;
    //    /*00*/l[n] = "12:23:34 19-10-21";  e[n++] = "12:23:34 19-10-21";
    //    /*01*/l[n] = "12:23:34  19-10-21"; e[n++] = "12:23:34 19-10-21";
    //    /*02*/l[n] = "12  19-10-21";       e[n++] = "12:0:0 19-10-21";
    //    /*03*/l[n] = "12:23  19-10-21";    e[n++] = "12:23:0 19-10-21";
    //    /*04*/l[n] = "12:23:34  19-10";    e[n++] = "12:23:34 19-10-0";
    //    /*05*/l[n] = "12:23:34  19";       e[n++] = "12:23:34 19-0-0";
    //    /*06*/l[n] = "12:23:34";           e[n++] = "12:23:34 0-0-0";
    //    /*07*/l[n] = "12:23";              e[n++] = "12:23:0 0-0-0";
    //    /*07*/l[n] = "12:";                e[n++] = "12:0:0 0-0-0";
    //    /*07*/l[n] = "12";                 e[n++] = "12:0:0 0-0-0";
    //    /*07*/l[n] = "24";                 e[n++] = "24:0:0 0-0-0";
    //    /*07*/l[n] = "-12";                e[n++] = "0:0:0 12-0-0";
    //    /*07*/l[n] = "-12-09-21";          e[n++] = "0:0:0 12-9-21";
    //
    //    for (int i = 0; i < n; i++) {
    //        String line = l[i];
    //        String exp = e[i];
    //        String res = CmdUtil.argTimeMillis(l, i, 0);
    //        if (!exp.equals(res)) {
    //            System.out.println("fail line:"+i);
    //            System.out.println("Exp:"+exp);
    //            System.out.println("Res:"+res);
    //            fail("line# " + i);
    //        }
    //    }
    //}

    @Test
    public void test_parseTDVariants() {
        System.out.println("parseTDVariants");
        String[] a = new String[]{
            /*0*/"12:23:34 19-10-21",
            /*1*/"12:23:34",     //дату подставит текущую а время указанное
            /*2*/"1634567800000", //timeMillis
            /*3*/"25", //распознает как миллис
            /*4*/"23", //распознает как время - только часыtimeMillis
            /*5*/"-19-10-21", //время полночь!
            /*6*/"0",  //самое начало эпохи
            /*7*/"00", //полночь относительно текущей даты

        };
        long res = CmdUtil.argTimeMillis(a, 0, -1L);
        long exp = 1634635414000L;
        assertEquals(exp, res);
        //String look = Instant.ofEpochMilli(res).atZone(ZoneId.systemDefault()).toString();//2021-10-19T12:23:34+03:00
        //assertTrue(look.startsWith("2021-10-19T12:23:34"));
        //System.out.println(res+"\n"+look);

        res = CmdUtil.argTimeMillis(a, 1, -1L);
        String look = Instant.ofEpochMilli(res).atZone(ZoneId.systemDefault()).toString();//2021-10-19T12:23:34+03:00
        assertTrue("EqTime", look.contains(a[1]));//2021-12-02T12:23:34
        //System.out.println(look);

        //просто парсить как лонг
        assertEquals(1634567800000L/*a[2]*/, CmdUtil.argTimeMillis(a, 2, -1L));
        assertEquals(25, CmdUtil.argTimeMillis(a, 3, -1L));

        //это уже время полноценное заменит только часы остальное будет как у текущего времени
        assertNotEquals(23, CmdUtil.argTimeMillis(a, 4, -1L));

        exp = 1634590800000L;//2021-10-19T00:00+03:00
        //System.out.println(Instant.ofEpochMilli(exp).atZone(ZoneId.systemDefault()).toString());//2021-10-19T00:00+03:00
        assertEquals(exp, CmdUtil.argTimeMillis(a, 5, -1L));

        // "0" - распарсит как начало эпохи
        assertEquals(0, CmdUtil.argTimeMillis(a, 6, -1L));
        // "00" - как указание только часов относительно текущего времени.
        assertTrue(CmdUtil.argTimeMillis(a, 7, -1L) > 0);
    }

    /*Новая механика - возможность указывать день одним числом
    04-12 vs 4-12*/
    @Test
    public void test_parseTimeDateVariantsCase001() {
        System.out.println("parseTimeDateVariants");
        String s = "22-30--4-12";
        LocalDateTime res = CmdUtil.parseTimeDateVariants(s, 0);
        assertEquals(22, res.getHour());
        assertEquals(30, res.getMinute());
        assertEquals(4, res.getDayOfMonth());
        assertEquals(12, res.getMonthValue());

        String s2 = "-8-11";
        LocalDateTime res2 = CmdUtil.parseTimeDateVariants(s2, 0);
        assertEquals(0, res2.getHour());
        assertEquals(0, res2.getMinute());
        assertEquals(8, res2.getDayOfMonth());
        assertEquals(11, res2.getMonthValue());
        //-8-9 - не будет работать нужно указывать -8-09

        LocalDateTime now = LocalDateTime.now();

        //месяц обязательно через два числа, либо подставит текущий месяц
        String s3 = "-2-1";
        LocalDateTime res3 = CmdUtil.parseTimeDateVariants(s3, 0);
        assertEquals(0, res3.getHour());
        assertEquals(0, res3.getMinute());
        assertEquals(2, res3.getDayOfMonth());
        assertEquals(now.getMonthValue(), res3.getMonthValue());
    }

    @Test
    public void test_getClassNameFrom() {
        System.out.println("getClassNameFrom");
        String in = "Dev/2021/cmds4j/src/main/java/org/swarg/cmds/CmdManager.java";
        String exp = "org.swarg.cmds.CmdManager";
        assertEquals(exp, getClassNameFrom(in));
        assertEquals(exp, getClassNameFrom(exp));
        assertEquals(exp, getClassNameFrom("org/swarg/cmds/CmdManager"));
        assertEquals(exp, getClassNameFrom("org\\swarg\\cmds\\CmdManager"));
        assertEquals(exp, getClassNameFrom("src/main/java/org/swarg/cmds/CmdManager"));
    }
}
