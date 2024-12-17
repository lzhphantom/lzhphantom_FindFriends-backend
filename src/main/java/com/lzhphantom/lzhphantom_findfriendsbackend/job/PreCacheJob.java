package com.lzhphantom.lzhphantom_findfriendsbackend.job;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzhphantom.lzhphantom_findfriendsbackend.mapper.UserMapper;
import com.lzhphantom.lzhphantom_findfriendsbackend.model.domain.User;
import com.lzhphantom.lzhphantom_findfriendsbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 预热缓存
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class PreCacheJob {
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    //重点用户
    private final List<Long> hotUserIdList = CollUtil.newArrayList(1L,2L,3L);
    /**
     * 每天7点预热用户推荐列表缓存
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void preCacheRecommendUser() {
        RLock lock = redissonClient.getLock("lzhphantom:user:recommend:job");
        boolean getLock = false;
        try {
            getLock = lock.tryLock(0, 3000, TimeUnit.SECONDS);
            if (getLock){
                for (Long userId : hotUserIdList) {
                    String recommendUserKey = String.format("lzhphantom:user:recommend:%s", userId);
                    Page<User> page = new Page<>(1, 20);
                    Page<User> result = userService.page(page);
                    result.setRecords(result.getRecords().stream().map(userService::getSafetyUser).collect(Collectors.toList()));
                    redisTemplate.opsForValue().set(recommendUserKey, result,8, TimeUnit.HOURS);
                }
            }
        } catch (InterruptedException e) {
            log.error("redis分布式锁获取失败:每天7点预热用户推荐列表缓存",e);
            throw new RuntimeException(e);
        }finally {
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }


    }
}
