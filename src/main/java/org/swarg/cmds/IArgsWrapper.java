package org.swarg.cmds;

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

    int ai();         //получить индекс текущего аргумента
    int aipp();       //вренуть индекс текущего аргумента а затем инкрементировать на 1 return ai++
    int setAi(int i);
    int argsCount();
    int argsRemain(); 
    boolean noArgs(int i);
    boolean noArgs();
    boolean hasArg();
    boolean hasArg(int i);

    String  arg(int i);
    String  arg(int i, String def);
    boolean argB(int i);
    int     argI(int i);
    int     argI(int i, int def);
    long    argL(int i, long def);
    double  argD(int i, double def);
    UUID    argUUID(int i);
    Class   argClass(int i, boolean onlyloaded);
    Method  argMethod(Class clazz);
    String  join(int i);
    //reflection? Construction

    boolean isArgEqual(int i, String value);
    boolean isIntArg(int i);
    boolean isIntArg(int i, boolean positiveOnly);
    boolean isDoubleArg(int i);

    boolean isCmd(int i, String name);
    boolean isCmd(int i, String name, String shortName); //todo autoshort
    boolean isCmd(String name);//use this.ai
    boolean isCmd(String name, String shortName);
    boolean isHelpCmd(boolean noArgsIsHelpReq);
    boolean isHelpCmd();
    boolean isHelpCmdOrNoArgs();

    //optional keys
    boolean isOptKey(int i);
    boolean isOptValue(int i);
    int     optKeyIndex(String...optNames);
    boolean hasOpt(String...optNames);
    String  optValue(String...optNames); //String defOnKeyExists,
    String  optValueOrDef(String def, String...optNames);
    long    optValueLongOrDef(long def, String...optNames);
    double  optValueDoubleOrDef(double def, String...optNames);

}
