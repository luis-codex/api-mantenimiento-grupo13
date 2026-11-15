package ec.edu.espe.mantenimiento.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceAuditAspect.class);

    @Around("execution(* ec.edu.espe.mantenimiento.service..*(..))")
    public Object auditServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        String operation = joinPoint.getSignature().toShortString();
        log.info("Auditoría: llamada a {}", operation);
        try {
            Object result = joinPoint.proceed();
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
            log.info("Auditoría: {} completada en {} ms", operation, elapsedMillis);
            return result;
        } catch (Throwable exception) {
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
            log.error("Auditoría: {} falló después de {} ms", operation, elapsedMillis, exception);
            throw exception;
        }
    }
}
