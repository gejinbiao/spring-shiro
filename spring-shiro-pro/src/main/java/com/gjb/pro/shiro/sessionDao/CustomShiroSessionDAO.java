package com.gjb.pro.shiro.sessionDao;

import com.gjb.pro.shiro.session.CustomSimpleSession;
import com.gjb.pro.utils.Servlets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author gejinbiao@ucfgroup.com
 * @Title sessionDao实现session管理
 * @Description:* 针对自定义的ShiroSession的cache CRUD操作，通过isChanged标识符，确定是否需要调用Update方法
 * 通过配置securityManager在属性cacheManager查找从缓存中查找Session是否存在，如果找不到才调用下面方法
 * Shiro内部相应的组件（DefaultSecurityManager）会自动检测相应的对象（如Realm）是否实现了CacheManagerAware并自动注入相应的CacheManager。
 * @Company: ucfgroup.com
 * @Created 2018-03-01
 */
public class CustomShiroSessionDAO extends AbstractSessionDAO implements InitializingBean {

    private static final Logger logger = LogManager.getLogger(CustomShiroSessionDAO.class);


    private CacheManager cacheManager;
    private Cache<Serializable, Session> cache;
    //获取cache名称
    private String activeSessionsCacheName = "shiro-activeSessionCache";


    /**
     * 如DefaultSessionManager在创建完session后会调用该方法；
     * 如保存到关系数据库/文件系统/NoSQL数据库；即可以实现会话的持久化；
     * 返回会话ID
     *
     * @param session
     * @return
     */
    protected Serializable doCreate(Session session) {
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, sessionId);
        this.cache.put(sessionId.toString(), session);
        return sessionId;
    }

    /**
     * 根据会话ID获取会话
     *
     * @param serializable
     * @return
     */
    protected Session doReadSession(Serializable serializable) {
        Session s = null;
        HttpServletRequest request = Servlets.getRequest();
        if (request != null) {
            String uri = request.getServletPath();
            // 如果是静态文件，则不获取SESSION
            if (Servlets.isStaticFile(uri)) {
                return null;
            }
            s = (Session) request.getAttribute(serializable.toString());
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
     *
     * @param session
     * @throws UnknownSessionException
     */
    public void update(Session session) throws UnknownSessionException {
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

                /**
                 * 每次请求shiro都会更新最后访问时间，导致调用此方法，由于使用了redis或ehcache缓存，
                 * 那么如果静态资源很多会导致短时间内大量更新redis缓存
                 * 但是这是不需要的，只有在访问controller请求时才需要更新session
                 */
                HttpServletRequest request = Servlets.getRequest();
                if (request != null) {
                    String uri = request.getServletPath();
                    // 如果是静态文件，则不更新SESSION
                    if (Servlets.isStaticFile(uri)) {
                        return;
                    }
                    // 如果是视图文件，则不更新SESSION
                    if (StringUtils.endsWithIgnoreCase(uri, "jsp") || StringUtils.startsWithIgnoreCase(uri,"/WEB-INF/views/")) {
                        return;
                    }
                    //更新保存
                    this.cache.put(session.getId(), session);
                }
            }

        } catch (Exception e) {
            logger.warn("更新Session失败", e);
        }
    }

    /**
     * 删除会话；当会话过期/会话停止（如用户退出时）会调用
     *
     * @param session
     */

    public void delete(Session session) {
        if (session == null) {
            logger.error("Session 不能为null");
            return;
        }
        Serializable id = session.getId();
        if (id == null) {
            throw new NullPointerException("session id is empty");
        }
        try {
            this.cache.remove(id);
        } catch (Exception e) {
            logger.error("删除session出现异常，id:{}", id, e);
        }
    }

    /**
     * 获取所有活跃用户
     *
     * @return
     */
    public Collection<Session> getActiveSessions() {
        return (Collection) (cache != null ? cache.values() : Collections.emptySet());
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

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public String getActiveSessionsCacheName() {
        return activeSessionsCacheName;
    }

    public void setActiveSessionsCacheName(String activeSessionsCacheName) {
        this.activeSessionsCacheName = activeSessionsCacheName;
    }


}
