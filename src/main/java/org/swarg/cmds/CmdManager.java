package org.swarg.cmds;


import java.util.Map;
import java.util.HashMap;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import static org.swarg.cmds.CmdMHBox.isValidName;

/**
 * Простой менеджер команд, для регистрации обёрток над именем команды и ссылкой
 * на метод её реализующей к именам команды.
 *
 * Смысл этой механики в том, чтобы монтировать току доступа к нужному коду
 * прямо в рантайме либо автоматически на основе аннотированных методов.
 *
 * 02-12-21
 * @author Swarg
 */
public class CmdManager {

    /*Связь имён команд и контейнеров-обёрток над ссылками на методы их реализующие
    Одна и та же команда может иметь два имени - полное и сокращенное
    В мапе для каждого из имени будет своё соответствие на один и тот же CmdMHBox*/
    protected final Map<String, CmdMHBox> name2box;
    protected MethodHandles.Lookup lookup;
    protected Throwable lastError;


    public CmdManager() {
        this.name2box = new HashMap<>();
    }

    public void setLookup(MethodHandles.Lookup lookup) {
        this.lookup = lookup;
    }

    protected MethodHandles.Lookup lookup() {
        return lookup == null ? MethodHandles.lookup() : lookup;
    }


    /**
     * Регистрация команды внутри ToolsCommands
     * Второе имя для удобства использования. Первое полное - второе сокращенное
     * можно указывать только первое опуская второе имя.
     * c.mhandle - метод в котором описана логика выполнения данной команды
     * @param c - контейнер содержащий в себе данные о команде, её полное и сокращенное имя + ссылку на метод с логикой
     * @return 0 - регистрация не прошла 1 - только для одной команды(полного или сокращенного имени) 2 - для обоих имён
     * @IllegalStateException если команда уже зарегистрирована, либо если основное имя не валидно
     */
    public int register(CmdMHBox c) {
        if (c != null && name2box != null) {
            int i = 0;

            if (isValidName(c.name())) {
                if (!name2box.containsKey(c.name())) {//cmdFullName
                    name2box.put(c.name(), c);
                    i++;
                } else {
                    throw new IllegalStateException("Already registered '" + c.name() + "' to " + c.getMethodHandle());
                }
            }
            //основное имя обязательно должно быть задано и должно быть валидным.
            else {
                throw new IllegalStateException("name: '" + c.name()+ "'");
            }

            //регистрация доступа к команде по её сокращенному имени
            if (c.hasShortName()) {
                if (!name2box.containsKey(c.sname())) {//cmdShortName
                    name2box.put(c.sname(), c);
                    i++;
                } else {
                    throw new IllegalStateException("Already registered '" + c.name() + "' to " + c.getMethodHandle());
                }
            }
            return i;
        }
        return 0;
    }

    /**
     * Убрать из мапы (По двум именам полному и сокращенному)
     * @param c
     * @return
     */
    public boolean unreg(CmdMHBox c) {
        if (c != null && name2box != null) {
            int i = 0;
            if (name2box.containsKey(c.name())) {
                name2box.remove(c.name());
                ++i;
            }
            if (c.hasShortName() && name2box.containsKey(c.sname())) {
                name2box.remove(c.sname());
                ++i;
            }
            return i > 0;
        }
        return false;
    }

    /**
     * Убрать команду из мапы
     * @param name любое имя нужной команды полное либо сокращенное
     * автоматически определит воторое имя и уберёт из мапы связывание с
     * контейнером к команде
     * @return
     */
    public boolean unregCommand(String name) {
        if (name2box != null && name2box.containsKey(name)) {
            CmdMHBox c = name2box.remove(name);
            if (c != null) {
                //второе имя команды либо полное либо сокращенное
                String secName = c.getSecondName(name);
                if (name2box.containsKey(secName)) {
                    name2box.remove(secName);
                }
            }
            return true;
        }
        return false;
    }


