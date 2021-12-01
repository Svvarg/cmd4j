package org.swarg.cmds;

import java.util.List;
import java.lang.reflect.Method;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Вариант динамической реализации регистрации на лету новых команд
 * через methodHandler с использованием анотации ACmd.
 * Идея в том, чтобы создать связать имя команды и сслыку на метод её реализующий
 * единственный аргумент которого это IArgsWrapper.
 * Для возможности просмотреть описания(Desc) всех команд, их использование(Usage)
 * и пришла идея создать данный контейнер. Для того, чтобы хранить не просто
 * methodHandler ключ для которого имя команды, а контейнер-обёртку в котором
 * кроме самой ссылки на метод была бы мета-информация о данной команде (
 * в том числе пермишены для возможности ограничивать её выполнение)
 *
 * две мапы - имя-methodhandle
 * Контейнер содержащий в себе имя команды внутри Acmd
 * Узнать имя класса и метода имея только methodHandler можно через
 * (MemberName) DirectMethodHandle.member (приватное поле)
 *
 * 01-12-21
 * @author Swarg
 */
public class CommandMHContainer {
    //public  ACmd meta;
    private final String name; //обязательное поле - полное имя команды
    private final String sname;/*скоращенное имя команды может отсутствовать*/
    private String usage;
    private String desc;
    private Class[] permissions;

    /*ссылка на метод который реализует данную команду
    метод должен принимать только 1 аргумент IArgsWrapper*/
    private final MethodHandle mhandle;

    //private List<CommandContainer> child;

    /**
     * Создание обёрточного контейнера для готовой ссылки на метод и аннотации
     * над методом
     * @param acmd
     * @param mh
     */
    public CommandMHContainer(ACmd acmd, MethodHandle mh) {
        this.name = acmd.name();
        this.sname = acmd.sname();
        this.usage = acmd.usage();
        this.desc = acmd.desc();
        this.permissions = acmd.permissions();
        this.mhandle = mh;
    }

    public MethodHandle getMethodHandle() {
        return mhandle;
    }

    public String name() {
        return this.name;
    }

    public String sname() {
        return this.sname;
    }
    
    public boolean hasShortName() {
        return this.sname != null && !this.sname.isEmpty() && !this.sname.equals(this.name);
    }

    public String usage() {
        return this.usage;
    }

    public String desc() {
        return this.desc;
    }

    public Class[] permissions() {
        return this.permissions;
    }

    /**
     * Создать MH-контейнер команды для метода
     * в случае успеха возращает контейнер, если метод без нужно аннатации - Null
     * если ошибки в аннатации кидет исключение IllegalArgumentException
     * -метод должен приниамать один аргумент типа IArgsWrapper
     * -пока подразумевается что метод должен будет статическим (пока проверки нет)
     *
     * @param lookup MethodHandles.Lookup() | MHLookupUtil.IMPL_LOOKUP
     * @param m
     * @return
     * @throw IllegalArgumentException
     */
    public static CommandMHContainer of(MethodHandles.Lookup lookup, Method m) {
        if (m != null && lookup != null && m.isAnnotationPresent(ACmd.class)) {
            Class<?>[] ptypes = m.getParameterTypes();
            if (ptypes.length != 1) {
                throw new IllegalArgumentException( "Method " + m + " has @ACmd annotation, but requires " + ptypes.length + " arguments.  Command handler methods must require a single argument.");
            }
            final Class ptype0 = ptypes[0];
            ACmd acmd = m.getAnnotation(ACmd.class);
            final String name = acmd.name();
            if (name == null || name.isEmpty() || CmdUtil.isDigit(name.charAt(0))) { //- может ли имя команды начинатся с тире???
                throw new IllegalArgumentException("illegal name '" + name + "'");
            }
            if (IArgsWrapper.class.isAssignableFrom(ptype0)) {
                try {
                    final MethodHandle mh = lookup.unreflect(m);
                    return mh == null ? null : new CommandMHContainer(acmd, mh);
                }
                catch (Throwable t) {
                }
            }
            else {
                throw new IllegalArgumentException( "Method " + m + " has @ACmd annotation, but takes a argument that is not an IArgsWrapper | ACommandBase");
            }
        }
        return null;
    }
}
