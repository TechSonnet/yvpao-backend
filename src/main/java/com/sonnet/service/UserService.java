package com.sonnet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sonnet.model.domain.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author chang
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-01-16 20:18:12
*/
public interface UserService extends IService<User> {

    long userRegister(String userAccount, String password, String checkPassword);

    User userLogin(String userAccount, String password, HttpServletRequest request);

    List<User> findByUsername(String username);

    User hindUserInfo(User user);

    void userLogout(HttpServletRequest request);

    List<User> searchUsersByTags(List<String> tags);

    Page<User> recommendUsers(long current, long pageSize, HttpServletRequest request);
}