    /**
     * Зарегистрировать все команды(методы аннотированные ACmd) данного класса
     * (добавить связь имена-обёртка над ссылкой на метод) в мапу для возможности
     * вызывать по одному из именён каманды (короткому или полному)
     * @param clazz
     * @param ignoreErrors только IllegalArgumentException (например плохие имена)
     * @return
     */
    public int registerClass(Class clazz, boolean ignoreErrors) {
        clearError();
        if (clazz != null && name2box != null) {
            int cnt = 0;//сколько успешных регистраций прошло
            Method[] ma = clazz.getMethods();
            if (ma != null && ma.length > 0) {
                for (int i = 0; i < ma.length; i++) {
                    Method m = ma[i];
                    if (m.isAnnotationPresent(ACmd.class)) {
                        try {
                            CmdMHBox box = CmdMHBox.of(lookup(), m);
                            if (box != null) {
                                //1 - только для одного(полного)имени 2- для обоих
                                if (register(box) > 0) {
                                    cnt++;
                                }
                            }
                        }
                        catch (IllegalArgumentException t) {
                            if (!ignoreErrors) {
                                this.lastError = t;
                                throw t;
                            }
                        }
                    }
                }
            }
            return cnt;
        }
        return -1;
    }

    /**
     * Убрать из регистрации все команды (методы аннотированные ACmd) класса
     * @param clazz
     * @return
     */
    public int unregClass(Class clazz) {
        if (clazz != null && name2box != null) {
            int cnt = 0;//снятых с регистрации команд
            Method[] ma = clazz.getMethods();
            if (ma != null && ma.length > 0) {
                for (int i = 0; i < ma.length; i++) {
                    Method m = ma[i];
                    if (m.isAnnotationPresent(ACmd.class)) {
                        ACmd acmd = m.getAnnotation(ACmd.class);
                        final String name = acmd.name();
                        //второе имя будет подхвачено в методе анрег
                        if (unregCommand(name)) {
                            ++cnt;
                        }
                    }
                }
            }
            return cnt;
        }
        return -1;//error
    }


    /**
     * Найти и зарегистрировать команду на заданное имя(полное и сокращенное)
     * по полному имени класса и методу.
     * Метод должен быть статический и принимать один аргумент IArgsWrapper
     * "Ручной" способ создания связи - имя команды метод её реализующий
     * указывается класс, имя метода и названия команды (полное и сокращенное)
     * Если метод имеет ACmd-Аннотаницию то из неё будут взяты данные, но
     * имя команды будет установлено в fullName и shortName
     * @param clazz класс в котором икать метод реализующий данную команду
     * @param method имя метода реализующую команду
     * @param fullName полное имя команды если указать Null - команда будет иметь имя метода, а короткого имени не будет.
     * @param shortName короткое
     * @return
     */
    public int regCmd(String clazz, String method, String fullName, String shortName) {
        clearError();
        try {
            Class c0 = Class.forName(clazz);
            CmdMHBox box = CmdMHBox.of(lookup(), c0, method, false);
            if (box != null) {
                if (fullName != null && !fullName.isEmpty()) {
                    box.setNames(fullName, shortName);
                }
                return register(box);
            }
            return 0;
        }
        catch (ClassNotFoundException c) {
            this.lastError = c;
            return -1;
        }
    }

    /**
     * Зарегистрировать из заданного класса метод, установив имя команды равное
     * имя методу
     * @param clazz
     * @param method
     * @return
     */
    public int regCmd(String clazz, String method) {
        return regCmd(clazz, method, null, null);
    }

    /**
     * Найти класс по имени и зарегистрировать все описанные в нём команды
     * Регистрация всех методов-команд из заданного класса помеченных
     * аннотацией ACmd
     * @param classname
     * @return
     */
    public int registerClass(String classname, boolean ignoreErrors) {
        clearError();
        try {
            Class clazz = Class.forName(classname);
            return registerClass(clazz, ignoreErrors);
        }
        catch (ClassNotFoundException c) {
            this.lastError = c;
            return -1;//класс не найден
        }
    }

