package com.lyy.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验
 *
 * @author <a href="https://github.com/Artimislyy">lyy</a>
 */
@Target(ElementType.METHOD)//个注解只能用于方法上。
@Retention(RetentionPolicy.RUNTIME)//注解在运行时仍然可用
public @interface AuthCheck {

    /**
     * 必须有某个角色
     *
     * @return
     */
    String mustRole() default "";

}

