package com.sonnet.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sonnet.common.ErrorCode;
import com.sonnet.exception.BusinessException;
import com.sonnet.mapper.UserMapper;
import com.sonnet.model.domain.RegisterResult;
import com.sonnet.model.domain.User;
import com.sonnet.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.sonnet.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author chang
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-01-16 20:18:12
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 处理用户注册
     *
     * @param userAccount
     * @param password
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String password, String checkPassword) {
        RegisterResult registerResult = new RegisterResult();

        // 1. 对参数进行校验
        int registerParameter = validateRegisterParameter(userAccount, password, checkPassword);
        if (registerParameter < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名已注册");
        }
        // 2. 对密码进行加密
        String encryptPassword = Base64.encode(password);
        // 3，将数据存入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return 1;
    }

    /**
     * 校验用户注册的参数
     *
     * @param userAccount
     * @param password
     * @param checkPassword
     * @return
     */
    private int validateRegisterParameter(String userAccount, String password, String checkPassword) {

        // 判断用户名、密码、二次密码是否为空
        boolean hasBlank = StrUtil.hasBlank(userAccount, password, checkPassword);
        if (hasBlank){
            return -1;
        }
        // 用户名长度大于 4 位
        if(userAccount.length() < 4){
            return -1;
        }
        // 密码长度必须大于 6 位
        if(password.length() < 6){
            return -1;
        }
        // 不包含特殊字符
        String userAccountPattern = "^[a-zA-Z0-9]+$";
        if(!userAccount.matches(userAccountPattern)){
            return -1;
        }
        // 密码和二次密码相等
        if(!password.equals(checkPassword)){
            return -1;
        }
        // 账号不能重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_account", userAccount);
        Long count = userMapper.selectCount(userQueryWrapper);
        if (count > 0){
            return -1;
        }
        return 1;
    }


    /**
     * 处理用户注册
     *
     * @param userAccount
     * @param password
     * @param request
     * @return
     */

    @Override
    public User userLogin(String userAccount, String password, HttpServletRequest request) {
        // 参数校验
         if (!validateLoginParameter(userAccount,password)) {
            return null;
        }
        // 密码正确则返回用户信息，否则返回 null
        User user = isCorrectPassword(userAccount, password);
        if (user == null){
            return null;
        }
        // 用户信息脱敏
        User safeUser = hindUserInfo(user);
        // 保存用户状态
        saveUserStatus(safeUser, request);
        return safeUser;
    }

    /**
     * 通过用户名查询用户列表
     * @param username
     * @return
     */
    @Override
    public List<User> findByUsername(String username) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.like("username", username);
        List<User> users = userMapper.selectList(userQueryWrapper);
        return users.stream().map(user -> hindUserInfo(user)).collect(Collectors.toList());
    }

    /**
     * 校验用户登录参数
     *
     * @param userAccount
     * @param password
     * @return
     */
    private boolean validateLoginParameter(String userAccount, String password) {
        if (StrUtil.hasBlank(userAccount,password)){
            return false;
        }
        return true;
    }

    /**
     * 用户登录密码是否正确，正确返回用户信息，否则返回 null
     * @param userAccount
     * @param password
     * @return
     */
    private User isCorrectPassword(String userAccount, String password) {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("user_account", userAccount);
        userQueryWrapper.eq("user_password",Base64.encode(password));
        User user = userMapper.selectOne(userQueryWrapper);
        if (user == null){
            log.info("account or password is incorrect!");
            return null;
        }
        return user;
    }

    /**
     * 用户信息脱敏
     * @param user
     * @return
     */
    @Override
    public User hindUserInfo(User user) {
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setGender(user.getGender());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setUserRole(0);
        safeUser.setTags(user.getTags());
        return safeUser;
    }

    /**
     * 用户注销接口
     * @param request
     * @return
     */
    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }

    /**
     * 通过标签列表查询用户列表
     * @param tags
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tags) {

        // 1. 校验参数
        if (CollectionUtils.isEmpty(tags)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 构造 QueryWrapper
        QueryWrapper queryWrapper = new QueryWrapper<>();
        for(String tag : tags){
            queryWrapper = (QueryWrapper) queryWrapper.like("tags", tag);
        }

        // 3. 查询
        List<User> userList = Optional.ofNullable(userMapper.selectList(queryWrapper)).orElse(new ArrayList<>());

        return userList.stream().map(user -> this.hindUserInfo(user)).collect(Collectors.toList());
    }

    /**
     * 推荐相似用户
     *
     * @param current
     * @param pageSize
     * @param request
     * @return
     */
    @Override
    public Page<User> recommendUsers(long current, long pageSize, HttpServletRequest request) {
        // 1. 检验参数和权限
        if(current <= 0 || pageSize <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 2. 有缓存从缓存加载
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User userInfo = (User) userObject;
        String redisKey = String.format("yvpao:recommendUsers:%s",userInfo.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return userPage;
        }

        // 3. 无缓存从数据库查询,并写入缓存
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        // 这个分页查询，是怎么查的
        Page<User> page = this.page(new Page<>(current, pageSize), userQueryWrapper);
        try {
            // 设置过期时间，很重要
            valueOperations.set(redisKey, page, 1000*60*60, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return page;
    }

    /**
     * 保存用户信息状态
     * @param user
     * @param request
     */
    private void saveUserStatus(User user, HttpServletRequest request) {
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
    }
}