    /**
     * Убрать из регистрации все команды (методы аннотированные ACmd)
     * заданного класса
     * @param classname
     * @return
     */
    public int unregClass(String classname) {
        clearError();
        try {
            Class clazz = Class.forName(classname);
            return unregClass(clazz);
        }
        catch (ClassNotFoundException e) {
            this.lastError = e;
            return -1;
        }
    }

    /**
     * Убрать из регистрации все команды
     * @return
     */
    public int unregAllCommands() {
        final int cnt = this.name2box.size();
        this.name2box.clear();
        return cnt;
    }

    // --------------------------------------------------------------------- \\

    /**
     * Проверить зарегистрирована ли команда обёрнутая в контейнер CmdMHBox
     * @param c
     * @return
     */
    public boolean hasCmd(CmdMHBox c) {
        return c != null && this.name2box != null && (this.name2box.containsKey(c.name()) || this.name2box.containsKey(c.sname()));
    }
    /**
     * Проверить по заданному имени есть ли такая зарегистрированная команда
     * @param name
     * @return
     */
    public boolean hasCmd(String name) {
        return CmdMHBox.isValidName(name) && this.name2box.containsKey(name) && this.name2box.get(name) != null;
    }
    /**
     * По имени команды получить контейнер-обёртку над ней либо null
     * @param name
     * @return
     */
    public CmdMHBox getCmdBox(String name) {
        return this.name2box.get(name);
    }

    /**
     * По имени команды (строка в массиве String[] с индексом w.ai)
     * получить обёртку на неё и вернуть
     * @param w
     * @return
     */
    public CmdMHBox getCmdBox(IArgsWrapper w) {
        if (w != null && w.hasArg()) {
            final String cmd = w.arg(w.ai());
            if (hasCmd(cmd)) {
                w.inc();
                return getCmdBox(cmd);
            } else {
                throw new IllegalStateException("Not found cmd " + cmd);
            }
        }
        throw new IllegalStateException("no command");
    }

    /**
     * Форматированная справка о команде
     * @param sb
     * @param c
     * @param showDesc
     * @param showUsage
     * @param verbose
     * @return
     */
    public StringBuilder appendCmdBoxInfo(StringBuilder sb, CmdMHBox c, boolean showDesc, boolean showUsage, boolean verbose) {
        if (c != null && sb != null) {
            //простое форматирование короткого имени если оно есть либо 4 пробела
            //   e|echo
            if (c.sname == null || c.sname.isEmpty()) {
                sb.append("    ");
            }
            else {
                int r = 4 - c.sname.length();
                while (r-- > 0) {
                    sb.append(' ');
                }
                sb.append(c.sname);
            }
            sb.append('|');
            
            sb.append(c.name()).append("  ");

            boolean descPrinted = false;
            if (showDesc && c.hasDesc()) {
                sb.append(c.desc());
                descPrinted = true;
            }

            if (showUsage && c.hasUsage()) {
                if (descPrinted) {
                    sb.append('\n').append("     ");//?
                }
                sb.append(' ').append(c.usage());
            }
            //сколько минимум аргументов нужно для вызова команды
            //"чтобы не показывало Usage"
            sb.append(" [ReqArgs:").append(c.reqArgs()).append("] ");

        }
        return sb;
    }

    /**
     * Список всех зареганых команн и методов на которые они зареганы
     * @param showDesc
     * @param showUsage
     * @param verbose
     * @return
     */
    public StringBuilder status(boolean showDesc, boolean showUsage, boolean verbose) {
        StringBuilder sb = new StringBuilder();
        if (!name2box.isEmpty()) {
            //отобразит количество связей имя-команда (может быть больше чем самих команд. т.к. есть и сокращенные имена
            sb.append("==== ").append("Commands [").append(name2box.size()).append("] ====\n");

            for (Map.Entry<String, CmdMHBox> e : name2box.entrySet()) {
                String name = e.getKey();
                CmdMHBox c = e.getValue();
                //только для полных имён команд(чтобы исключить повторение для коротких имён)
                if (c != null && c.isFullName(name)) {
                    appendCmdBoxInfo(sb, c, showDesc, showUsage, verbose).append('\n');
                }
            }

            return sb;
        }
        else {
            return sb.append("Empty\n");
        }
    }


