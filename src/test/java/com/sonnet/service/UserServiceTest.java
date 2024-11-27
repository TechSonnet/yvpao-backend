package com.sonnet.service;

import com.sonnet.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void searchUsersByTags() {

        List<String> tagsList = new ArrayList<>();
        tagsList.add("Java");
//        tagsList.add("Python");

        List<User> users = userService.searchUsersByTags(tagsList);
        System.out.println(users);
    }
}