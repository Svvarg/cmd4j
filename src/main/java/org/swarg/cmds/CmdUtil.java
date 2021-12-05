package org.swarg.cmds;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 08-01-21
 *
 * @author Swarg
 */
public class CmdUtil
{

    public static final String DEFAULT_EMPTY_STR_ARG = "";

    public static int argsCount(String[] args) {
        return args == null ? 0 : args.length;
    }

    public static String arg(String[] args, int i) {
        return args == null || i >= args.length ? null : args[i];
    }

    //только с положительными числами
    public static int argI(String[] args, int i) {
        String arg = arg(args, i);
        int val = 0;
        if (CmdUtil.isIntNum(arg, false)) {
            try {
                val = Integer.parseInt(arg);
            }
            catch (Exception e) {
            }
        }
        return val;
    }

    public static int argI(String[] args, int i, int def) {
        String arg = arg(args, i);
        int val = def;
        if (CmdUtil.isIntNum(arg, false)) {
            try {
                val = Integer.parseInt(arg);
            }
            catch (Exception e) {
            }
        }
        return val;
    }

    //только с положительными числами
    public static long argL(String[] args, int i, long def) {
        String arg = arg(args, i);
        long val = def;
        if (CmdUtil.isIntNum(arg, false)) {
            try {
                val = Long.parseLong(arg);
            }
            catch (Exception e) {
            }
        }
        return val;
    }

    /**
     * Получить double значение аргумента с индексом i либо def если такого
     * значения нет, либо оно заданно не корректно Принимает только double
     * описанные обычным способом в виде '-0.999'
     *
     * @param args
     * @param i
     * @param def
     * @return
     */
    public static double argD(String[] args, int i, double def) {
        String arg = arg(args, i);
        double val = def;
        if (isDouble(arg)) {
            try {
                val = Double.parseDouble(arg);
            }
            catch (Exception e) {
            }
        }
        return val;
    }

    public static boolean argEqual(String[] args, int i, String value) {
        String arg = arg(args, i);
        return (arg != null && arg.equals(value));
    }

