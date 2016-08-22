package com.unionpay.ost.exception;

/**
 * 继承运行时异常
 * Created by jsf on 16/8/4..
 */
public class MyException extends RuntimeException {

    public MyException(){
        super();
    }
    public MyException(String msg){
        super(msg);
    }
    public MyException(Exception e,String msg){
         super(msg);
         e.printStackTrace();
    }

}
