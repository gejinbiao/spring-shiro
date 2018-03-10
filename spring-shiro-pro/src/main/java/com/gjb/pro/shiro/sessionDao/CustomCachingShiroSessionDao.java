package com.gjb.pro.shiro.sessionDao;

import com.gjb.pro.shiro.session.CustomSimpleSession;
import com.gjb.pro.utils.Servlets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author gejinbiao@ucfgroup.com
 * @Title
 * @Description:将从redis读取的session进行本地缓存，本地缓存失效时重新从redis读取并更新最后访问时间，解决shiro频繁读取redis问题
 * @Company: ucfgroup.com
 * @Created 2018-03-02
 */
public class CustomCachingShiroSessionDao extends EnterpriseCacheSessionDAO implements InitializingBean {

    private static final Logger logger = LogManager.getLogger(CustomCachingShiroSessionDao.class);

    private CacheManager cacheManager;
    private Cache<Serializable, Session> cache;
    //获取cache名称
    private String activeSessionsCacheName = "shiro-activeSessionCache";


    /**
     * 如DefaultSessionManager在创建完session后会调用该方法；
     * 如保存到关系数据库/文件系统/NoSQL数据库；即可以实现会话的持久化；
     * 返回会话ID；主要此处返回的ID.equals(session.getId())；
     *
     * @param session
     * @return
     */
    @Override
    protected Serializable doCreate(Session session) {
        // 创建一个Id并设置给Session
        Serializable sessionId = super.generateSessionId(session);
        assignSessionId(session, sessionId);
        this.cache.put(sessionId, session);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable serializable) {
        Session s = null;
        HttpServletRequest request = Servlets.getRequest();
        if (request != null) {
            String uri = request.getServletPath();
            // 如果是静态文件，则不获取SESSION
            if (Servlets.isStaticFile(uri)) {
                return null;
            }
            s = (Session) request.getAttribute("session_" + serializable);
        }
        if (s != null) {
            return s;
        }
        logger.info("从redis读取session信息");
        s = this.cache.get(serializable);
        return s;
    }


    /**
     * 更新会话；如更新会话最后访问时间/停止会话/设置超时时间/设置移除属性等会调用
     */
    @Override
    public void update(Session session) {

        //如果会话过期/停止 没必要再更新了
        try {
            if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
                return;
            }

            if (session instanceof CustomSimpleSession) {
                // 如果没有主要字段(除lastAccessTime以外其他字段)发生改变
                CustomSimpleSession ss = (CustomSimpleSession) session;
                if (!ss.isChanged()) {
                    return;
                }
                //更新保存
                this.cache.put(session.getId(), session);
            }

        } catch (Exception e) {
            logger.warn("更新Session失败", e);
        }


    }

    @Override
    public void delete(Session session) {
        try {
            this.cache.remove(session.getId());
            logger.debug("Session {} 被删除", session.getId());
        } catch (Exception e) {
            logger.warn("修改Session失败", e);
        }
    }

    @Override
    public Collection<Session> getActiveSessions() {

        return (Collection)(cache != null?cache.values(): Collections.emptySet());
    }


    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(cacheManager, "cacheManager must not be null!");
        Assert.hasText(activeSessionsCacheName, "cacheName must not be empty");
        this.cache = cacheManager.getCache(activeSessionsCacheName);
    }

    public String getActiveSessionsCacheName() {
        return activeSessionsCacheName;
    }

    public void setActiveSessionsCacheName(String activeSessionsCacheName) {
        this.activeSessionsCacheName = activeSessionsCacheName;
    }
}