    /**
     * Найти для аргумента с индексом i из args индекс в any значение которого
     * равно значению аргумента из args в ячейке i
     *
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
     *
     * @param args
     * @return
     */
    public static int IndexOfArgEquals(String[] args, String find) {
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg == null && find == null || arg != null && arg.equals(find)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static boolean stringToBoolean(String s) {
        return !isNullOrEmpty(s) && s.length() < 5 && ("true".equalsIgnoreCase(s)
                || "yes".equalsIgnoreCase(s)
                || "on".equalsIgnoreCase(s)
                || s.charAt(0) == '1'
                || s.charAt(0) == '+');
    }

    /**
     * Ключ должен начинаться с тире начало имени ключа не должно быть цифрой
     * (0-9)
     *
     * @param arg
     * @return
     */
    public static boolean isValidOptKeyName(String arg) {
        return arg != null && arg.length() > 1 && arg.charAt(0) == '-' && !isDigit(arg.charAt(1));
    }

    /**
     * В наборе аргементов присутствует опциональный параметр c указанным именем
     *
     * @param args
     * @param optNames должен начинаться с тире(минуса) -1 не считается именем
     * ключа
     * @return
     */
    public static int optIndex(String[] args, String... optNames) {
        int len = argsCount(args);
        if (len < 1 || optNames == null || optNames.length == 0) {
            return -1;
        }
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
     *
     * @param args
     * @param name должен начинаться с тире(минуса)
     * @return
     */
    public static boolean hasOpt(String[] args, String... name) {
        return optIndex(args, name) > -1;
    }

    public static String optValue(String[] args, String defForExistsKey, String... optNames) {
        int i = optIndex(args, optNames);
        if (i > -1) {
            return (i + 1 >= args.length || CmdUtil.isValidOptKeyName(args[i + 1])) ? defForExistsKey : args[i + 1];
        }
        return null;
    }

    /**
     * Если ключ задан но значения у него нет, либо если ключ вообще не задан
     * вернуть - def
     *
     * @param args
     * @param def
     * @param optNames
     * @return
     */
    public static String optValueOrDef(String[] args, String def, String... optNames) {
        int i = optIndex(args, optNames);
        if (i > -1) {
            return (i + 1 >= args.length || CmdUtil.isValidOptKeyName(args[i + 1])) || isNullOrEmpty(args[i + 1])
                    ? def : args[i + 1];
        }
        return def;
    }

    /**
     * Получить целочисленное числовое значение ключа либо def значение
     *
     * @param args
     * @param def если ключ не задан либо если значение ключа либо не задано
     * либо заданно не корректно
     * @param optNames имена ключа
     * @return
     */
    public static long optValueLongOrDef(String[] args, long def, String... optNames) {
        int i = optIndex(args, optNames);
        if (i > -1 && i + 1 < args.length) {
            if (!isValidOptKeyName(args[i + 1])) {
                return CmdUtil.argL(args, i + 1, def);
            }
        }
        return def;
    }

    public static int optValueIntOrDef(String[] args, int def, String... optNames) {
        int i = optIndex(args, optNames);
        if (i > -1 && i + 1 < args.length) {
            if (!isValidOptKeyName(args[i + 1])) {
                return CmdUtil.argI(args, i + 1, def);
            }
        }
        return def;
    }

    public static double optValueDoubleOrDef(String[] args, double def, String... optNames) {
        int i = optIndex(args, optNames);
        if (i > -1 && i + 1 < args.length) {
            if (!isValidOptKeyName(args[i + 1])) {
                return CmdUtil.argD(args, i + 1, def);
            }
        }
        return def;
    }

    /**
     * Время либо из лонг-числа либо из форматированного вывода
     * HH:mm:ss-dd-MM-yy если есть не указанные элементы шаблона - брать из
     * текущего времени. игнорировать разделители(могут быть любые)
     *
     * @param args
     * @param def
     * @param optNames
     * @return
     */
    public static long optValueTimeMillisOrDef(String[] args, long def, String[] optNames) {
        int i = optIndex(args, optNames);
        if (i > -1 && i + 1 < args.length) {
            if (!isValidOptKeyName(args[i + 1])) {
                return CmdUtil.argTimeMillis(args, i + 1, def);
            }
        }
        return def;
    }

    /**
     * Время в миллис из строки в лонг число. Если указано просто число (больше
     * 24) - распарсить его в лонг Если указана дата-время - распарсить и
     * вернуть для него общемировой timeMillis HH:mm:ss-dd-MM-yy
     * для того чтобы указать полночь используй "00:00" либо "00"
     * строка "0" распознается кака дата самого начала эпохи 1970год и вернёт 0
     * "00"-"23" - часы,  "0"| > "23" - начало эпохи
     * @param args
     * @param i
     * @param def
     * @return
     */
    public static long argTimeMillis(String[] args, int i, long def) {
        final String s = arg(args, i);
        final int sz = s == null ? 0 : s.length();
        if (CmdUtil.isIntNum(s, true)) {
            try {
                long v = Long.parseLong(s);
                if (v > 23 || sz != 2) {// "00"-"23" - часы,  "0"| > "23" - начало эпохи
                    return v;
                }
            }
            catch (Exception e) {
                return def;
            }
        } 
        LocalDateTime l = parseTimeDateVariants(s, 0);
        if (l != null) {
            return l.atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;//UTC
        }        
        return def;
    }
    /**
     * Парсинг времени-даты из строки в обьект (Для удобного ввода с консоли)
     * Если нужно указать только часть времени разделяй время и дату двумя любыми
     * разделителями в виде time--date
     * если интересует только дата - начни с одного разделителя.
     * Все не введённые параметры для даты берутся из текущего времени, а для
     * времени - равные нулям т.е. если не задать время будет 00:00:00 для указанной даты
     * Не поддерживает указание наносекунд (сбрасывает их в ноль)
     * Поддерживаемые Варианты:
     * HH:mm:ss--dd:MM:yy
     * HH:mm:ss--dd:MM
     * HH:mm:ss-dd
     * HH--dd
     * hh:mm
     * -dd:mm:yy
     * HH:mm:ss--dd:MM:yy
     * Элементы даты и времени должны указываться строго двумя числами!
     * @param s
     * @param off
     * @return LocalDateTime
     */
    //public static LocalDateTime parseTimeDateVariants(String s, int off, long deftime) {
    public static LocalDateTime parseTimeDateVariants(String s, int off) {
        final int sz = s == null ? 0 : s.length();
        if (off + 2 <= sz && sz >= 2) {
            //HH:mm:ss--dd:MM:yy
            //012345678901234567
            LocalDateTime l = LocalDateTime.now();
            //LocalDateTime l = LocalDateTime.ofEpochSecond(deftime/1000, 0, ZoneOffset.UTC);//.now();
            ///*DEBUG*/int h=0, m=0, sec=0, n=0, d=0, M=0, y=0, off = 0;
            //int h = l.getHour(), m = l.getMinute(), sec = l.getSecond();// n = l.getNano();
            int h = 0, m = 0, sec = 0;// n = l.getNano();
            int d = l.getDayOfMonth(), M = l.getMonthValue(), y = l.getYear();
            if (isDigit(s, off)) {// если два разделителя - дальше дата
                h = parseAsInt2(s, off, h); off += 3;
                if (isDigit(s, off)) {// если два разделителя - дальше дата
                    m = parseAsInt2(s, off, m); off += 3;
                    if (isDigit(s, off)) {// если два разделителя - дальше дата
                        sec = parseAsInt2(s, off, sec); off += 3;
                    }
                }
            }
            //если время не указано(начинается с разделителя т.е. идёт сразу дата)
            //- брать время не текущее,а 00:00:00
            //else {
            //    h = 0; m = 0; sec = 0;
            //}
            
            //двойной разделитель между временем и датой
            if (!isDigit(s, off)) {
                ++off;
            }

            if (off + 2 <= sz) {
                d = parseAsInt2(s, off, d);//off += 3;
                //Для того чтобы можно было день указывать одним числом высчитываю на сколько нужно сместиться
                off += (d > 9) ? 3 : 1 + (isDigit(s, off) ? 1 : 0) + (isDigit(s, off+1) ? 1 : 0);

                if (off < sz) {
                    M = parseAsInt2(s, off, M); off += 3;
                }
                if (off < sz) {
                    y = parseAsInt2(s, off, y); off += 3;
                }
                //if (d < 0 || M < 0 || y < 0) throw new IllegalStateException("Unparsed date: '" + s+"'");
            }
            //return h+":"+m+":"+sec+" "+d+"-"+M+"-"+y;
            if (y < 1970) {
                y += 2000;
            }
            return LocalDateTime.of(y, M, d, h, m, sec, 0);
            //parse(stime, Strings.DT_FORMAT).atZone(ZoneOffset.systemDefault());
        }
        return null;//def;
    }

    /**
     * Является ли символ по указанному смещению числом. '0' -> '9'
     * @param s строка
     * @param i индекс символа
     * @return
     */
    public static boolean isDigit(String s, int i) {
        return s != null && s.length() > i && '0' <= s.charAt(i) && '9' >= s.charAt(i);
    }
    /**
     * "12" -> 12
     * Только положительные числа
     * @param s
     * @param off
     * @param def
     * @return
     */
    public static int parseAsInt2(String s, int off, int def) {
        int sz = s == null ? 0 : s.length();
        if (off >= 0 && off+1 < sz) {
            int n1 = s.charAt(off) - '0';//0==49
            int n2 = s.charAt(off+1) - '0';
            int v = 0;
            if (n1 >= 0 && n1 <= 9 ) {
                v = n1;
            } 
            if (n2 >= 0 && n2 <= 9) {
                v = v * 10 + n2;
            }
            return v;
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
        if (cmdName == null || cmdName.isEmpty()) {
            return false;
        }
        return name1 != null && cmdName.equalsIgnoreCase(name1) || name2 != null && cmdName.equalsIgnoreCase(name2);
    }

    public static boolean isCmd(String cmdName, String name) {
        return isCmd(cmdName, name, null);
    }

    //целое число
    public static boolean isIntNum(String str, boolean positiveOnly) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        if (positiveOnly) {
            return isDigitsOnly(str, 0);
        }
        if (str == DEFAULT_EMPTY_STR_ARG || str.length() < 1) {
            return false;
        }
        char c = str.charAt(0);
        boolean first = (c == '-' || (c >= 48 && c <= 57));
        return (first && str.length() == 1 || first && isDigitsOnly(str, 1));
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
     * Получить инстанс Class по его имени Поддержка примитивов
     *
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
                if (isCmd(n, "void", "V")) {
                    return void.class;
                }
                if (isCmd(n, "boolean", "?")) {
                    return boolean.class;
                }
                if (isCmd(n, "byte", "?")) {
                    return byte.class;
                }
                if (isCmd(n, "char", "?")) {
                    return char.class;
                }
                if (isCmd(n, "short", "?")) {
                    return short.class;
                }
                if (isCmd(n, "int", "I")) {
                    return int.class;
                }
                if (isCmd(n, "float", "?")) {
                    return float.class;
                }
                if (isCmd(n, "long", "?")) {
                    return long.class;
                }
                if (isCmd(n, "double", "D")) {
                    return double.class;
                }
            }
            try {
                if (!canLoad) {
                    //вернёт класс только если тот уже загружен в память класслоадера
                    java.lang.reflect.Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class[]{String.class});
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

            }
            catch (Throwable t) {
                /*DEBUG*/
                t.printStackTrace();
            }
        }
        return clazz;
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
        }
        else {
            Class cl = obj.getClass();
            out.append(cl.getSimpleName());
            if (cl == String.class) {
                out.append(".length:").append(((String) obj).length());
            }
            else {
                int sz = -1;
                if (obj instanceof java.util.List) {
                    sz = ((java.util.List) obj).size();
                }
                else if (obj instanceof java.util.Map) {
                    sz = ((java.util.Map) obj).size();
                }
                else if (obj instanceof java.util.Set) {
                    sz = ((java.util.Set) obj).size();
                }
                if (sz > -1) {
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
                key = (String) obj;
            }
            else if (obj instanceof Class) {
                key = shortNamesForReflect ? ((Class) obj).getSimpleName() : ((Class) obj).getName();
            }
            else if (obj instanceof Field) {
                key = ((Field) obj).getName();
            }
            else if (obj instanceof Method) {
                key = ((Method) obj).getName();
            }
            else {
                key = String.valueOf(obj);
            }
        }
        return key;
    }

