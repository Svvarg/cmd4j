package org.swarg.cmds.example;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.swarg.cmds.CmdManager;
import org.swarg.cmds.ArgsWrapper;
import org.swarg.cmds.IArgsWrapper;
import org.swarg.cmds.ACmd;

/**
 * Один из примеров использования командного менеджера для быстрого построения
 * небольшого консольного приложения с атоматической регистрацией команд
 * (связывания их имён и методов их реализующих) используя аннотации ACmd
 *
 * Команды Для проверки
 * (Для запуска Нужно пересобрать jar, переместив до этого данный класс из
 * Test Package в Source. Данный пример не включен в саму библиотеку)
 *
 * java -cp target/cmds4j-0.2.jar org.swarg.cmds.example.AppCM sum4 5
 * java -cp target/cmds4j-0.2.jar org.swarg.cmds.example.AppCM cm help
 * status/reg-class/unreg-class/reg-method/unreg-cmd/rename-command/run
 * 03-12-21
 * @author Swarg
 */
public class AppCM extends CmdManager {

    private PrintStream out;

    public AppCM() {
        super();
        //регистрирую в мапинге связь имя команды - метод её реализующий
        registerClass(getClass(), false);
        //так же можно добавить команды из другого класса
        //в том числе динамически в ходе работы приложения через
        //command-manager reg-class
    }

    // -----------------------  команды  -----------------------------------\\
    //          регистрируемые автоматически в конструкторе

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


    //-------------  Простой движок консольного приложения  -----------------\\


    /**
     * Пример обрабатывающий входные аргументы
     * @param args
     */
    public static void main(String[] args) {
        IArgsWrapper w = new ArgsWrapper(args);
        AppCM cm = new AppCM();
        cm.out = System.out;
        cm.performCmd(w);
    }

    /**
     * Обработка команд и вывод ответа
     * @param w
     */
    public void performCmd(IArgsWrapper w) {
        if (w.isHelpCmdOrNoArgs()) {
            out.println(status(true, false));
            return;
        }
        /*Смотрим на текущий (0й) элемент массива args и если знаем такую команду -выполняем*/
        if (isRegCmd(w)) {//тоже самое что (cm.hasCmd(w.arg(w.ai())))  - т.е.
            getCmdBox(w).perform(w);
            //возвращаем результат выполнения команды (указывая что нет ограничений на ожидаемый результат)
            out.println(w.pull(Object.class));
        }
        else if (w.isCmd("command-manager", "cm")) {
            cmdCommandManager(w);
            /*вытаскиваем ответ на команду и выводим пользователю
            это сделано для того, чтобы можно было на метод push в IArgsWrapper
            назначить своё действие - отправку пользователю по сети или просто
            вывод в консоль здесь же реализация ArgsWrapper просто запоминает
            ответ на команду и позволяет обратиться к нему */
            out.println(w.pull(Object.class));
        }
        else if (w.isCmd("interact", "i")) {
            interact(w, System.in);
        }
        else {
            out.println("Uknown command");
        }
    }

    /**
     * Интерактивная обработка команд вводимых например с (System.in)
     * Для выхода из интерактивного режима используй q
     * @param w
     * @param in System.in
     */
    public void interact(IArgsWrapper w, InputStream in) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
            out.println("Interact Mode type q - for quit");
            String line;
            do {
                line = r.readLine();
                if ("quit".equalsIgnoreCase(line) || "exit".equalsIgnoreCase(line) || "q".equalsIgnoreCase(line) || "e".equalsIgnoreCase(line)) {
                    System.out.println("Exit");
                    break;
                }
                performCmd(w.setArgs(line.split(" ")));

            } while(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }




}
