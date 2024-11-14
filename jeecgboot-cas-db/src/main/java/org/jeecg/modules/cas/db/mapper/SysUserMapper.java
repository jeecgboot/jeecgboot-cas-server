package org.jeecg.modules.cas.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.cas.db.entity.SysUser;

/**
 * @author zhoujf
 *
 */
public interface SysUserMapper {

    @Select("select * from sys_user where username=#{username}")
    SysUser getSysUser(String username);
    
    @Select("select * from sys_user")
    List<SysUser> queryUserList();

}
