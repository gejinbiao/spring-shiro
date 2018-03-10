package com.gjb.pro.service;

import com.baomidou.mybatisplus.service.IService;
import com.gjb.pro.commons.result.PageInfo;
import com.gjb.pro.model.User;
import com.gjb.pro.model.vo.UserVo;

import java.util.List;

/**
 *
 * User 表数据服务层接口
 *
 */
public interface UserService extends IService<User> {

    List<User> selectByLoginName(UserVo userVo);

    void insertByVo(UserVo userVo);

    UserVo selectVoById(Long id);

    void updateByVo(UserVo userVo);

    void updatePwdByUserId(Long userId, String md5Hex);

    void selectDataGrid(PageInfo pageInfo);

    void deleteUserById(Long id);
}