    /**
     * Проверить ялвялется ли текущий активный аргумент на котором в ACommandBase
     * установлен текущий индекс - известной зарегистрированной командой которую
     * можно вызвать
     * @param w IArgsWrapper
     * @return
     */
    public boolean isRegCmd(IArgsWrapper w) {
        if (w != null && w.hasArg() && name2box.size() > 0) {
            final String cmd = w.arg(w.ai());
            return hasCmd(cmd);//name2box.containsKey(cmd) && name2box.get(cmd) != null;
        }
        return false;
    }

    /**
     * Рассматривая текущий активный аргумент в IArgsWrapper как имя команды
     * проверить зарегистрованна ли такая команда в name2box и если да 
     * вызвать сслыку на метод реализующий команду на выполнение
     * @param w - враппер над (String[])args
     * @return true - флаг того, что команды была распознана и для неё был вызван
     * зареганный на неё метод
     * распознать и выполнить
     */
    public boolean processCommand(IArgsWrapper w) {
        clearError();
        if (w != null && w.hasArg()) {
            final String cmd = w.arg(w.ai());
            if (hasCmd(cmd)) {
                CmdMHBox c = name2box.get(cmd);
                /*Команда прошла - смещаем текущий индекс на следующий элемент
                в (String[])args устанавливая его на первый аргумент для данной распознанной команды*/
                w.inc();
                //c.permissions();//TODO check

                this.lastError = c.perform(w);//с проверкой на количество вводимых аргументов

                return true;
            }
        }
        return false;
    }


    public boolean hasError() {
        return this.lastError != null;
    }
    
    public Throwable pollLastError() {
        Throwable t = lastError;
        clearError();
        return t;
    }

    public void clearError() {
        this.lastError = null;
    }

     /*  -----------------------------------------------------------------
                Общая механика регистрируемых на лету команд
        Регистрация как всех методов заданного класса так и по отдельности  */


    protected String getCMUsage() { 
        return "<status/reg-class/unreg-class/reg-method/unreg-cmd/rename-command/run>";
    }

    /**
     * Минимальный готовый набор команд для ручного взаимодействия с CmdManager`ом
     * Например для ручной регистрации командами в рантайме через консоль
     * @param w
     * @return
     */
    public boolean cmdCommandManager(IArgsWrapper w) {
        Object ans = "?";
        if (w.isHelpCmdOrNoArgs()) {
            ans = getCMUsage();
        }

        else if (w.isCmd("status", "st")) {
            ans = cmStatus(w);
        }
        else if (w.isCmd("reg-method", "rm")) {
            ans = cmReqMethod(w);
        }
        else if (w.isCmd("unreg-cmd", "uc")) {
            ans = cmUnregCmd(w, ans);
        }
        else if (w.isCmd("reg-class", "rc")) {
            ans = cmRegClass(w);
        }
        else if (w.isCmd("unreg-class", "uc")) {
            ans = cmUngerClass(w);
        }
        else if (w.isCmd("rename-command", "rnc")) {
            ans = cmRenameCmd(w, ans);
        }
        else if (w.isCmd("run", "r")) {
            ans = cmRunCmd(w);
        }

        //для отладки
        else if (w.isCmd("last-error", "le")) {
            ans = cmLastError(w);
        }

        else if (moreDefaultCmds(w)) {
            ;//ok use w.push(Object)
        } else {
            ans = "Uknown cmd: '"+ w.arg(w.ai())+"'";
        }

        return w.push(ans);
    }

