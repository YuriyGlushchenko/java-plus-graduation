package ru.practicum.common.aop.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Entering method {}", joinPoint.getSignature());
        Object[] args = joinPoint.getArgs();
        log.info("Request parameters: {}", args);

        Object result = joinPoint.proceed();

        log.info("Exiting method: {} - Response: {}", joinPoint.getSignature(), result);

        return result;
    }



}
