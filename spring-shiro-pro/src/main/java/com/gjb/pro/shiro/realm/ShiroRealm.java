package com.gjb.pro.shiro.realm;

import com.gjb.pro.model.User;
import com.gjb.pro.model.vo.UserVo;
import com.gjb.pro.service.RoleService;
import com.gjb.pro.service.UserService;
import com.gjb.pro.shiro.ShiroByteSource;
import com.gjb.pro.shiro.ShiroUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author gejinbiao@ucfgroup.com
 * @Title
 * @Description: shiro权限认证
 * @Created 2018-02-23
 */
public class ShiroRealm extends AuthorizingRealm {

    private static final Logger LOGGER = LogManager.getLogger(ShiroRealm.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    public ShiroRealm(CacheManager cacheManager, CredentialsMatcher matcher) {
        super(cacheManager, matcher);
    }

    /**
     * 权限认证
     * @param principalCollection
     * @return
     */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        ShiroUser shiroUser = (ShiroUser) principalCollection.getPrimaryPrincipal();

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setRoles(shiroUser.getRoles());
        info.addStringPermissions(shiroUser.getUrlSet());

        return info;
    }

    /**
     * Shiro登录认证(原理：用户提交 用户名和密码
     * --- shiro 封装令牌
     * ---- realm 通过用户名将密码查询返回
     * ---- shiro 自动去比较查询出密码和用户输入密码是否一致
     * ---- 进行登陆控制 )
     */
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        LOGGER.info("Shiro开始登录认证");
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        UserVo uservo = new UserVo();
        uservo.setLoginName(token.getUsername());
        List<User> list = userService.selectByLoginName(uservo);
        // 账号不存在
        if (list == null || list.isEmpty()) {
            return null;
        }
        User user = list.get(0);
        // 账号未启用
        if (user.getStatus() == 1) {
            return null;
        }
        // 读取用户的url和角色
        Map<String, Set<String>> resourceMap = roleService.selectResourceMapByUserId(user.getId());
        Set<String> urls = resourceMap.get("urls");
        Set<String> roles = resourceMap.get("roles");
        ShiroUser shiroUser = new ShiroUser(user.getId(), user.getLoginName(), user.getName(), urls);
        shiroUser.setRoles(roles);
        // 认证缓存信息
        return new SimpleAuthenticationInfo(shiroUser, user.getPassword(),
                ShiroByteSource.of(user.getLoginName()), getName());
    }


    @Override
    protected Object getAuthenticationCacheKey(PrincipalCollection principals) {
        ShiroUser shiroUser = (ShiroUser) super.getAvailablePrincipal(principals);
        return shiroUser.toString();
    }

    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        super.getAvailablePrincipal(principals);
        ShiroUser shiroUser = (ShiroUser) super.getAvailablePrincipal(principals);
        return shiroUser.toString();
    }

    /**
     * 清除用户缓存
     *
     * @param shiroUser
     */
    public void removeUserCache(ShiroUser shiroUser) {
        removeUserCache(shiroUser.getLoginName());
    }

    /**
     * 清除用户缓存
     *
     * @param loginName
     */
    public void removeUserCache(String loginName) {
        //SimplePrincipalCollection principals = new SimplePrincipalCollection();
        PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
        //principals.add(loginName, super.getName());
        super.doClearCache(principals);
    }
}
