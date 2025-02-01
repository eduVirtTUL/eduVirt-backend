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
import org.slf4j.event.Level;

@Slf4j
@Aspect
@Component
@Order(100)
public class LoggerAspect {

    @Pointcut(value = "@annotation(pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor) || " +
            "@within(pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor)")
    private void loggingInterceptorPointcut() {}

    @Pointcut(value = "@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Controller)")
    private void controllerMethodPointcut() {}

    @Pointcut(value = "@within(org.springframework.stereotype.Service)")
    private void serviceMethodPointcut() {}

    @Pointcut(value = "execution(* org.springframework.data.repository.Repository+.*(..))))")
    private void repositoryMethodPointcut() {}

    @Around("controllerMethodPointcut()")
    private Object controllerMethodLogger(ProceedingJoinPoint point) throws Throwable {
        return logWithGivenLevel(Level.INFO, point);
    }

    @Around(value = "repositoryMethodPointcut() || serviceMethodPointcut() " +
            "|| (loggingInterceptorPointcut() && !controllerMethodPointcut())")
    private Object repositoryAndServiceMethodLogger(ProceedingJoinPoint point) throws Throwable {
        return logWithGivenLevel(Level.DEBUG, point);
    }

    private Object logWithGivenLevel(Level level, ProceedingJoinPoint point)
            throws Throwable {
        StringBuilder builder = new StringBuilder();
        Object result;
        try {
            appendMethodExecution(builder, point);
            builder.append(" | ");
            appendSubject(builder);
            builder.append(" | ");
            appendMethodParameters(builder, point);
            builder.append(" | ");

            result = point.proceed();
        } catch (Throwable throwable) {
            appendExceptionInfo(builder, throwable);
            log.error(builder.toString());
            throw throwable;
        }

        appendResultInfo(builder, result);
        log.atLevel(level).log(builder.toString());
        return result;
    }

    private void appendMethodExecution(StringBuilder stringBuilder, ProceedingJoinPoint joinPoint) {
        stringBuilder.append("Method: ")
                .append(joinPoint.getSignature().getName())
                .append(" in class: ")
                .append(joinPoint.getSignature().getDeclaringType().getSimpleName());
    }

    private void appendSubject(StringBuilder stringBuilder) {
        String callerIdentity = "[Anonymous]";
        List<String> callerRoleList = List.of("ROLE_ANONYMOUS");

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            Authentication authenticationObj = SecurityContextHolder.getContext().getAuthentication();
            callerIdentity = authenticationObj.getName();
            callerRoleList = authenticationObj.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        }

        stringBuilder.append("Invoked by: ").append(callerIdentity)
                .append(" with roles: ").append(callerRoleList);
    }

    private void appendMethodParameters(StringBuilder stringBuilder, ProceedingJoinPoint point) {
        Object[] parameters = point.getArgs();
        if (parameters.length == 0) stringBuilder.append("Method does not take any parameters.");
        else {
            stringBuilder.append("List of parameters: ")
                    .append("[ ");
            for (Object parameter : point.getArgs()) {
                stringBuilder
                        .append(parameter).append(": ")
                        .append(parameter != null ? parameter.getClass().getSimpleName() : "null");
                if (Arrays.stream(point.getArgs()).toList().getLast() != parameter)
                    stringBuilder.append(", ");
            }
            stringBuilder.append(" ]");
        }
    }

    private void appendExceptionInfo(StringBuilder builder, Throwable throwable) {
        builder.append("Exception: ")
                .append(throwable.getClass().getSimpleName())
                .append(" was thrown during method execution.");

        if (throwable.getMessage() != null)
            builder.append(" Message: ")
                    .append(throwable.getMessage())
                    .append(".");

        if (throwable.getCause() != null)
            builder.append(" Cause: ")
                .append(throwable.getCause().getClass().getSimpleName())
                .append(" : ")
                .append(throwable.getCause().getMessage());
    }

    private void appendResultInfo(StringBuilder builder, Object result) {
        if (result != null) {
            builder.append(" Method returned value: ")
                    .append(result)
                    .append(" of definition: ")
                    .append(result.getClass().getSimpleName());
        } else {
            builder.append(" Method did not return any value.");
        }
    }

}

