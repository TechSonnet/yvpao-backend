package com.sonnet.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sonnet.model.domain.User;
import com.sonnet.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // 重点用户 id 列表
    // 实际应用中，可以考虑从数据库中直接读取重点用户 id
    private List<Long> mainUserList = Arrays.asList(1L);

    @Scheduled(cron = "*/30 * * * * ?")
    public void testSchedule() {
        RLock lock = redissonClient.getLock("precachejob:docache:lock");
        try{
            if (lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                for (Long userId : mainUserList) {
                    // 1. 从数据库中查询十条热点数据（如 VIP 用户）
                    QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), userQueryWrapper);
                    // 2. 用户信息格式化，方便存入 Redis
                    String redisKey = String.format("yvpao:user:commend:%s", userId);
                    // 3. 将数据放入 Redis
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    try {
                        valueOperations.set(redisKey,userPage, 3, TimeUnit.HOURS);
                    } catch (Exception e) {
                        log.error("redis set key error");
                    }
                }
            }
        }catch (InterruptedException e){
            log.error("Redisson error", e);
        }finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}
