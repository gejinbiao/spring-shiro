package com.gjb.pro.shiro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gejinbiao@ucfgroup.com
 * @Title
 * @Description:
 * @Company: ucfgroup.com
 * @Created 2018-02-27
 */
public class RetryLimitHashedCredentialsMatcher extends HashedCredentialsMatcher implements InitializingBean {

    private final static Logger logger = LogManager.getLogger(RetryLimitHashedCredentialsMatcher.class);
    private final static String DEFAULT_CHACHE_NAME = "retryLimitCache";

    private Cache<String, AtomicInteger> passwordRetryCache;
    private CacheManager cacheManager;
    private PasswordHash passwordHash;
    private String retryLimitCache;

    public RetryLimitHashedCredentialsMatcher() {
        this.retryLimitCache = DEFAULT_CHACHE_NAME;


    }
    public RetryLimitHashedCredentialsMatcher(CacheManager cacheManager) {
        this();
       this.cacheManager = cacheManager;

    }

    @Override
    public boolean doCredentialsMatch(AuthenticationToken authcToken, AuthenticationInfo info) {
        String username = (String) authcToken.getPrincipal();
        // retry count + 1
        AtomicInteger retryCount = passwordRetryCache.get(username);
        if (retryCount == null) {
            retryCount = new AtomicInteger(0);
            passwordRetryCache.put(username, retryCount);
        }
        if (retryCount.incrementAndGet() > 5) {
            // if retry count > 5 throw
            logger.warn("username: " + username + " 密码连续输入错误超过5次，锁定半小时 in period");
            throw new ExcessiveAttemptsException("用户名: " + username + " 密码连续输入错误超过5次，锁定半小时！");
        } else {
            passwordRetryCache.put(username, retryCount);
        }

        boolean matches = super.doCredentialsMatch(authcToken, info);
        if (matches) {
            // clear retry count
            passwordRetryCache.remove(username);
        }
        return matches;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(PasswordHash passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRetryLimitCache() {
        return retryLimitCache;
    }

    public void setRetryLimitCache(String retryLimitCache) {
        this.retryLimitCache = retryLimitCache;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(cacheManager, "cacheManager must not be null!");
        Assert.hasText(retryLimitCache, "cacheName must not be empty!");
        this.passwordRetryCache = cacheManager.getCache(retryLimitCache);
    }
}
