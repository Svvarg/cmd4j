package org.swarg.cmds;

import java.util.Objects;
import java.util.UUID;
import org.swarg.cmds.IArgsWrapper;
import static org.swarg.cmds.CmdUtil.getCharCount;
import static org.swarg.cmds.CmdUtil.isNullOrEmpty;
import static org.swarg.cmds.CmdUtil.stringToBoolean;
import static org.swarg.cmds.CmdUtil.DEFAULT_EMPTY_STR_ARG;

/**
 * 08-01-21
 * Имплементация обёртки вокруг аргументов IArgWrapper для 
 * удобного взаимодействия с вводимыми аргументахми.
 * -автоматический сдвиг индекса текущего обрабатываемого аргумента при прохождении команды
 * -возможность передавать результирующее значение через глубокую рекурсию в нужное место (response)
 * -базовый набор функционала для конвертации строковых аргументов в нужный тип данных
 * -опциональные ключи (имя начинающееся с тире)
 *
 * isCmd("cmdName") { arg1 = arg(); } -
 *   здесь внутри isCmd автоматически будет перемещён индекс указывающий
 *   порядковый номер следующего разбираемого аргумента.
 * isCmd(0, "name") arg(1) и прочее
 *
 * String v = arg(1);  //проверка на границы аргументов args[]
 * boolean verbose = hasOpt("-verbose"); //проверка на наличие опционального ключа
 * String className = optValue("-find-class");  - выдаст значение идущее сразу
 *      за указанным именем ключа
 * long pos = argL(2, -1); //если 2й аргумент число - выдаст число либо
 *      дефолтное значение(-1), не кидая исключений
  *
 * w.ai - указатель на индекс текущей строки в args с которой начинать
 * распознавание команды и её аргументов. Пример
 * 0   1    2
 * rt jvm uptime
 * ^   ^    ^ 
 * |   |    |
 * |   |     `(uptime) аргумент для команды jvm (под-команда глубина2)
 *  \   `i=1 команда текущего контекста (под-команда глубина1)
 *   корневая команда
 
 * TODO добавить возможность распознавать сокращения для команд через
 * CmdUtil.getMatchedForInput
 * настройка кидать исключение при парсинге чисел или нет
 * @author Swarg
 * В идеале думал создать не интерфейс а класс от которого можно было бы
 * наследоваться и расширять базовый функционал, но бывают случаи когда некий
 * обработчик уже наследуется от других классов в таком случае выход только -
 * общий интерфес команд для некой стандартизайции обработки аргументов
 *
 * ArgsWrapperImpl
 */
public class ArgsWrapper implements IArgsWrapper {
    
    private String[] args;
    public int ai;//current argument index;
    private Object response;

    public ArgsWrapper() {
    }

    public ArgsWrapper(String[] args) {
        this.args = args;
    }

    /**
     * Установка новых аргументов, все старые данные затираются включая response
     * @param args
     */
    @Override
    public IArgsWrapper setArgs(String[] args) {
        this.args = args;
        this.ai = 0;
        this.response = null;
        return this;
    }
    
    @Override
    public String[] getArgs() {
        return args;
    }

    @Override
    public IArgsWrapper inc() {
        this.ai++;
        return this;
    }
    /**
     * Поместить объект в результирующее значение ( Например для переноса через
     * глубокую рекурсию в нужное место)
     * Установка ответа
     * @param response
     * @return
     */
    @Override
    public boolean push(Object response) {
        this.response = response;
        return true;
    }

    /**
     * Вытащить хранимый ответ
     * @param expClass ограничение по классу если не null будет проверено
     * является ли объект содержащийся внутри ожидаемого класса иначе null
     * для такого рода конструкций res = (String) pull(String.class)
     * Для исключения ClassCastException
     * При выдаче значение ответа не удаляет его из поля. и продолжает хранить
     * @return
     */
    @Override
    public Object pull(Class expClass) {
        Object val = null;
        if ( response !=null ) {
            if (expClass != null ) {
                Class resClass = response.getClass();
                //если задано что ожидается - проверка ожидаемого ли класса лежит в response
                if ( resClass != expClass && !expClass.isAssignableFrom(response.getClass() )) {
                    return null;
                }
            }
            val = this.response;            
        }
        return val;
    }

