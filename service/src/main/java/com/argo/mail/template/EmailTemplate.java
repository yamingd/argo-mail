package com.argo.mail.template;

import com.argo.mail.EmailMessage;

/**
 * Created with IntelliJ IDEA.
 * User: Yaming
 * Date: 2014/12/22
 * Time: 21:51
 */
public interface EmailTemplate {

    /**
     * 模板id
     * @return
     */
    Integer getId();
    /**
     * 从模板生产内容.
     * @return
     */
    String render(EmailMessage emailMessage);

    /**
     * 邮件标题
     * @return
     */
    String getTitle();
}
