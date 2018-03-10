package com.gjb.pro.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import javax.servlet.http.HttpServletRequest;

/**
 * @author gejinbiao@ucfgroup.com
 * @Title
 * @Description: Servlet工具类
 * @Company: ucfgroup.com
 * @Created 2018-03-05
 */
public class Servlets {

    private static ResourceUrlProvider resourceUrlProvider;

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static boolean isStaticFile(String uri) {
        String staticUri = resourceUrlProvider.getForLookupPath(uri);
        return staticUri != null;
    }

    public ResourceUrlProvider getResourceUrlProvider() {
        return resourceUrlProvider;
    }

    public void setResourceUrlProvider(ResourceUrlProvider resourceUrlProvider) {
        this.resourceUrlProvider = resourceUrlProvider;
    }
}
