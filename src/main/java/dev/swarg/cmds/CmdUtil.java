package dev.swarg.cmds;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * 08-01-21
 * @author Swarg
 */
public class CmdUtil {
    
    public static final String DEFAULT_EMPTY_STR_ARG = "";

    private static final Object EMPTY_OBJECT = new Object() {
        @Override public String toString() {
            return DEFAULT_EMPTY_STR_ARG;
        }
    };

    /**
     * Только для чтения! если лист null|empty - выдаст пустой
     * @param list
     * @return
     */
    public static List safe(List list) {
        return (list == null) ? Collections.EMPTY_LIST : list;
    }
    
    public static Class safe(Class clazz) {
        return (clazz == null) ? Class.class : clazz;
    }
    public static Map safe(Map map) {
        return (map == null) ? Collections.EMPTY_MAP : map;
    }

    public static Object safeO(Object obj) {
        return (obj == null) ? EMPTY_OBJECT : obj;
    }
    
    public static Boolean safeBool(Object obj) {
        return (obj == null) ? false
                : obj instanceof Boolean ? (Boolean) obj : false;
    }

    public static Boolean safe(Boolean bool) {
        return (bool == null) ? Boolean.FALSE : bool;
    }
    public static String[] safe(String[] args) {
        return (args == null) ? EMPTY_ASTR : args;
    }
    
    public static final String[]  EMPTY_ASTR  = new String[0];
    public static final int[]  EMPTY_AINT  = new int[0];
    public static final long[] EMPTY_ALONG = new long[0];
    public static final byte[] EMPTY_ABYTE = new byte[0];
    public static final double[] EMPTY_ADOUBLE = new double[0];
    
    public static byte[] safe(byte[] bytes) {
        return (bytes == null) ? EMPTY_ABYTE : bytes;
    }
    public static int[]  safe(int[] a) {
        return (a == null) ? EMPTY_AINT : a;
    }
    public static long[] safe(long[] a) {
        return (a == null) ? EMPTY_ALONG : a;
    }
    public static double[] safe(double[] a) {
        return (a == null) ? EMPTY_ADOUBLE : a;
    }

    public static String getClassName(Object obj) {
        return (obj == null) ? "null": obj.getClass().getName();
    }
    public static String getHashCode(Object obj) {
        return (obj == null) ? "0": Integer.toHexString(obj.hashCode());
    }


    public static int argsCount (String[] args) {
        return args == null ? 0 : args.length;
    }
    
    public static String arg(String[] args, int i) {
        return args==null || i >= args.length ? null : args[i];
    }
    //только с положительными числами
    public static int argI(String[] args, int i) {
        String arg = arg(args,i);
        int val = 0;
        if (CmdUtil.isIntNum(arg, false)) {
            try {
                val = Integer.parseInt(arg);
            } catch (Exception e) {
            }
        }
        return val;
    }
    //только с положительными числами
    public static long argL(String[] args, int i, long def) {
        String arg = arg(args,i);
        long val = def;
        if (CmdUtil.isIntNum(arg, false)) {
            try {
                val = Long.parseLong(arg);
            } catch (Exception e) {
            }
        }
        return val;
    }
    /**
     * Получить double значение аргумента с индексом i либо def если такого
     * значения нет, либо оно заданно не корректно
     * Принимает только double описанные обычным способом в виде '-0.999'
     * @param args
     * @param i
     * @param def
     * @return
     */
    public static double argD(String[] args, int i, double def) {
        String arg = arg(args,i);
        double val = def;
        if (isDouble(arg)) {
            try {
                val = Double.parseDouble(arg);
            } catch (Exception e) {
            }
        }
        return val;
    }

    public static boolean argEqual(String[] args, int i, String value) {
        String arg = arg(args,i);
        return (arg!=null && arg.equals(value));
    }
    
