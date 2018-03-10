package com.gjb.pro.service;

import com.baomidou.mybatisplus.service.IService;
import com.gjb.pro.commons.result.Tree;
import com.gjb.pro.model.Resource;
import com.gjb.pro.shiro.ShiroUser;

import java.util.List;

/**
 *
 * Resource 表数据服务层接口
 *
 */
public interface ResourceService extends IService<Resource> {

    List<Resource> selectAll();

    List<Tree> selectAllMenu();

    List<Tree> selectAllTree();

    List<Tree> selectTree(ShiroUser shiroUser);

}