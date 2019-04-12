package com.example.IDM.util.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Aspect
@Configuration
public class TimeStatLogger 
{
    private static final Logger logger = LoggerFactory.getLogger(TimeStatLogger.class);
    @Around("execution(* *(..)) && @annotation(com.example.IDM.util.aspect.Timed)")
    public Object log(ProceedingJoinPoint point) throws Throwable 
    {
        long start = System.currentTimeMillis();
        Object result = point.proceed();
        logger.info("className={}, methodName={}, timeMs={}, threadId={}",new Object[]{
            MethodSignature.class.cast(point.getSignature()).getDeclaringTypeName(),
            MethodSignature.class.cast(point.getSignature()).getMethod().getName(),
            System.currentTimeMillis() - start,
            Thread.currentThread().getId()}
          );
        return result;
    }
}