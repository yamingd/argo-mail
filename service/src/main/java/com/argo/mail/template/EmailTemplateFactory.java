package com.argo.mail.template;

import com.argo.mail.EmailMessage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Yaming
 * Date: 2014/12/22
 * Time: 21:54
 */
@Component
public class EmailTemplateFactory implements InitializingBean {

    private Map<Integer, EmailTemplate> mapping = new HashMap<Integer, EmailTemplate>();

    public static EmailTemplateFactory instance = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    /**
     * 添加模板
     * @param template
     */
    public void add(EmailTemplate template){
        mapping.put(template.getId(), template);
    }

    /**
     * 根据模板id
     * @param templateId
     * @return
     */
    public EmailTemplate find(Integer templateId){
        return mapping.get(templateId);
    }

    public String render(EmailMessage emailMessage) throws Exception{
        EmailTemplate template = mapping.get(emailMessage.bodyTemplateId);
        if (template != null){
            return template.render(emailMessage);
        }
        throw new Exception("Template Not Found. id=" + emailMessage.bodyTemplateId);
    }

}