    /**
     * 0 1 2 3
     * os chunk-obj 10 10 sub cmd param -> от 3 {sub cmd param} simple pipeline
     *
     * @param args
     * @param i
     * @return
     */
    public static String[] subArgsFrom(String[] args, int i) {
        String[] a = null;
        if (args != null) {
            int sz = args.length;
            if (i < 0) {
                i = 0;
            }
            int len = sz - i;
            if (len <= 0) {
                a = new String[0];
            }
            else {
                a = new String[len];
                for (int j = 0; j < len; j++) {
                    a[j] = args[i + j];
                }
            }
        }
        return a;
    }

    /**
     * Лист объектов в котором явно строки преобразовать в Массив строк тоже
     * самое что (List)list.toArray(String[]); но с защитой от нулов
     *
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
            }
            else {
                if (o instanceof String) {
                    val = (String) o;
                }
                else {
                    val = o.toString();
                }
            }
            args[i] = val;
        }
        return args;
    }

    //--- commons Strings --->
    /**
     * Проверить соответствует ли для полного имени(name) заданное
     * сокращение(shorthand) AcoBaCo == abc -All-Things == -at обои должны
     * начинаться с тире WorldEvent == we -WorldEvent == -we -GlobalWorldEvent
     * == -gwe (но не равно) -gw -global-world-event == -gwe global-world-event
     * == gwe
     *
     * @param name
     * @param shorthand
     * @return
     */
    public static boolean isShortHandMatched(String name, String shorthand) {
        if (!isNullOrEmpty(name) && !isNullOrEmpty(shorthand)) {
            if (name.equalsIgnoreCase(shorthand)) {
                return true;
            }
            char lc = 0x00;
            int j = 0;
            boolean goodNoMoreLetters = false;
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (i == 0 && c == '-') {
                    if (shorthand.charAt(0) != '-') {
                        return false;
                    }
                    else {
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
                    if (Character.toLowerCase(c) != hc) {
                        return false;
                    }
                    j++;
                    if (j >= shorthand.length()) {
                        goodNoMoreLetters = true;//return true;
                    }
                }
                lc = c;
            }
            return goodNoMoreLetters;
        }
        return false;
    }

