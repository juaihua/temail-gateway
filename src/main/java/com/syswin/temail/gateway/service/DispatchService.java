package com.syswin.temail.gateway.service;

import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.util.function.Consumer;

/**
 * 抽取{@code DispatchService}是为了更方便的进行契约测试的测试用例编写，不然完全可以整合到相应的Service层中
 *
 * @author 姚华成
 * @date 2018-10-22
 */
public interface DispatchService {

  void dispatch(CDTPPacketTrans packet,
      Consumer<byte[]> consumer,
      Consumer<? super Throwable> errorConsumer);
}
