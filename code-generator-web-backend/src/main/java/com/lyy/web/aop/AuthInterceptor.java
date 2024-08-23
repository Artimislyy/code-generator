package com.lyy.web.aop;

import com.lyy.web.annotation.AuthCheck;
import com.lyy.web.common.ErrorCode;
import com.lyy.web.exception.BusinessException;
import com.lyy.web.model.entity.User;
import com.lyy.web.model.enums.UserRoleEnum;
import com.lyy.web.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验 AOP
 * 拦截器AuthInterceptor会在带有AuthCheck注解的方法执行前被调用。
 * 具体来说，当Spring AOP框架检测到目标方法上存在AuthCheck注解时，会自动调用AuthInterceptor的doInterceptor方法。
 *
 * @author <a href="https://github.com/Artimislyy">lyy</a>
 */
@Aspect//定义一个切面类
@Component//标识一个类作为 Spring 容器中的一个组件。当 Spring 容器启动时，它会自动扫描并识别带有 @Component 注解的类，将其自动注册为 Bean。除了Component还有Controller\Service\@Repository
public class AuthInterceptor {

    @Resource//用于依赖注入的注解
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */

    //ProceedingJoinPoint用于在环绕通知中控制目标方法的执行。它继承自 JoinPoint 接口，并添加了一些额外的方法。
    //.proceed()调用这个方法会继续执行被拦截的方法，并返回方法的返回值。如果目标方法抛出异常，这个方法也会抛出相应的异常。
    @Around("@annotation(authCheck)")//定义一个环绕通知,可以在目标方法执行前后进行一些额外的操作,@annotation(authCheck) 是一个切点表达式，表示这个环绕通知将应用于所有带有 authCheck 注解的方法。
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 不需要权限，放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 必须有该权限才通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 如果被封号，直接拒绝
        if (UserRoleEnum.BAN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 必须有管理员权限
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)) {
            // 用户没有管理员权限，拒绝
            if (!UserRoleEnum.ADMIN.equals(userRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

