package dev.swarg.cmds;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 08-01-21
 * @author Swarg
 */
public interface IArgsWrapper
{
    //установка аргументов
    IArgsWrapper setArgs(String[] line);
    String[]     getArgs();
    IArgsWrapper inc();

    //поместить объект ответа для дальнейшего извлечения использующим данный враппер
    boolean push(Object response);
    Object  pull(Class expClass);

    int argsCount();
    int argsRemain(); 
    boolean noArgs(int i);
    boolean noArgs();
    boolean hasArg();
    boolean hasArg(int i);
    

    String  arg(int i);
    String  arg(int i, String def);
    boolean argB(int i);
    boolean argB(int i, boolean def);
    int     argI(int i);
    int     argI(int i, int def);
    long    argL(int i, long def);
    double  argD(int i, double def);
    UUID    argUUID(int i);
    Class   argClass(int i, boolean onlyloaded);
    Method  argMethod(Class clazz);
    //reflection? Construction

    boolean isArgEqual(int i, String value);
    boolean isIntArg(int i);
    boolean isIntArg(int i, boolean positiveOnly);
    boolean isDoubleArg(int i);

    boolean isCmd(int i, String name);
    boolean isCmd(int i, String name, String shortName); //todo autoshort
    boolean isCmd(String name);//ai
    boolean isCmd(String name, String shortName);
    boolean isHelpCmd(boolean noArgsIsHelpReq);
    boolean isHelpCmd();
    boolean isHelpCmdOrNoArgs();

    //optional keys
    boolean isOptKey(int i);
    boolean hasOpt(String...optNames);
    String  optValue(String...optNames); //String defOnKeyExists,
    String  optValueOrDef(String def, String...optNames);
    long    optValueLongOrDef(long def, String...optNames);
    double  optValueDoubleOrDef(double def, String...optNames);

}
