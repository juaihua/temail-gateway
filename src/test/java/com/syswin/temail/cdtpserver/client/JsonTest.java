package com.syswin.temail.cdtpserver.client;


import com.google.gson.Gson;

/**
 * Created by weis on 18/8/5.
 */
public class JsonTest {
    public static void main(String[] args) {
        Gson gson = new Gson();
        String jsonString = "{name:'Antony',age:'22',sex:'male',telephone:'88888'}";

        Staff staff = gson.fromJson(jsonString, Staff.class);
        System.out.println(staff.toString());

        staff.setName("魏胜");
        String jsonStr = gson.toJson(staff);
        System.out.println(jsonStr);

    }

}