    /**
     * для добавления в cmdCommandManager своих дефолтных команд
     * @param w
     * @return
     */
    protected boolean moreDefaultCmds(IArgsWrapper w) {
        //Пример того, как переопределять
        //if (w.isCmd("my-cmd")) {
        //    //action
        //    return w.push(Result)|true;//флаг того что команда была распознана и выволнена
        //} else
        return false;
    }


    /* --------------------------------------------------------------------- \
      Реализация логики обработчика команд для взаимодействия с CmdManager
      (например в рантайме через консоль) node - cmdCommandManager()
    \  -------------------------------------------------------------------- */


    /**
     * Формирование справки по всем зареганым командам
     * Либо вывод справки по одной указанной
     *
     * Состояние CmdManager`а - показать все зарегестрированные в нём команды
     * (файктически это создание справки на лету)
     * @param w
     * @return
     */
    protected Object cmStatus(IArgsWrapper w) {
        Object ans;
        if (w.isHelpCmd()) {
            ans = "[cmdName] [-v|-verbose] [-nd|-no-desc] [-nu|-no-usage]";
        } else {
            boolean showDesc  = !w.hasOpt("-no-desc", "-nd");
            boolean showUsage = !w.hasOpt("-no-usage", "-nu");
            /*для кастомизации вывода, путём переопределения метода
            appendCmdBoxInfo в наследниках CmdManager`a*/
            boolean verbose   =  w.hasOpt("-v", "-verbose");
            //для одной конкретной
            if (w.hasArg()) {
                final String cmd = w.arg(w.ai());
                if (hasCmd(cmd)) {
                    CmdMHBox box = getCmdBox(cmd);
                    ans = appendCmdBoxInfo(new StringBuilder(), box, showDesc, showUsage, verbose);
                } else {
                    ans = "Not found cmd: " + cmd;
                }
            }
            //для всех зареганых команд
            else {
                ans = status(showDesc, showUsage, verbose);
            }
        }
        return ans;
    }


    /**
     * Зареристрировать автоматические все аннотированные(ACmd) методы в
     * заданном классе.
     * @param w
     * @return
     */
    protected Object cmRegClass(IArgsWrapper w) {
        Object ans;
        if (w.isHelpCmdOrNoArgs()) {
            ans = "(class) [-ie|-ignore-errors]";//[Default:"+"org.swarg.mcforge.tools.DebugTools
        } else {
            String clazz = w.arg(w.aipp());//"org.swarg.mcforge.tools.DebugTools"
            //например для того чтобы пропустить ошибки в именах команд из аннотайций или другие ошибки
            boolean ignoreErrors = w.hasOpt("-ignore-errors", "-ie");//только для IllegalArgumentException заполнение данных по аннотациям
            int cnt = registerClass(clazz, ignoreErrors);
            ans = "[" + clazz + "] Commands Registered: " + cnt;
            //todo вывод полных имён
        }
        return ans;
    }

    /**
     * Cнять с регистрации все команды (аннотированные ACmd) методы для
     * указанного класса
     * @param w
     * @return
     */
    protected Object cmUngerClass(IArgsWrapper w) {
        Object ans;
        if (w.isHelpCmdOrNoArgs()) {
            ans = "(class)";
        } else {
            String clazz = w.arg(w.aipp());//"org.swarg.mcforge.tools.DebugTools"
            int cnt = unregClass(clazz);
            ans = "["+ clazz + "] Command UnRegistered: " + cnt;
        }
        return ans;
    }

