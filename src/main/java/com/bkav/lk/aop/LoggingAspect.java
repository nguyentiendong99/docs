package com.bkav.lk.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Pointcut("within(com.bkav.lk.web.rest..*) || within(com.bkav.lk.service.impl..*)")
    public void logBeforeFunctionPointcut() {

    }

    @Before("logBeforeFunctionPointcut()")
    public void logBeforeFunctionAdvice(JoinPoint joinPoint) {
        logger.debug("Class {}. Function {}() with argument[s] = {}", joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
    }

}
