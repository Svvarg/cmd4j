package org.swarg.cmds;

import java.util.Objects;
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
public class CmdMHBox {

    protected String name; //обязательное поле - полное имя команды
    protected String sname;/*скоращенное имя команды может отсутствовать*/
    protected String usage;
    protected String desc;
    /*минимальное число аргументов требуемое для запуска команды, иначе выводить usage*/
    protected int reqArgs;
    protected Class[] permissions;

    /*ссылка на метод который реализует данную команду
    метод должен принимать только 1 аргумент IArgsWrapper*/
    private final MethodHandle mhandle;


    /**
     * Создание обёрточного контейнера для готовой ссылки на метод и аннотации
     * над методом
     * @param acmd мета информация о команде содержащаяся в аннотации (либо null)
     * @param mh ссылка на метод. не допустимо указывать null
     */
    public CmdMHBox(ACmd acmd, MethodHandle mh) {
        Objects.requireNonNull(mh, "methodnandle");
        this.mhandle = mh;
        this.setMeta(acmd);
    }
    /**
     *
     * @param name должно быть валидным
     * @param sname может быть null
     * @param mhandle
     */
    public CmdMHBox(String name, String sname, MethodHandle mhandle) {
        this.name = name;
        this.sname = sname;//может быть null
        checkNames();
        Objects.requireNonNull(mhandle, "methodnandle");
        this.mhandle = mhandle;
    }

    /**
     * Установить данные о команде из аннотации
     * @param acmd
     * @return
     */
    public boolean setMeta(ACmd acmd) {
        if( acmd != null) {
            this.name = acmd.name();
            this.sname = acmd.sname();
            checkNames();
            this.usage = acmd.usage();
            this.desc = acmd.desc();
            this.reqArgs = acmd.reqArgs();
            this.permissions = acmd.permissions();
            return true;
        }
        return false;
    }

    /**
     * Проверить на валидность имена. если второе имя указано пустое - установить
     * его в null
     * Если указаны не валидные имена команд - кинет IllegalStateException
     */
    protected void checkNames() {
        if (!CmdMHBox.isValidName(name)) {
            throw new IllegalStateException("name '" + name + "'");
        }
        if (sname != null) {
            if (sname.isEmpty()) {
                sname = null;
            } else if (!CmdMHBox.isValidName(sname)) {
                throw new IllegalStateException("sname '" + sname + "'");
            }
        }
    }

    public CmdMHBox setNames(String name, String sname) {
        this.name = name;
        this.sname = sname;
        checkNames();
        return this;
    }

    public CmdMHBox setUsage(String usage) {
        this.usage = usage;
        return this;
    }

    public CmdMHBox setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public CmdMHBox setPermissions(Class[] permissions) {
        this.permissions = permissions;
        return this;
    }

    public String name() {
        return this.name;
    }

    public String sname() {
        return this.sname;
    }
    
    public boolean hasShortName() {
        return isValidName(this.sname) && !this.sname.equals(this.name);
    }

    public MethodHandle getMethodHandle() {
        return mhandle;
    }

    public int reqArgs() {
        return this.reqArgs;
    }

    /**
     * Получить второе имя команды (полное или сокращенное для введенного имени n
     * @param n имя команды, если оно равно полному - вернёт сокращенное, если
     * сокращенному - полное, иначе вернёт null (это не имя данной команды)
     * @return
     */
    public String getSecondName(String n) {
        if (n != null && !n.isEmpty()) {
            return n.equals(this.name) ? this.sname : (n.equals(this.sname) ? this.name : null);
        }
        return null;
    }
    /**
     * Для определения что это полное имя(основное а не сокращенное)
     * Например для того, чтобы при выводе всех зареганых контейнеров на команды
     * не выводить дубли для уже выведенных команд
     * @param n
     * @return 
     */
    public boolean isFullName(String n) {
        return n!=null && n.equals(this.name);
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
     * Проверить может ли переданная строка быть именем команды
     * Это не должна быть пустая строка либо строка начинающаяся с цифры
     * Вопрос по поводу тире(минуса)???
     * @param name
     * @return
     */
    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && !CmdUtil.isDigit(name.charAt(0));
    }