    /**
     * Ручная регистрация метода. если не указывать имя команды оно будет 
     * автоматически взято из имени метода.
     * Для снятия команды с регистрации используй cmUnregCmd
     * @param w
     * @return
     */
    protected Object cmReqMethod(IArgsWrapper w) {
        Object ans;
        if (w.isHelpCmdOrNoArgs() || w.argsCount() < 2) {
            ans = "(class) (method) [name] [sname]";
        } else {
            String clazz = w.arg(w.aipp());
            String method= w.arg(w.aipp());
            String name  = w.arg(w.aipp());//имена не обязательны
            String sname = w.arg(w.aipp());
            int cnt = regCmd(clazz, method, name, sname);
            ans = "["+ clazz +"."+method+ "] isReg: " + (cnt > 0);
        }
        return ans;
    }

    /**
     * Снять команду с регистрации по одному или обоим именам
     * достаточно указать одно любое из имён команды (полное или сокращенное)
     * т.к все имена проверяются на уникальность. и не может быть две разных
     * команды с одинаковыми сокращенными именами
     * @param w
     * @param ans
     * @return
     * (cmUnregMethod)
     */
    protected Object cmUnregCmd(IArgsWrapper w, Object ans) {
        if (w.isHelpCmdOrNoArgs()) {
            ans = "(name)";
        } else {
            String name = w.arg(w.aipp());
            if (hasCmd(name)) {
                CmdMHBox box = getCmdBox(name);
                boolean unreg = unreg(box);
                ans = "UnReg: " + box.name() + "|" + box.sname() + " : " + unreg;
            }
        }
        return ans;
    }

    /**
     * [Debug]
     * @param w
     * @return
     */
    protected Object cmLastError(IArgsWrapper w) {
        Object ans;
        ans = this.lastError == null ? "-": this.lastError.getClass() + " " + this.lastError.getMessage();
        if (w.hasOpt("-print", "-p")) {
            this.lastError.printStackTrace();
        }
        return ans;
    }

    /**
     * переназначить имя команды на лету
     * (Актуально например для резервных методов с1-с8 для которых заданы
     * дефолтные имена. (javaagent)
     * @param w
     * @param ans
     * @return
     */
    protected Object cmRenameCmd(IArgsWrapper w, Object ans) {
        if (w.isHelpCmdOrNoArgs() || w.argsCount() < 2) {
            ans = "(lastname) (newfullname) [newshortname]";
        } else {
            String lastname = w.arg(w.aipp());
            //нахожу по указанному имени обёртку над командой
            if (!hasCmd(lastname)) {
                //CmdMHBox box = name2box.get(lastname);
                ans = "Not found command " + lastname;
            } else {
                String name = w.arg(w.aipp());
                String sname = w.arg(w.aipp());
                int errors = 0;
                if (!CmdMHBox.isValidName(name)) {
                    ans = "invalid name: '" + name + "'";
                    ++errors;
                }
                else if (hasCmd(name)) {
                    ans = "Already registered[1]: '" + name + "'";
                    ++errors;
                }
                if (sname != null && !sname.isEmpty()) {
                    if (!CmdMHBox.isValidName(sname)) {
                        ans = "invalid sname: '" + sname + "'";
                        ++errors;
                    }
                    else if (hasCmd(sname)) {
                        ans = "Already registered[2]: '" + sname + "'";
                        ++errors;
                    }
                }
                if (errors == 0) {
                    CmdMHBox box = getCmdBox(lastname);
                    unreg(box);
                    String lsn = box.sname();
                    box.setNames(name, sname);
                    boolean reg = register(box) > 0;
                    ans = "Command renamed: " + reg + " [" + lastname + "|" + lsn + "] > [" + box.name() + "|" + box.sname() + "]";
                }
            }
        }
        return ans;
    }

    /**
     * Для отладки и проверок(тестирование) подразумевается иной способ вызова
     * зареганых команд, но можно и так.
     * Запускаем команду по её имени с выводом результата её работы
     * TODO showLastError
     * @param w
     * @return 
     */
    protected Object cmRunCmd(IArgsWrapper w) {
        Object ans;
        if (processCommand(w)) {
            ans = w.pull(Object.class);
        } else {
            ans = "Not Found cmd: '" + w.arg(w.ai()) + "'";
        }
        return ans;
    }


}

