package com.lzhphantom.lzhphantom_findfriendsbackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 用于业务方法的幂等性处理
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String key() default ""; // 可以自定义锁的Key前缀
    long waitTime() default 3;
    long timeout() default 10; // 锁自动释放时间，默认10秒
    TimeUnit timeUnit() default TimeUnit.SECONDS; // 时间单位
}