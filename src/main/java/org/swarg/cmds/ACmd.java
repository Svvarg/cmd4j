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
    /*полное имя команды (обязательное)*/
    String name();
    /*скоращенное имя команды может отсутствовать*/
    String sname() default "";
    String usage() default "";
    String desc() default "";
    //String parent() default ""; //для Sub комманд
    Class[] permissions() default {};
}
