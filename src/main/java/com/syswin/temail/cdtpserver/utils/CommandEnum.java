package com.syswin.temail.cdtpserver.utils;

/**
 * Created by weis on 18/8/9.
 */
public enum CommandEnum {
    connect(100, "connect"),
    disconnect(101, "disconnect"),
    req(102, "req"),
    resp(103, "resp"),
    push(104, "push"),
    push_resp (105, "push_resp"),
    ping(106, "ping"),
    pong(107, "pong");

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
