package com.gjb.pro.service;

import com.baomidou.mybatisplus.service.IService;
import com.gjb.pro.commons.result.Tree;
import com.gjb.pro.model.Organization;

import java.util.List;

/**
 *
 * Organization 表数据服务层接口
 *
 */
public interface OrganizationService extends IService<Organization> {

    List<Tree> selectTree();

    List<Organization> selectTreeGrid();

}