package com.syswin.temail.cdtpserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by weis on 18/8/9.
 */
@RestController
public class SubjectInfo {

    @GetMapping(value = "/subject")
    public String getSubject(){

        return "push_topic_1";
    }
}
