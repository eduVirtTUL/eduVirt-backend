package pl.lodz.p.it.eduvirt.aspect.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
@Order(100)
public class LoggerAspect {

    @Pointcut(value = "@annotation(pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor) || " +
            "@within(pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor)")
    private void loggingInterceptorPointcut() {}

    @Around(value = "loggingInterceptorPointcut()")
    private Object methodLoggerAdvice(ProceedingJoinPoint point) throws Throwable {
        StringBuilder stringBuilder = new StringBuilder("\n").append("Method: ");
        Object result;
        try {
            try {
                String callerIdentity = "Anonymous";
                List<String> callerRoleList = List.of("ANONYMOUS");

                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    Authentication authenticationObj = SecurityContextHolder.getContext().getAuthentication();
                    callerIdentity = authenticationObj.getName();
                    callerRoleList = authenticationObj.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
                }

                stringBuilder.append(point.getSignature().getName())
                        .append(" | Class: ")
                        .append(point.getSignature().getDeclaringType().getSimpleName())
                        .append('\n')
                        .append("Invoked by user authenticated as: ")
                        .append(callerIdentity)
                        .append(" | List of users levels: ")
                        .append(callerRoleList)
                        .append('\n');

                stringBuilder.append("List of parameters: ")
                        .append("[ ");
                for (Object parameter : point.getArgs()) {
                    stringBuilder.append(parameter).append(": ").append(parameter != null ? parameter.getClass().getSimpleName() : "null");
                    if (Arrays.stream(point.getArgs()).toList().getLast() != parameter) stringBuilder.append(", ");
                }
                stringBuilder.append(" ]").append('\n');
            } catch (Exception exception) {
                log.error("Exception: {} occurred while processing logger aspect message, since:  ", exception.getClass().getSimpleName(), exception.getCause());
                throw exception;
            }

            result = point.proceed();
        } catch (Throwable throwable) {
            stringBuilder.append("Exception: ")
                    .append(throwable.getClass().getSimpleName())
                    .append(" was thrown during method execution.");

            if (throwable.getMessage() != null) {
                stringBuilder.append(" Message: ")
                        .append(throwable.getMessage())
                        .append(".");
            }

            if (throwable.getCause() != null) stringBuilder
                    .append(" Cause: ")
                    .append(throwable.getCause().getClass().getSimpleName())
                    .append(" : ")
                    .append(throwable.getCause().getMessage());

            log.error(stringBuilder.toString());
            throw throwable;
        }

        if (result != null) {
            stringBuilder.append(" Method returned value: ")
                    .append(result)
                    .append(" of type: ")
                    .append(result.getClass().getSimpleName());
        } else {
            stringBuilder.append(" Method did not return any value.");
        }

        log.info(stringBuilder.toString());

        return result;
    }
}

