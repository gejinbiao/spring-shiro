package com.gjb.pro.shiro.session;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionFactory;

/**
 * @author gejinbiao@ucfgroup.com
 * @Title
 * @Description: 自定义Session工厂方法 返回会标识是否修改主要字段的自定义Session
 * @Company: ucfgroup.com
 * @Created 2018-03-05
 */
public class sessionFactory implements SessionFactory {
    @Override
    public Session createSession(SessionContext sessionContext) {
        if (sessionContext != null) {
            String host = sessionContext.getHost();
            if (host != null) {
                return new CustomSimpleSession(host);
            }
        }

        return new CustomSimpleSession();
    }
}
