package com.argo.mail.impl;

import com.argo.mail.EmailExecutor;
import com.argo.mail.EmailMessage;
import com.argo.mail.EmailService;
import com.argo.mail.MailServiceConfig;
import com.argo.mail.server.EmailSMTPSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 对外的统一接口.
 * Created by yaming_deng on 14-8-27.
 */
@Service
public class EmailServiceImpl implements EmailService, InitializingBean, DisposableBean {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private static AtomicLong pending = new AtomicLong();

    private Integer batch = 10;
    private Integer interval = 1;
    private EmailExecutor executor;
    private ThreadPoolTaskExecutor pools;
    private volatile boolean stopping;
    private boolean enabled = false;

    @Autowired
    private EmailSMTPSender emailSMTPSender;

    @Override
    public void afterPropertiesSet() throws Exception {

        stopping = false;

        MailServiceConfig.load();

        batch = MailServiceConfig.instance.getBatch();
        interval = MailServiceConfig.instance.getInterval();
        enabled = MailServiceConfig.instance.isEnabled();
        if (!enabled){
            logger.warn("EmailService has been disabled!");
            return;
        }

        pools = new ThreadPoolTaskExecutor();
        pools.setCorePoolSize(batch / 10);
        pools.setMaxPoolSize(batch);
        pools.setWaitForTasksToCompleteOnShutdown(true);
        pools.afterPropertiesSet();
    }

    @Override
    public void start(EmailExecutor executor) {
        if (!enabled){
            logger.warn("EmailService has been disabled!");
            return;
        }

        this.executor = executor;
        if (this.executor != null) {
            new PullThread().start();
        }
    }

    @Override
    public void destroy() throws Exception {
        this.stopping = true;
    }

    @Override
    public void add(final EmailMessage message) {
        if (!enabled){
            logger.warn("EmailService has been disabled!");
            return;
        }

        long total = pending.incrementAndGet();
        logger.info("Pending Email to send. total = " + total);

        pools.submit(new Runnable() {
            @Override
            public void run() {
                postSend(message);
            }
        });
    }

    @Override
    public void send(final EmailMessage message) {
        if (!enabled){
            logger.warn("EmailService has been disabled!");
            return;
        }

        long total = pending.incrementAndGet();
        logger.info("Pending Email to send. total = " + total);

        pools.submit(new Runnable() {
            @Override
            public void run() {
                postSend(message);
            }
        });
    }

    public class PullThread extends Thread{

        @Override
        public void run() {

            while (!stopping){

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<EmailMessage> items = executor.dequeue(batch);
                if (items.size() == 0){
                    continue;
                }

                final CountDownLatch latch = new CountDownLatch(items.size());
                for (final EmailMessage item : items){
                    pending.incrementAndGet();
                    pools.submit(new Runnable() {
                        @Override
                        public void run() {
                            postSend(item);
                            latch.countDown();
                        }
                    });
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    protected void postSend(final EmailMessage item) {
        boolean flag = emailSMTPSender.send(item);
        if (!flag){
            item.tryLimit--;
            executor.callback(item, false);
        }else{
            executor.callback(item, true);
        }
        long total = pending.decrementAndGet();
        logger.info("Pending Email to send. total = " + total);
    }

}
