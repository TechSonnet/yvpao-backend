package com.sonnet;
import java.util.ArrayList;
import java.util.Date;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sonnet.mapper.UserMapper;
import com.sonnet.model.domain.User;
import com.sonnet.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class AppTests {

    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Resource
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void encryptPwd(){
        String pwd = "hello word";
        String encode = bCryptPasswordEncoder.encode(pwd);
        System.out.println(encode);
    }

    @Test
    void passwordPattern(){
        String passwordPattern = "^[a-zA-Z0-9]$";
        String password = "1234567890";
        boolean isValid = password.matches(passwordPattern);
        System.out.println(isValid); // true
        password = "admin";
        isValid = password.matches(passwordPattern);
        System.out.println(isValid); // false
    }

    @Test
    void huToolsTest(){
        boolean b = StrUtil.hasBlank("11", "9","12");
        System.out.println(b);
    }

    @Test
    void saveUserTest(){
        int count = 1000;
        int temp = 0;
        ArrayList<User> userList = new ArrayList<>();
        for (int i = 50; i < 701; i++) {
            temp++;
            User user = new User();
            user.setId((long)i);
            user.setUsername("");
            user.setUserAccount("");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword("");
            user.setPhone("");
            user.setEmail("");
            user.setUserStatus(0);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            user.setIsDelete(0);
            user.setUserRole(0);
            user.setTags("");
            userList.add(user);
            if (temp == 10){
                temp = 0;
                boolean b = userService.removeBatchByIds(userList);
                userList.clear();
            }
        }
    }

}
