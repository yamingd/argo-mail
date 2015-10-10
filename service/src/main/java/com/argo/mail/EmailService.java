package com.argo.mail;


import com.argo.annotation.RmiService;

/**
 * Created by yaming_deng on 14-8-27.
 */
@RmiService
public interface EmailService {

    /**
     * 启动服务
     * @param executor
     */
    void start(EmailExecutor executor);
    /**
     * 先进队列.
     * @param message
     */
    void add(EmailMessage message);

    /**
     * 立刻发送
     * @param message
     */
    void send(EmailMessage message);
}