    /**
     * Получить полное количество аргументов независимо от внутреннего индекса
     * @return
     *
     */
    @Override
    public int argsCount() {
        return this.args == null ? 0 : this.args.length;
    }
    /**
     * Сколько осталось аргументов после текущего индекса
     * @return
     */
    @Override
    public int argsRemain() {
        return this.args == null ? 0 : this.args.length - this.ai;
    }

    /**
     *
     * @param i по дефолту начиная с 0го аргумента
     * @return
     */
    @Override
    public boolean noArgs(int i) {
        int len = argsCount();
        return (len == 0 || len <= i);
    }
    /**
     * argsRemain есть ли еще аргументы?
     * @return
     */
    @Override
    public boolean noArgs() {
        int len = argsCount();
        return (len == 0 || len <= this.ai);
    }

    protected boolean argOutOfBounds(int i) {
        return (this.args == null || i < 0 || i >= this.args.length);
    }
    /**
     * По указанному индексу доступен не пустой аргумент
     * @param i
     * @return
     */
    @Override
    public boolean hasArg(int i) {
        return !argOutOfBounds(i) && !isNullOrEmpty(args[i]);
    }

    /**
     * Есть ли аргумент в массиве this.args[] с индексом this.ai
     * @return
     */
    @Override
    public boolean hasArg() {
        return !argOutOfBounds(this.ai) && !isNullOrEmpty(args[this.ai]);
    }
    /**
     * Возвращает из массива присланных от отправителя строку с указанным индексом
     * либо DEFAULT_EMPTY_STR_ARG если выход за границы массива
     * @param i
     * @return
     */
    @Override
    public String arg(int i) {
        return argOutOfBounds(i) ? DEFAULT_EMPTY_STR_ARG: args[i];
    }
    /**
     * Получить значение стокового аргумента по индексу i либо def если
     * либо выход за границы либо аргумент пустой
     * @param i
     * @param def
     * @return
     */
    @Override
    public String arg(int i, String def) {
        String v = def;
        if (!argOutOfBounds(i) && !isNullOrEmpty(args[i])) {
            v = args[i];
        }
        return v;
    }


    /**
     * Преобразовать аргумент с указанным индексом в boolean
     * true дают строки: true yes on 1
     * @param i
     * @return
     */
    @Override
    public boolean argB(int i) {
        return stringToBoolean(arg(i));
    }
    
//    @Override
//    public boolean argB(int i, boolean def) {
//        return (hasArg(i)) ? stringToBoolean(arg(i)) : def;
//    }

    /**
     * Получить int-значение из аргумента по указанному индексу
     * либо 0 если выход за границы либо аргумент невозможно распарсить в int
     * @param i
     * @return
     */
    @Override
    public int argI(int i, int def) {
        int v = def;
        if (hasArg(i)) {
            try {v = Integer.parseInt(arg(i));} catch (Exception e) {}
        }
        return v;
    }
    @Override
    public int argI(int i) {
        return argI(i, 0);
    }

    @Override
    public long argL(int i, long def) {
        long v = def;
        if (hasArg(i)) {
            try { v = Long.parseLong(arg(i)); } catch (Exception e) {}
        }
        return v;
    }

    @Override
    public double argD(int i, double def) {
        double v = def;
        if (hasArg(i)) {
            try { v = Double.parseDouble(arg(i)); } catch (Exception e) {}
        }
        return v;
    }
    /**
     * Получить Class по его полному имени класса из аргумента с индексом i
     * @param i
     * @param canLoad только если класс уже загружен в память класслоадера иначе null
     * @return
     */
    @Override
    public Class argClass(int i, boolean canLoad) {
        return CmdUtil.argClass(arg(i), canLoad);
    }