    /**
     * Создать MH-контейнер команды для метода аннотированного ACmd
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
    public static CmdMHBox of(MethodHandles.Lookup lookup, Method m) {
        if (m != null && lookup != null && m.isAnnotationPresent(ACmd.class)) {
            Class<?>[] ptypes = m.getParameterTypes();
            if (ptypes.length != 1) {
                throw new IllegalArgumentException(
                "Method " + m + " has @ACmd annotation, but requires " + ptypes.length +
                " arguments.  Command handler methods must require a single argument.");
            }
            final Class ptype0 = ptypes[0];
            ACmd acmd = m.getAnnotation(ACmd.class);
            final String name = acmd.name();
            //- может ли имя команды начинатся с тире???
            if (!isValidName(name)) {
                throw new IllegalArgumentException("command name '" + name + "'");
            }
            if (IArgsWrapper.class.isAssignableFrom(ptype0)) {
                try {
                    final MethodHandle mh = lookup.unreflect(m);
                    return mh == null ? null : new CmdMHBox(acmd, mh);
                }
                catch (Throwable t) {
                    ;//?
                }
            }
            else {
                throw new IllegalArgumentException( 
                 "Method " + m + " has @ACmd annotation, but takes a argument "
                         + "that is not an IArgsWrapper");
            }
        }
        return null;
    }

    /**
     * Найти метод по его имени в заданном классе и создать для него контейнер
     * Например Для ручной регистрации по одной команде. Когда нужно создать
     * контейнер команды для точно известного имени метода, который может быть
     * и не аннотирован ACmd, Подразумевается что данные будут добавлены далее
     * Если же аннотация ACmd есть - заполнять на её основе все поля контейнера
     * @param lookup отвечает за зону видимости
     * @param clazz
     * @param methodName имя метода
     * @param annotatedOnly true - если метод не аннотирован ACmd вернёть null
     * иначе вернуть контейнер в котором будет MH но не будет данных о команде
     * (для дальнейшего заполнения)
     * Пока подразумевается работа только со статическмими методами
     * @return 
     */
    public static CmdMHBox of(MethodHandles.Lookup lookup, Class clazz, String methodName, boolean annotatedOnly) {
        if (clazz != null && methodName != null && !methodName.isEmpty()) {
            Method[] ma = clazz.getDeclaredMethods();
            if (ma != null && ma.length > 0) {
                for (int i = 0; i < ma.length; i++) {
                    Method m = ma[i];
                    if (m.getParameterCount() == 1 && methodName.equals(m.getName())
                            && IArgsWrapper.class.isAssignableFrom(m.getParameterTypes()[0])) {

                        MethodHandle mh = null;
                        try {
                            mh = lookup.unreflect(m);
                        }
                        catch (Throwable t) {
                            return null;//?
                        }
                        //по умолчанию полное имя команды будет совпадать с названием метода
                        //имя можно будет изменить далее по коду
                        final CmdMHBox c = new CmdMHBox(m.getName(), null, mh);
                        ACmd meta = m.getAnnotation(ACmd.class);
                        if (meta != null && c != null) {
                            //проверяю на валидное имя указанное в аннотации
                            if (!isValidName(meta.name())) {
                                throw new IllegalArgumentException("command name '" + meta.name() + "'");
                            }
                            c.setMeta(meta);
                        }
                        return c;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Вызвать выполнение метода реализующего данную команду
     * с проверкой на количество вводимых аргументов (reqArgs)
     * Если аргументов не достаточно выводит usage
     * @param w
     * @return трейс последней ошибки либо null (для CmdManager)
     */
    public Throwable perform(IArgsWrapper w) {
        if (w != null && this.mhandle != null) {
            //если требуется аргументов больше чем имеется - вывести usage
            if (reqArgs > w.argsRemain() ) {
                w.push((usage == null || usage.isEmpty()) ? ("Args Required: " + reqArgs) : usage);
                //t = уведомлять ли?
            }
            else {
                try {
                    this.mhandle.invoke(w);
                }
                catch (Throwable t) {
                    return t;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "CmdMHBox{" + "name=" + name + ", sname=" + sname + ", usage=" + usage + ", desc=" + desc + ", reqArgs=" + reqArgs + ", permissions=" + permissions + ", mhandle=" + mhandle + '}';
    }

}
