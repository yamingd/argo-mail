package com.argo.mail;

import com.argo.yaml.YamlTemplate;
import com.google.common.base.MoreObjects;

import java.io.IOException;
import java.util.Map;

/**
 * Created by yamingd on 9/14/15.
 */
public class MailServiceConfig {

    private boolean enabled = false;
    private Integer port = 25;
    private String host;
    private String user;
    private String passwd;
    private String feedback;
    private Integer failedLimit;
    private String domain;
    private Integer batch;
    private boolean auth;
    private Integer timeout;
    private Integer interval;
    private String nickname;

    private Map<String, String> props;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Integer getFailedLimit() {
        return failedLimit;
    }

    public void setFailedLimit(Integer failedLimit) {
        this.failedLimit = failedLimit;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Integer getBatch() {
        return batch;
    }

    public void setBatch(Integer batch) {
        this.batch = batch;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("host", host)
                .add("port", port)
                .add("user", user)
                .add("passwd", passwd)
                .add("feedback", feedback)
                .add("failedLimit", failedLimit)
                .add("domain", domain)
                .add("batch", batch)
                .add("auth", auth)
                .add("timeout", timeout)
                .toString();
    }

    private static final String confName = "mail.yaml";

    public static MailServiceConfig instance = null;

    /**
     * 加载配置信息
     * @throws IOException
     */
    public synchronized static void load() throws IOException {
        if (instance != null){
            return;
        }

        try {
            MailServiceConfig.instance = load(confName);
        } catch (IOException e) {
            MailServiceConfig.instance = new MailServiceConfig();
            throw e;
        }
    }

    /**
     * 加载配置信息
     * @throws IOException
     */
    public static MailServiceConfig load(String confName) throws IOException {
        MailServiceConfig config = YamlTemplate.load(MailServiceConfig.class, confName);
        return config;
    }
}
