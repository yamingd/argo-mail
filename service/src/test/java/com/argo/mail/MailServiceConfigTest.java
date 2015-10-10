package com.argo.mail;

import org.junit.Test;

/**
 * Created by yamingd on 9/14/15.
 */
public class MailServiceConfigTest {

    @Test
    public void testLoad() throws Exception {
        MailServiceConfig.load();
        System.out.println(MailServiceConfig.instance);
    }
}