    /**
     * Поиск в мапе объекта ключа соответствующего вводу input Возвращает сам
     * объект ключа если либо он равен Input либо его текстовому представлению
     * соответствует сокращенный вывод ChunkEvent == ce
     *
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

                if (isShortHandMatched(key, input)) {
                    return obj;
                }
            }
        }
        return null;
    }

    /**
     * Получить из всего массива обьектов первый соответствующий вводу обьект
     * сравнение идёт по именам или по сокращениям имён например
     * ClassFieldInteract == cfi new-world-event == nwe -All-Things == al Т.е.
     * если текстовое представление из массива соответствует вводу - вернуть
     * обьект из массива
     *
     * @param a
     * @param input
     * @return
     */
    public static Object getMatchedForInput(Object[] a, String input) {
        if (a != null && a.length > 0 && !isNullOrEmpty(input)) {
            for (int i = 0; i < a.length; i++) {
                String key = objectToString(a[i], !input.contains("."));
                if (isShortHandMatched(key, input)) {
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
     *
     * @param line
     * @param start
     * @return
     */
    public static boolean isDigitsOnly(String line, int start) {
        if (line == null || line.isEmpty() || start >= line.length()) {
            return false;
        }
        for (int i = start; i < line.length(); i++) {
            int code = line.charAt(i);
            if (!(code >= 48 && code <= 57)) {
                return false;
            }
        }
        return true;
    }

    // приватные чтобы небыло конфликта с org.swarg.commons.Strings
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static int getCharCount(String s, char c) {
        if (!isNullOrEmpty(s)) {
            int i = 0;
            int sz = s.length();
            int count = 0;
            while (i < sz) {
                i = s.indexOf(c, i);
                if (i < 0) {
                    break;
                }
                count++;
                i++;
            }
            return count;
        }
        return 0;
    }

}
