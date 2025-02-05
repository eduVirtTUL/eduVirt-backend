package pl.lodz.p.it.eduvirt.aspect.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class TransactionLoggerAspect {

    @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void transactionalMethods() {
    }

    @Pointcut("@within(org.springframework.transaction.annotation.Transactional) && !@annotation(org.springframework.transaction.annotation.Transactional)")
    public void transactionalClassesOnly() {
    }

    @Pointcut("transactionalMethods() || transactionalClassesOnly()")
    private void methodsAndClasses() {
    }

    @Around(value = "methodsAndClasses()", argNames = "joinPoint")
    private Object logTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        final Class<?> targetClass = joinPoint.getTarget().getClass();
        final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Transactional transactional = AnnotatedElementUtils.findMergedAnnotation(method, Transactional.class);
        if (transactional == null) {
            transactional = AnnotatedElementUtils.findMergedAnnotation(targetClass, Transactional.class);
        }

        final String propagation = Objects.isNull(transactional) ? "UNKNOWN" : transactional.propagation().name();
        final String readOnly = Objects.isNull(transactional) ? "UNKNOWN" : String.valueOf(transactional.readOnly());

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            try {
                final UUID id = UUID.fromString(Objects.requireNonNull(TransactionSynchronizationManager.getCurrentTransactionName()));
                log.info("TRANSACTION: CONTINUE | id: {} | method: {}.{} | propagation: {} | readOnly: {}", id, joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName(), propagation, readOnly);
            } catch (IllegalArgumentException e) {
                final UUID id = UUID.randomUUID();
                TransactionSynchronizationManager.setCurrentTransactionName(id.toString());
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationImpl(id.toString(), log));
                log.info("TRANSACTION: START | id: {} | method: {}.{} | propagation: {} | readOnly: {}", id, joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName(), propagation, readOnly);
            }
        }

        return joinPoint.proceed();
    }

}
