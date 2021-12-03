package org.swarg.cmds;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Для анатирования методов и связывания их с именами команд например через мапу
 * 1-12-21
 * @author Swarg
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ACmd {
    /*полное имя команды (обязательное) 
    При использовании совместно с CommandMHContainer не может быть пустым,
    не может начинатся с цифр. Допустимо начало с тире*/
    String name();
    /*скоращенное имя команды. может отсутствовать(приравнивается к null)*/
    String sname() default "";
    /*Автоматически выводимые Usage например при недостатке аргументов*/
    String usage() default "";
    String desc() default "";
    /*минимальное число требуемых аргументов для запуска команды
    При недостатке рагументов - автоматические  выводить usage.
    0 - команда может быть запущена без аргументов*/
    int    reqArgs() default 0;
    Class[] permissions() default {};
    //String parent() default ""; //для Sub комманд
}
