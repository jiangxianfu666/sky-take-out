package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    // 切入点：mapper包下，加了@AutoFill注解的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始执行公共字段自动填充");

        // 1. 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 2. 获取@AutoFill注解，拿到操作类型 INSERT / UPDATE
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        // 3. 获取mapper方法的实体参数（实体对象）
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        Object entity = args[0];

        // 4. 准备填充数据
        LocalDateTime now = LocalDateTime.now();
        // 这里简化写，实际项目从ThreadLocal获取当前登录用户ID
        Long currentUserId = 1L;

        try {
            // 5. 反射调用setter填充公共字段
            if(operationType == OperationType.INSERT){
                //新增：创建时间、创建人、修改时间、修改人
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentUserId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentUserId);
            }else if(operationType == OperationType.UPDATE){
                //修改：更新时间、更新人
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentUserId);
            }
        }catch (Exception e){
            log.error("公共字段自动填充失败", e);
        }
    }
}