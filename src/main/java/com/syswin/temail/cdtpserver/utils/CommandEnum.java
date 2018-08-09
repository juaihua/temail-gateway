package com.syswin.temail.cdtpserver.utils;

/**
 * Created by weis on 18/8/9.
 */
public enum CommandEnum {
    HeartbeatPing(1, "ping"),
    HeartbeatPong(2, "pong"),
    Login(100, "login");

    private  int      code;
    private  String   desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


    CommandEnum(int code,  String  desc){
        this.code  = code;
        this.desc = desc;
    }




}
