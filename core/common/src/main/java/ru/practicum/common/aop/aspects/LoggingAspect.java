package ru.practicum.common.aop.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    public LoggingAspect() {
        log.info("✅ LoggingAspect created!");
    }


    /**
     * Точка среза: все методы, помеченные @Loggable
     */
    @Pointcut("@annotation(ru.practicum.common.aop.annotation.Loggable)")
    public void loggableMethods() {}

    /**
     * Логирует вход и выход из методов, помеченных @Loggable
     */
    @Around("loggableMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("----------------→ Entering {}.{}() with arguments: {}", className, methodName, args);

//        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
//            long executionTime = System.currentTimeMillis() - startTime;
            log.info("←------------ Exiting {}.{}() with result: {}", className, methodName, result);
            return result;
        } catch (Exception e) {
            log.error("✗xxxxxxxxx Error in {}.{}(): {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}