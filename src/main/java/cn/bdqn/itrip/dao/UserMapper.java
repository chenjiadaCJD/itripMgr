package cn.bdqn.itrip.dao;

import cn.bdqn.itrip.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    /**
     * 通过UserCode查找用户（登录）getUserByUserCode
     * @return
     */
    User login(@Param("userCode") String userCode,@Param("password") String password);

    /**
     * 通过异步请求 验证 邮箱或手机是否已被注册
     * @param userCode  邮箱或手机
     * @return
     */
    User getUserByUserCode(@Param("userCode") String userCode);

    /**
     * 注册
     * @param userCode  手机号/邮箱
     * @param nickname  昵称
     * @param password  密码
     * @return  返回数据库受影响行数
     */
    int register(@Param("userCode")String userCode,@Param("nickname") String nickname,@Param("password") String password);
}