    /**
     * Получить метод по его описанию
     * methodName ()
     * methodName ( java.lang.String java.lang.Object )
     * @param clazz
     * @return
     */
    public java.lang.reflect.Method argMethod(Class clazz) {
        if (clazz != null) {
            try {
                String name = arg(ai++);
                if (!isNullOrEmpty(name)) {
                    Class[] parameterTypes = null;
                    String s = arg(ai++);
                    int len = s == null ? 0 : s.length();
                    if (len > 0) {
                        //указание на отсутствие аргументов () или V
                        if (len == 2 && CmdUtil.isCmd(s, "()","V"))  {
                            parameterTypes = new Class[]{};
                        }
                        // ( class1 class2 .. N )
                        else if (len == 1 && s.charAt(0) == '(') {
                            //int sz = argsRemain();
                            int close = indexOfArgEquals(")");
                            int params = close - ai;
                            if (params < 0) {
                                params = 0;
                            }
                            parameterTypes = new Class[params];
                            if (params > 0) {
                                int j = 0;
                                for (int i = ai; i < close; i++) {
                                    parameterTypes[j++] = argClass(i, false);
                                }
                            }
                        }
                        return clazz.getDeclaredMethod(name, parameterTypes);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();//debug
            }
        }
        return null;
    }

    /**
     * Получить uuid из строкового аргумента с индексом i либо null если аргумент
     * не является uuid
     * @param i
     * @return
     */
    @Override
    public UUID argUUID(int i) {
        UUID uuid = null;
        if (hasArg(i)) {
            String arg = arg(i);
            if ( getCharCount(arg, '-') == 4 ) {
                try {
                    uuid = UUID.fromString( arg );
                } catch (Exception e) {;}
            }
        }
        return uuid;
    }


    /**
     * Индекс строки в find равной аргументу из arg[i]
     * Получить индекс String в массиве find значение которой равно
     * аргументу содержащемуся в массиве args по индексу i-номеру
     * @param i
     * @param find
     * @return
     */
    public int indexOfAnyEqualsArg(int i, String... find) {
        return CmdUtil.IndexOfAnyEqualsArg(args, i, find);
    }
    /**
     * Найти индекс аргумента равный строке find
     * регистрозависимо
     * @param find
     * @return
     */
    public int indexOfArgEquals(String find) {
        return CmdUtil.IndexOfArgEquals(args, find);
    }


    /**
     * Значение аргумента this.args[i] равно указанному value
     * @param i
     * @param value
     * @return
     */
    @Override
    public boolean isArgEqual(int i, String value) {
        String arg = arg(i);
        return (arg == null && value == null || arg != null && arg.equals(value));
    }

    /**
     * Аргумент по индексу i равен одной из строк
     * Не изменяет значение this.ai
     * 'String...names' нет смысла т.к. либо полное имя либо сокращенное 1 или 2
     * @param i
     * @param name1
     * @param name2
     * @return
     */
    public boolean isCmd(int i, String name1, String name2) {
        String cmdName = arg(i);
        return !isNullOrEmpty(name1) && cmdName.equalsIgnoreCase(name1) ||
               !isNullOrEmpty(name2) && cmdName.equalsIgnoreCase(name2);
    }

    public boolean isCmd(int i, String name) {
        return isCmd(i, name, null);
    }
    /**
     * Индекс указывает this.ai
     * Автоматически инкремирует thia.ai если "команда проходит"
     * (теперь ai указывает на следующий после прошедшей команды аргумент)
     *
     * @param name
     * @return
     */
    public boolean isCmd(String name) {
        return isCmd(this.ai, name, null) && (this.ai++) > -1;
    }
    
    /**
     * Инкремирует this.a если имя команды(this.args[ai]) равно одной из указанных
     * строк name shortName
     * @param name
     * @param shortName
     * @return
     */
    @Override
    public boolean isCmd(String name, String shortName) {
        return isCmd(this.ai, name, shortName) && (this.ai++) > -1;
    }

    private static final String[] HELP_ALIASES = {"help", "-help", "-h"};
    /**
     * Текущий запрос - запрос помощи ( help -help -h)
     * @param noArgsIsHelpReq считать отсутствие аргументов запросом помощи
     * @return
     */
    @Override
    public boolean isHelpCmd(boolean noArgsIsHelpReq) {
        return noArgsIsHelpReq && noArgs() || indexOfAnyEqualsArg(ai, HELP_ALIASES) > -1 && (this.ai++) > -1;
    }
    @Override
    public boolean isHelpCmdOrNoArgs() {
        return isHelpCmd(true);
    }
    @Override
    public boolean isHelpCmd() {
        return isHelpCmd(false);
    }
    /**
     * Годиться и для проверки long чисел, т.к. по сути не имеет ограничение на
     * длину последовательности цифр
     * @param i
     * @param positiveOnly
     * @return
     */
    @Override
    public boolean isIntArg(int i, boolean positiveOnly) {
        return CmdUtil.isIntNum(arg(i), positiveOnly);
    }
    @Override
    public boolean isIntArg(int i) {
        return CmdUtil.isIntNum(arg(i), false);
    }
    @Override
    public boolean isDoubleArg(int i) {
        return CmdUtil.isDouble(arg(i));
    }


    @Override
    public boolean isOptKey(int i) {
        return CmdUtil.isValidOptKeyName(arg(i));
    }
    @Override
    public boolean isOptValue(int i) {
        return CmdUtil.isValidOptKeyName(arg(i-1)) && !isNullOrEmpty(arg(i));
    }
    /**
     * Найти индекс опционального ключа -key в аргументах this.args
     * чувствителен к регистру
     * Ищет начиная от аргумента с индексом this.ai и к концу this.args
     * @param optNames должен начинаться с тире(минуса) -1 не считается именем ключа
     * @return индекс опционального ключа или -1
     */
    public int optKeyIndex(String... optNames) {
        int len = argsRemain();
        if (len > 0 && optNames != null && optNames.length > 0) {
            final int sz = argsCount();
            for (int i = sz-len; i < sz; i++) {
                String arg = args[i];
                if (CmdUtil.isValidOptKeyName(arg)) {
                    for (int j = 0; j < optNames.length; j++) {
                        if (arg.equals(optNames[j])) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    /**
     * (В аргументах есть опциональный ключ по указанному имени)
     * В переданных аргументах (this.args[]) есть хотя бы одна строка равная
     * любой строке из optNames
     * Для возможности указывать доп настройки без привязки к индексу
     * На данный момент нужно указывать OptKey в конце строки т.к. они выдаются
     * через arg(i) как и обычные аргументы
     * cmd doit -verbose -fast
     * @param optNames
     * @return
     */
    @Override
    public boolean hasOpt(String...optNames) {
        return optKeyIndex(optNames) > -1;
    }
    /**
     * Получить значение опционального ключа если он существует
     * (следующая строка в массиве this.args после имени ключа)
     * если ключ существует, но значение не задано, либо в значение попадает
     * следующий опциональный ключ (-key) то вернуть def значение
     * если ключа нет - вернуть null
     * @param optNames
     * @return
     */
    @Override
    public String optValue(String...optNames) {
        String defOnKeyExists = null;
        return CmdUtil.optValue(args, defOnKeyExists, optNames);
    }
    /**
     * Получить значение (следующий за именем указанного опционального ключа
     * аргумент, не являющийся другим опциональным ключам) опционального ключа
     * Если ключ задан но значение не задано,
     * либо если ключ вообще не задан вернуть - def
     * @param def
     * @param optNames
     * @return
     */
    public String optValueOrDef(String def, String...optNames) {
        return CmdUtil.optValueOrDef(args, def, optNames);
    }

    /**
     * Получить целочисленное значение опционального ключа по его именам
     * -key 123 -next-key...
     * @param def вернёт это значение если ключ не задан либо без значения либо с некорректным значением
     * @param optNames
     * @return
     */
    @Override
    public long optValueLongOrDef(long def, String...optNames) {
        return CmdUtil.optValueLongOrDef(args, def, optNames);
    }
    /**
     * Получить значение опционального ключа по одному из его имён
     * Если значение ключа не задано(или в значении не число)
     * или ключ вообще не задан вернёт def значение
     *
     * -key 1.23 ano-arg...
     * @param def
     * @param optNames
     * @return
     */
    @Override
    public double optValueDoubleOrDef(double def, String...optNames) {
        return CmdUtil.optValueDoubleOrDef(args, def, optNames);
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Args:[");
        if (this.args != null && this.args.length>0) {
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                if (i == ai) sb.append('|');

                sb.append( args[i] );

                if (i == ai) sb.append('|');
            }
        }
        sb.append("] AI:").append( this.ai );//arg index

        sb.append(" Response: ");
        CmdUtil.appendObjectStatus(response, sb);
        return sb.toString();
    }

    /**
     * Поучить hashCode для всех аргументов от i до конца args
     * Например конгда нужно хешировать ответ на однотипный запрос
     * или под имя файла для уникальной команды на основе хэша команды
     * @param i
     * @return
     */
    public int hashOfArgs(int i) {
        int hash = 1;
        for (int j = i; j < args.length; j++) {
            hash = 31 * hash + Objects.hashCode(args[j]);
        }
        return hash;
    }

    @Override
    public String join(int i) {
        if (this.args != null && i > -1 && i < args.length) {
            StringBuilder sb = new StringBuilder();
            for (int k = i; k < args.length; k++) {
                sb.append(args[k]).append(' ');
            }
            if (sb.length() > 1) sb.setLength(sb.length()-1);
            return sb.toString();
        }
        return "";
    }


}
