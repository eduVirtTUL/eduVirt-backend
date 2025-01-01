package pl.lodz.p.it.eduvirt.aspect.repository;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.exceptions.general.ApplicationBaseException;
import pl.lodz.p.it.eduvirt.exceptions.general.IntervalServerError;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Order(5)
@Component
public class RepositoryAspect {

    private final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Pointcut(value = "execution(* pl.lodz.p.it.eduvirt.repository..*(..))")
    private void repositoryMethodPointcut() {}

    @Around(value = "repositoryMethodPointcut()")
    private Object handleRepositoryMethodExceptions(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (OptimisticLockException optimisticLockException) {
            throw new ApplicationOptimisticLockException(
                    "Object could not be edited, since it was already edited by other user");
        } catch (DataIntegrityViolationException exception) {
            Throwable exceptionCopy = exception;
            do {
                if (exceptionCopy.getMessage().contains("cluster_metric_metric_id_fk")) {
                    throw new MetricDeleteException("Given metric could not be deleted");
                } else if (exceptionCopy.getMessage().contains("cluster_metric_cluster_id_unique")) {
                    Matcher matcher = Pattern.compile(UUID_REGEX).matcher(exceptionCopy.getMessage());
                    List<UUID> matches = new LinkedList<>();
                    while (matcher.find()) { matches.add(UUID.fromString(matcher.group())); }
                    throw new ClusterMetricExistsException(matches.getFirst(), matches.getLast());
                }
                exceptionCopy = exceptionCopy.getCause();
            } while (exceptionCopy != null);
            throw new ApplicationDatabaseException(exception.getMessage());
        } catch (ConstraintViolationException exception) {
            Set<String> violations = new HashSet<>();
            for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
                violations.add(constraintViolation.getMessage());
            }
            throw new ApplicationConstraintViolationException("Object validation failed.", violations);
        } catch (ApplicationBaseException applicationBaseException) {
            throw applicationBaseException;
        } catch (Throwable throwable) {
            throw new IntervalServerError(throwable.getMessage());
        }
    }
}
