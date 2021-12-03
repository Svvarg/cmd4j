package org.swarg.cmds;

/**
 * Не изменять существующие методы - данный класс используется в тестах
 * 02-12-21
 * @author Swarg
 */
public class Model {

    /**
     *
     * @param w
     */
    public static void cmdSumWithoutAnnotation(IArgsWrapper w) {
        int x = w.argI(w.aipp());
        int y = w.argI(w.aipp());
        w.push(x+y);
    }

    /**
     * IArgsWrapper - интерфейс для возможности создавать простые обработчики
     * команд завися только от cmd4j библиотеки
     * @param w
     */
    @ACmd(name="sum", desc="sum of two ints", usage="sum (int) (int)", reqArgs=2)
    public static void cmdSum(IArgsWrapper w) {
        int x = w.argI(w.aipp());
        int y = w.argI(w.aipp());
        w.push(x+y);
    }


    @ACmd(name="echo", sname="e", desc="Simple Echo", usage="echo s1 s2 .. sn")
    public static void cmdEcho(IArgsWrapper w) {
        String msg = w.join(w.ai());
        w.push(msg);//toSender(msg);
    }


    @ACmd(name="0badname")
    public static void cmdBadName(IArgsWrapper w) {
        w.push("badName");
    }

    //нет описания при недостаточном их количестве выведет дефолтное сообщение об этом
    @ACmd(name="pow", desc="pow of two ints", reqArgs=2)
    public static void cmdPow(IArgsWrapper w) {
        int x = w.argI(w.aipp());
        int y = w.argI(w.aipp());
        w.push(Math.pow(x, y));
    }

}
