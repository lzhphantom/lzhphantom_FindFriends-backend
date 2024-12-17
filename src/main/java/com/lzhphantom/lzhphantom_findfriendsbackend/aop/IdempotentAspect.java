package com.lzhphantom.lzhphantom_findfriendsbackend.aop;

import cn.hutool.core.util.StrUtil;
import com.lzhphantom.lzhphantom_findfriendsbackend.annotation.Idempotent;
import com.lzhphantom.lzhphantom_findfriendsbackend.common.ErrorCode;
import com.lzhphantom.lzhphantom_findfriendsbackend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotentAspect {

    private final RedissonClient redissonClient;

    @Pointcut("@annotation(com.lzhphantom.lzhphantom_findfriendsbackend.annotation.Idempotent)")
    public void idempotentPointCut() {
    }

    @Around("idempotentPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getMethod(joinPoint);
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        // 构建分布式锁的 Key
        String lockKey = buildLockKey(joinPoint, idempotent);
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            // 尝试获取锁，最多等待{waitTime}秒，锁的自动过期时间为{timeout}秒
            acquired = lock.tryLock(idempotent.waitTime(), idempotent.timeout(), idempotent.timeUnit());

            if (!acquired) {
                log.warn("Could not acquire lock for key: {}", lockKey);
                throw new BusinessException(ErrorCode.DUPLICATE_ERROR,"重复请求");
            }
            return joinPoint.proceed();
        }catch(Exception e){
            log.error("Idempotent error:{}", e.getMessage());
            throw new BusinessException(ErrorCode.DUPLICATE_ERROR,"重复请求");
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        Method method = joinPoint.getSignature().getDeclaringType().getMethod(joinPoint.getSignature().getName(),
                ((MethodSignature) joinPoint.getSignature()).getParameterTypes());
        return method;
    }

    private String buildLockKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        if (StrUtil.isEmpty(idempotent.key())) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR,"key不能为空");
        }
        String keyPrefix =  "idempotent:" + idempotent.key();
        Object[] args = joinPoint.getArgs();
        log.info("args: {}", args);

        // 可以根据实际需求自定义 key 的生成策略
        return keyPrefix + ":" + args[0]; // 假设第一个参数是唯一的 ID，比如订单 ID
    }
}
