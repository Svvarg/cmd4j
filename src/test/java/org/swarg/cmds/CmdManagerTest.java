package org.swarg.cmds;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 02-12-21
 * @author Swarg
 */
public class CmdManagerTest {

    //helper
    public Object exec(CmdManager cm, ArgsWrapper w, String... a) {
        cm.cmdCommandManager(w.setArgs(a));
        return w.pull(Object.class);
    }

    public boolean assertContainsAll(Object in, String...parts) {
        String s = in.toString();
        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];
            if (!s.contains(part)) {
                fail("Not Found ["+part+"] in: '" + s + "'");
                return false;
            }
        }
        return true;
    }

    @Test
    public void test_RegNonAnnotationCmds() {
        System.out.println("commandsCmdManager");

        //подготовка для проверки
        final Class any = Object.class;
        CmdManager cm = new CmdManager();

        //для передачи служебных(дефолтных) команд внутрь CmdManager
        ArgsWrapper w = new ArgsWrapper(new String[]{"help"});

        cm.cmdCommandManager(w);
        //получение ответа на выполненную команду "help"
        Object res = w.pull(any);
        assertEquals(cm.getCMUsage(), res);
        
        //то же самое одной строчкой через хэлпер
        assertEquals(cm.getCMUsage(), exec(cm, w, "help"));

        
        //смотрим на пустой список команд
        cm.cmdCommandManager(w.setArgs(new String[]{"status"}));//== args(w, "status")
        res = w.pull(any);
        assertEquals("Empty\n", res.toString());


        //ручная регистрация метода без указания имени команды - для неё будет вязто имя метода
        String cn = "org.swarg.cmds.Model";
        String mn = "cmdSumWithoutAnnotation";
        res = exec(cm, w, "reg-method", cn, mn);//"(class) (method) [name] [sname]";
        //проверям успешность регистрации команды в менеджере
        assertEquals("registerd", true, cm.hasCmd(mn));

        //данные о команде по одному из её имён ( здесь по имени метода)
        res = exec(cm, w, "status", mn);
        assertContainsAll(res, " |", mn);//короткое имя показывается до полного, если его нет идёт "  |"
        //System.out.println(res);


        //регистрация метода из указанного класса на имя команды "cmd-sum"
        res = exec(cm, w, "reg-method", cn, mn, "cmd-sum");
        //[org.swarg.cmds.Model.cmdSumWithoutAnnotation] isReg: true
        assertContainsAll(res, cn, mn, "isReg", "true");
        //System.out.println(res);
        res = exec(cm, w, "status", "not-exists-cmd-name");
        assertContainsAll(res, "Not found cmd","not-exists-cmd-name");

        res = exec(cm, w, "status", "cmd-sum");
        assertContainsAll(res, " |", "cmd-sum");

        //получить список всех команд и их описание
        res = exec(cm, w, "status");
        assertContainsAll(res, mn, "cmd-sum");
        assertEquals(2, cm.name2box.size());

        //вызов команды по её имени с указанными аргументами
        res = exec(cm, w, "run", "cmd-sum", "4", "8");
        assertEquals(12, res);

        //пример вызова команды по её имени через менеджер.
        assertEquals(true, cm.hasCmd("cmd-sum"));
        //Т.к. аргументы в реализацию команды передаётся через ArgsWrapper, а не на прямую:
        ArgsWrapper aw = new ArgsWrapper(new String[]{"16", "8"});
        //perform возвращется null или throwable если при выволнении команды была ошибка
        assertEquals("No Throwable", null, cm.getCmdBox("cmd-sum").perform(aw));
        //достаём результат выполнения команды
        assertEquals(24, aw.pull(any));

        //убрать команду из менеджера
        res = exec(cm, w, "unreg-cmd", "cmd-sum");
        assertContainsAll(res, "UnReg:", "cmd-sum", "|", "true");
        assertEquals(1, cm.name2box.size());

        //Убираю все команды
        cm.unregAllCommands();// cm.name2box.clear();
    }


    @Test
    public void test_RegAnnotationCmds() {
        System.out.println("RegAnnotationCmds");
        CmdManager cm = new CmdManager();

        ArgsWrapper w = new ArgsWrapper();

        //регистрация с подхватом настроек в аннотации
        String cn = "org.swarg.cmds.Model";
        String mn = "cmdSum";//метод с аннотацией

        //регистрация конкретного метода в заданном классе
        Object res = exec(cm, w, "reg-method", cn, mn);
        assertContainsAll(res, cn, mn, "isReg", "true");


        //выполнить команду с заданными аргументами sum 12 16
        assertEquals(true, cm.hasCmd("sum"));//имя команды взято из аннотации
        CmdMHBox box = cm.getCmdBox("sum");
        assertEquals("sum", box.name);
        assertEquals(null, box.sname);

        assertEquals("No Errors", null, box.perform(w.setArgs(new String[]{"12","16"})));
        assertEquals(28, w.pull(Object.class));

        res = exec(cm, w, "status", "sum");
        //   |sum  sum of two ints sum (int) (int)
        assertContainsAll(res, "sum", box.desc(), box.usage());
        //System.out.println(res);

        //вызов команды без аргументов для которой нужно минимум 2 аргумента
        //автоматические отображение usage
        boolean f = cm.processCommand(w.setArgs(new String[]{"sum"}));//box.perform(w.setArgs(new String[]{}))
        assertTrue(f);
        Object ans = w.pull(Object.class);
        assertEquals("Usage expected", cm.getCmdBox("sum").usage(), ans);

        //вызов команды без usage с недостаточным кол-вом аргументов
        exec(cm, w, "reg-method", cn, "cmdPow");

        f = cm.processCommand(w.setArgs(new String[]{"pow"}));
        assertTrue(f);
        ans = w.pull(Object.class);
        assertContainsAll(ans, "Args Required:",  "2");//дефолтное уведомление о том, что не хватает аргументов

        cm.processCommand(w.setArgs(new String[]{"pow", "2"}));
        assertContainsAll(w.pull(Object.class), "Args Required:",  "2");

        //с нужным кол-вом аргументов - проходит
        cm.processCommand(w.setArgs(new String[]{"pow", "2", "3"}));
        assertContainsAll(w.pull(Object.class), "8.0");
    }
    

    @Test
    public void test_RegCmdWithBadNameInAnnotation() {
        System.out.println("RegCmdWithBadNameInAnnotation");
        CmdManager cm = new CmdManager();
        ArgsWrapper w = new ArgsWrapper();

        //регистрация с подхватом настроек в аннотации
        String cn = "org.swarg.cmds.Model";
        String mn = "cmdBadName";//метод с аннотацией содержащий невалидное имя команды
        try {
            exec(cm, w, "reg-method", cn, mn);
            fail("Exp iae");
        }
        catch (IllegalArgumentException e) {
            assertEquals("command name '0badname'", e.getMessage());
            //ok
        }

        //руками указать другое имя не выйдет (TODO)
        try {
            exec(cm, w, "reg-method", cn, mn, "badname");
            fail("Exp iae");
        }
        catch (IllegalArgumentException e) {
            assertEquals("command name '0badname'", e.getMessage());
            //ok
        }
        assertEquals(0, cm.name2box.size());
    }


    @Test
    public void test_RegAllCommandsInClass() {
        System.out.println("RegAllCommandsInClass");
        CmdManager cm = new CmdManager();
        ArgsWrapper w = new ArgsWrapper();

        //регистрация с подхватом настроек в аннотации
        String cn = "org.swarg.cmds.Model";
        try {
            Object res = exec(cm, w, "reg-class", cn);
            fail("Exp iae");
        }
        catch (IllegalArgumentException e) {
            assertEquals("command name '0badname'", e.getMessage());
            //ok
        }
        //как то случайно может перетасовывать методы с которых начинает регистрацию
        //поэтому может валится сразу не зарегав ни одного метода
        //assertTrue("Some method registered", cm.name2box.size() > 0);
        cm.unregAllCommands();

        //пропускаем все ошибочные обьявления команд(аннотаций над методами - плохие имена)
        Object res = exec(cm, w, "reg-class", cn, "-ignore-errors");
        assertEquals("AutoReg all method in class", true, cm.hasCmd("sum"));
        assertContainsAll(res, cn, "Registered:",
                "3"//sum & echo & pow
        );
        assertEquals(true, cm.hasCmd("sum"));
        assertEquals(true, cm.hasCmd("echo"));
        //System.out.println("##"+res);

        res = cm.status(true, true).toString();
        CmdMHBox echo1 = cm.getCmdBox("e");
        CmdMHBox echo2 = cm.getCmdBox("echo");
        assertEquals(echo1, echo2);

        CmdMHBox sum = cm.getCmdBox("sum");

        assertContainsAll(res, echo1.name(), echo1.sname(), echo1.desc(), echo1.usage(),
                sum.name(), sum.desc(), sum.usage());
        //System.out.println(res);
        /*Здесь показывает кол-во связей имя-команда. реально команд 2 связи 3(е|echo на один контейнер ссылаются в мапе)
        ==== Commands [3] ====
            |sum  sum of two ints sum (int) (int)
           e|echo  Simple Echo echo s1 s2 .. sn
        */
    }

}