    /**
     * Найти для аргумента с индексом i из args индекс в any значение которого
     * равно значению аргумента из args в ячейке i
     * @param args
     * @param i
     * @param any
     * @return индекс в any значение которого равно args[i]
     */
    public static int IndexOfAnyEqualsArg(String[] args, int i, String... any) {
        if (args != null && args.length > 0 && any != null && any.length > 0) {
            String arg = arg(args, i);
            if (arg != null) {
                for (int j = 0; j < any.length; j++) {
                    if (arg.equalsIgnoreCase(any[j])) {
                        return j;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Найти индекс аргумента значение которого равно строке find
     * регистрозависимо
     * @param args
     * @return
     */
    public static int IndexOfArgEquals(String[] args, String find) {
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg == null && find==null || arg!=null && arg.equals(find)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public static boolean stringToBoolean(String s) {
        return !isNullOrEmpty(s) && s.length() < 5 && (
                "true".equalsIgnoreCase(s) ||
                "yes".equalsIgnoreCase(s) ||
                "on".equalsIgnoreCase(s) ||
                s.charAt(0) == '1' ||
                s.charAt(0) == '+');
    }

    /**
     * Ключ должен начинаться с тире начало имени ключа не должно быть цифрой (0-9)
     * @param arg
     * @return
     */
    public static boolean isValidOptKeyName(String arg) {
        return arg != null && arg.length() > 1 && arg.charAt(0) == '-' && !isDigit(arg.charAt(1));
    }

    /**
     * В наборе аргементов присутствует опциональный параметр c указанным именем
     * @param args
     * @param optNames должен начинаться с тире(минуса) -1 не считается именем ключа
     * @return
     */
    public static int optIndex(String[] args, String... optNames) {
        int len = argsCount(args);
        if (len < 1 || optNames == null || optNames.length == 0) return -1;
        for (int i = len - 1; i >= 0; i--) {
            String arg = args[i];
            if (isValidOptKeyName(arg)) {
                //TODO -key == --key ??
                for (int j = 0; j < optNames.length; j++) {
                    if (arg.equals(optNames[j])) {
                        return i;//индекс аргумента в args!
                    }
                }
            }
        }
        return -1;
    }

    /**
     * В наборе аргументов присутствует опциональный параметр c указанным именем
     * (Ищет справа на лево)
     * @param args
     * @param name должен начинаться с тире(минуса)
     * @return
     */
    public static boolean hasOpt(String[] args, String name) {
        return optIndex(args, name) > -1;
    }

    public static String optValue(String[] args, String defForExistsKey, String...optNames) {
        int i = optIndex(args, optNames);
        if (i > -1) {
            return (i+1 >= args.length || CmdUtil.isValidOptKeyName( args[i+1])) ? defForExistsKey : args[i+1];
        }
        return null;
    }

    /**
     * Если ключ задан но значения у него нет, либо если ключ вообще не задан
     * вернуть - def
     * @param args
     * @param def
     * @param optNames
     * @return
     */
    public static String optValueOrDef(String[] args, String def, String...optNames) {
        int i = optIndex(args, optNames);
        if (i > -1) {
            return (i+1 >= args.length || CmdUtil.isValidOptKeyName( args[i+1] )) || isNullOrEmpty( args[i+1] )
                    ? def : args[i+1];
        }
        return def;
    }
    /**
     * Получить целочисленное числовое значение ключа либо def значение
     * @param args
     * @param def если ключ не задан либо если значение ключа либо не задано либо заданно не корректно
     * @param optNames имена ключа
     * @return
     */
    public static long optValueLongOrDef(String[] args, long def, String...optNames) {
        int i = optIndex(args, optNames);
        if (i > -1 && i + 1 < args.length) {
            if ( !isValidOptKeyName(args[i+1])) {
                return CmdUtil.argL(args, i+1, def);
            }
        }
        return def;
    }

    public static double optValueDoubleOrDef(String[] args, double def, String...optNames) {
        int i = optIndex(args, optNames);
        if (i > -1 && i + 1 < args.length) {
            if ( !isValidOptKeyName(args[i+1])) {
                return CmdUtil.argD(args, i+1, def);
            }
        }
        return def;
    }
    
    /**
     * Правда если любое из значений равно эталонному
     */
    public static boolean equal(String etalon, Object v1, Object v2) {
        return etalon != null && (etalon.equals(v1) || etalon.equals(v2));
    }

    //String... нет смысла т.к. либо полное имя либо сокращенное 1 или 2
    public static boolean isCmd(String cmdName, String name1, String name2) {
        if (cmdName == null || cmdName.isEmpty()) return false;
        return name1 != null && cmdName.equalsIgnoreCase(name1) || name2!=null && cmdName.equalsIgnoreCase(name2);
    }
    
    public static boolean isCmd(String cmdName, String name) {
        return isCmd(cmdName, name, null);
    }

    //целое число
    public static boolean isIntNum(String str, boolean positiveOnly) {
        if (isNullOrEmpty(str)) return false;
        if (positiveOnly) return isDigitsOnly(str, 0);
        if (str == DEFAULT_EMPTY_STR_ARG || str.length() < 1) return false;
        char c = str.charAt(0);
        boolean first = (c == '-' || (c >= 48 && c <= 57));
        return (first && str.length()==1 || first && isDigitsOnly(str, 1) );
    }
    
    /**
     * 
     * @param str
     * @return
     */
    public static boolean isDouble(String str) {
        if (!isNullOrEmpty(str) && str != DEFAULT_EMPTY_STR_ARG) {
            char c = str.charAt(0);
            boolean hasDot = false;
            boolean hasE = false;
            if (!(c == '-' || (c >= 48 && c <= 57))) {
                return false;
            }
            int sz = str.length();
            ////считать валидным если на конце D?
            //if (sz > 2 && Character.toUpperCase(str.charAt(sz-1)) == 'D') {
            //    sz--;
            //}
            for (int i = 1; i < sz; i++) {
                c = str.charAt(i);
                if (c == '.') {
                    if (hasDot) {
                        return false;//только 1 точка может  быть в double числе
                    }
                    hasDot = true;
                    continue;
                }

                if (c == 'E') {//тут не совсем уверен как правильно они там должны располагаться
                    if (hasE) {//min double 4.9E-324
                        return false;//второе е недопустимо
                    }
                    hasE = true;
                    if (i + 1 < sz && str.charAt(i + 1) == '-') {
                        i++;
                    }
                    continue;
                }

                if (!(c >= 48 && c <= 57)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    /**
     * Получить инстанс Class по его имени
     * Поддержка примитивов
     * @param name
     * @param canLoad true- может запрашивать незагруженные в память классы
     * (false - выдавать только загруженные)
     * @return
     */
    public static Class argClass(String name, boolean canLoad) {
        Class clazz = null;
        if (!isNullOrEmpty(name)) {
            if (!name.contains(".")) {
                final String n = name;
                if (isCmd(n, "void", "V"))    return void.class;
                if (isCmd(n, "boolean", "?")) return boolean.class;
                if (isCmd(n, "byte", "?"))    return byte.class;
                if (isCmd(n, "char", "?"))    return char.class;
                if (isCmd(n, "short", "?"))   return short.class;
                if (isCmd(n, "int", "I"))     return int.class;
                if (isCmd(n, "float", "?"))   return float.class;
                if (isCmd(n, "long", "?"))    return long.class;
                if (isCmd(n, "double", "D"))  return double.class;
            }
            try {
                if (!canLoad) {
                    //вернёт класс только если тот уже загружен в память класслоадера
                    java.lang.reflect.Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class[] { String.class });
                    m.setAccessible(true);
                    ClassLoader cl = ClassLoader.getSystemClassLoader();
                    clazz = (Class) m.invoke(cl, name);
                }
                //запрос класса у класслоадера, если он ранее небыл загружен - загрузит
                else {
                    //clazz = Class.forName(name);
                    //через обращение к кэшу или к jvm
                    clazz = Class.forName(name, false, CmdUtil.class.getClassLoader());
                }

            } catch (Throwable t) {
            }
        }
        return clazz;
    }

    /**
     * Преобразовать набор классов в читаемый вывод
     * @param classes
     * @param caption
     * @param out
     * @param index добавлять порядковый номер
     */
    public static void appendClasses(Set<Class> classes, String caption, StringBuilder out, boolean index) {
        if (classes != null && out != null)
        if (!classes.isEmpty()) {
            Iterator<Class> iter = classes.iterator();
            int i = 0;

            if (!isNullOrEmpty(caption)) {
                out.append(caption).append('\n');
            }
            while (iter.hasNext()) {
                Class cl = iter.next();
                if (cl != null) {
                    if (index) {
                        out.append(i++).append(' ');
                    }
                    out.append( cl.getName() ).append('\n');
                }
            }
        }
    }

    /**
     *
     * @param obj
     * @param out
     * @return
     */
    public static StringBuilder appendObjectStatus(Object obj, StringBuilder out) {
        if (obj == null) {
            out.append("null");
        } else {
            Class cl = obj.getClass();
            out.append( cl.getSimpleName() );
            if (cl == String.class) {
                out.append(".length:").append( ((String)obj).length() );
            }else {
                int sz = -1;
                if (obj instanceof java.util.List) {
                    sz = ((java.util.List)obj).size();
                }
                else if (obj instanceof java.util.Map) {
                    sz = ((java.util.Map)obj).size();
                }
                else if (obj instanceof java.util.Set) {
                    sz = ((java.util.Set)obj).size();
                }
                if (sz>-1) {
                    out.append(".size:").append(sz);
                }
            }
        }
        return out;
    }

    /**
     *
     * @param obj
     * @param shortNamesForReflect
     * @return
     */
    private static String objectToString(Object obj, boolean shortNamesForReflect) {
        String key = null;
        if (obj != null) {
            if (obj instanceof String) {
                key = (String)obj;
            }
            else if (obj instanceof Class) {
                key = shortNamesForReflect ? ((Class)obj).getSimpleName() : ((Class)obj).getName();
            }
            else if (obj instanceof Field) {
                key = ((Field)obj).getName();
            }
            else if (obj instanceof Method) {
                key = ((Method)obj).getName();
            }
            else {
                key = String.valueOf(obj);
            }
        }
        return key;
    }

    /**
     *    0          1  2  3
     * os chunk-obj 10 10 sub cmd param  -> от 3 {sub cmd param}
     * simple pipeline
     * @param args
     * @param i
     * @return
     */
    public static String[] subArgsFrom(String[] args, int i) {
        String[] a = null;
        if (args != null) {
            int sz = args.length;
            if (i < 0) i = 0;
            int len = sz - i;
            if (len <= 0) {
                a = new String[0];
            } else {
                a = new String[len];
                for (int j = 0; j < len; j++) {
                    a[j] = args[i+j];
                }
            }
        }
        return a;
    }


    /**
     * Лист объектов в котором явно строки преобразовать в Массив строк
     * тоже самое что (List)list.toArray(String[]); но с защитой от нулов
     * @param list
     * @return
     */
    public static String[] listToStrArr(List list) {
        if (list == null || list.isEmpty()) {
            return new String[0];
        }
        String[] args = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            String val;
            if (o == null) {
                val = "";
            } else {
                if (o instanceof String) {
                    val = (String) o;
                } else {
                    val = o.toString();
                }
            }
            args[i] = val;
        }
        return args;
    }



    /*<  -----------------------------------------------------------------  > /
                                      B O X
    / <  -----------------------------------------------------------------  > */

    public static int boxSize(Object box) {
        if (box==null) return 0;
        int size = 0;
        if (box instanceof int[]) {
            size = ((int[])box).length;
        }
        else if (box instanceof long[]) {
            size = ((long[])box).length;
        }
        else if (box instanceof double[]) {
            size =  ((double[])box).length;
        }
        else if (box instanceof boolean[]) {
            size =  ((boolean[])box).length;
        }
        else if (box instanceof Object[]) {
            size =  ((Object[])box).length;
        }
        return size;
    }
    //int
    public static boolean toBox(int[] box, int i, int value) {
        if (box == null || i < 0 || box.length <= i) return false;
        return (box[i] = value)==value;
    }
    public static int unBox(int[] box, int i) {
        return (box == null || i < 0 || box.length <= i) ? 0 : box[i];
    }
    //long
    public static boolean toBox(long[] box, int i, long value) {
        if (box == null || i < 0 || box.length <= i) return false;
        return (box[i] = value)==value;
    }
    public static long unBox(long[] box, int i) {
        return (box == null || i < 0 || box.length <= i) ? 0 : box[i];
    }
    //double
    public static boolean toBox(double[] box, int i, double value) {
        if ( box == null || i < 0 || box.length <= i) return false;
        return (box[i] = value)==value;
    }
    public static double unBox(double[] box, int i) {
        return (box == null || i < 0 || box.length <= i) ? 0 : box[i];
    }
    //boolean
    public static boolean toBox(boolean[] box, int i, boolean value) {
        if ( box == null || i < 0 || box.length <= i) return false;
        return (box[i] = value) == value;
    }
    public static boolean unBox(boolean[] box, int i) {
        return (box == null || i < 0 || box.length <= i) ? false : box[i];
    }
    //object
    public static boolean toBox(Object[] box, int i, Object value) {
        if ( box == null || i < 0 || box.length <= i) return false;
        return (box[i] = value) == value;
    }
    public static Object unBox(Object[] box, int i) {
        return (box == null || i < 0 || box.length <= i) ? null : box[i];
    }

    public static boolean toBoxN(Object[] box, int i, Object... values) {
        if ( box == null || i < 0 || box.length <= i) return false;
        boolean added = false;
        for (int j = 0; j < values.length; j++) {
            Object value = values[j];
            int k = i+j;
            if (k > -1 && k < box.length) {
                box[k] = value;
                added = true;
            }
        }
        return added;
    }

    //для проверок автоматически преобразует к int[] long[] double[] Object[]
    public static Object unBox(Object box, int i) {
        if (box==null) return null;
        if (box instanceof int[]) return unBox((int[])box,i);
        if (box instanceof long[]) return unBox((long[])box,i);
        if (box instanceof double[]) return unBox((double[])box,i);
        if (box instanceof boolean[]) return unBox((boolean[])box,i);
        if (box instanceof Object[]) return unBox((Object[])box,i);
        throw new UnsupportedOperationException("This type of box not supported: "+box.getClass());
    }
    
    public static Object[] newBox() {
        return new Object[1];
    }
    
    public static Object[] newBox(int sz) {
        if (sz < 1) sz = 1;
        return new Object[sz];
    }
    /**
     * Если по указанному индексу лежит нечто не ожидаемого класса
     * - возвращать null вместо него Для исключения ClassCastException
     * @param box
     * @param i
     * @param clazz
     * @return
     */
    public static Object unBox(Object[] box, int i, Class clazz) {
        if (box == null || i < 0 || box.length <= i) {
            return null;
        }
        Object value = box[i];
        if (clazz != null) {
            if (value != null && value.getClass() != clazz) {
                value = null;
            }
        }
        return value;
    }

    public static Object unBoxNum(Object[] box, int i, Class clazz) {
        if (box == null || i < 0 || box.length <= i) {
            return null;
        }
        Object value = box[i];
        if (clazz != null) {
            if (value != null && value.getClass() != clazz) {
                value = 0;
            }
        }
        return value == null ? 0 : value;
    }

    /*<  -----------------------------------------------------------------  > /
            END                       B O X                         END
    / <  -----------------------------------------------------------------  > */


    public static void sleep(int ms) {
        try { Thread.sleep(ms); } catch(Exception e) {;}
    }


    /**
     * вместо обычного -128 -:- 127 к беззнаковому
     * unSign 0 -:- 255
     * @param b
     * @return
     */
    public static int byteToUnSign(byte b) {
        return b & 0xff;  // bytes to unsigned byte in an integer.
    }

    // --- ChunkCoordIntPair.chunkXZ2Int(x, z); ---
    /**
     * Распаковка для
     * ChunkCoordIntPair.chunkXZ2Int
     * @param longContainer
     * @return
     */
    public static int getLowInt(long longContainer) {
        return (int)longContainer;
    }
    public static int getHighInt(long longContainer) {
        return (int)(longContainer >> 32);
    }

    public static long makeLong(int low, int high) {
        return (long) low & 0xFFFFFFFFL | ((long) high & 0xFFFFFFFFL) << 32 ;
    }

    //--- commons Strings --->
    /**
     * Проверить соответствует ли для полного имени(name) заданное сокращение(shorthand)
     * AcoBaCo == abc
     * -All-Things == -at   обои должны начинаться с тире
     * WorldEvent == we
     * -WorldEvent == -we
     * -GlobalWorldEvent == -gwe  (но не равно) -gw
     * -global-world-event == -gwe
     * global-world-event == gwe
     * @param name
     * @param shorthand
     * @return
     */
    public static boolean isShortHandMatched(String name, String shorthand) {
        if (!isNullOrEmpty(name) && !isNullOrEmpty(shorthand)) {
            if (name.equalsIgnoreCase(shorthand)) return true;
            char lc = 0x00;
            int j = 0;
            boolean goodNoMoreLetters = false;
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (i == 0 && c == '-') {
                    if (shorthand.charAt(0) != '-') {
                        return false;
                    } else {
                        j++;
                        lc = '-';
                        continue;
                    }
                }
                if (i == 0 || Character.isUpperCase(c) || i > 0 && lc == '-') {
                    if (j >= shorthand.length()) {
                        return false;
                    }
                    char hc = Character.toLowerCase(shorthand.charAt(j));
                    if (Character.toLowerCase(c) != hc ) {
                        return false;
                    }
                    j++;
                    if (j>= shorthand.length()) {
                        goodNoMoreLetters = true ;//return true;
                    }
                }
                lc = c;
            }
            return goodNoMoreLetters;
        }
        return false;
    }

    /**
     * Поиск в мапе объекта ключа соответствующего вводу input
     * Возвращает сам объект ключа если либо он равен Input либо его текстовому
     * представлению соответствует сокращенный вывод
     * ChunkEvent == ce
     * @param map
     * @param input
     * @return
     */
    public static Object getMatchedForInput(Map map, String input) {
        if (map != null && !map.isEmpty() && !isNullOrEmpty(input)) {
            Iterator iter = map.keySet().iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                String key = objectToString(obj, !input.contains("."));
                    
                if ( isShortHandMatched(key, input) ) {
                    return obj;
                }                
            }
        }
        return null;
    }

    /**
     * Получить из всего массива обьектов первый соответствующий вводу обьект
     * сравнение идёт по именам или по сокращениям имён
     * например
     * ClassFieldInteract == cfi
     * new-world-event == nwe
     * -All-Things == al
     * Т.е. если текстовое представление из массива соответствует вводу - вернуть
     * обьект из массива
     * @param a
     * @param input
     * @return
     */
    public static Object getMatchedForInput(Object[] a, String input) {
        if (a != null && a.length > 0 && !isNullOrEmpty(input)) {
            for (int i = 0; i < a.length; i++) {
                String key = objectToString(a[i], !input.contains("."));
                if ( isShortHandMatched(key, input) ) {
                    return a[i];
                }
            }
        }
        return null;
    }




    public static boolean isDigit(char c) {
        return (c >= 48 && c <= 57); //0-9
    }
    /**
     * Проверить что строка содержит только числовые символы
     * @param line
     * @param start
     * @return
     */
    public static boolean isDigitsOnly(String line, int start) {
        if (line == null || line.isEmpty() || start >= line.length()) return false;
        for (int i = start; i < line.length(); i++) {
            int code = line.charAt(i);
            if (!( code >= 48 && code <= 57)) return false;
        }
        return true;
    }

    // приватные чтобы небыло конфликта с dev.swarg.commons.Strings
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
    public static int getCharCount(String s, char c) {
        if (!isNullOrEmpty(s)) {
            int i = 0;
            int sz = s.length();
            int count = 0;
            while ( i < sz) {
                i = s.indexOf(c, i);
                if (i < 0) break;
                count ++;
                i++;
            }
            return count;
        }
        return 0;
    }



}

