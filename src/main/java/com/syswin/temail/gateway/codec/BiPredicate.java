package com.syswin.temail.gateway.codec;

public interface BiPredicate {

  boolean check(short commandSpace, short command);
}
