package com.gjb.pro.service;

import com.baomidou.mybatisplus.service.IService;
import com.gjb.pro.commons.result.PageInfo;
import com.gjb.pro.model.SysLog;

/**
 *
 * SysLog 表数据服务层接口
 *
 */
public interface SysLogService extends IService<SysLog> {

    void selectDataGrid(PageInfo pageInfo);

}