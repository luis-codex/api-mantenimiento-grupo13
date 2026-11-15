package ec.edu.espe.mantenimiento.middleware;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

public class HttpLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE =
            HttpLoggingInterceptor.class.getName() + ".startTime";
    private static final String RESPONSE_TIME_HEADER = "X-Response-Time";

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        String clientId = request.getHeader("X-Client-Id");
        if (clientId != null && clientId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "X-Client-Id no puede estar vacío");
            return false;
        }

        request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception exception) {
        Object start = request.getAttribute(START_TIME_ATTRIBUTE);
        long elapsedMillis = start instanceof Long startTime
                ? TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
                : 0L;

        response.setHeader(RESPONSE_TIME_HEADER, elapsedMillis + " ms");
        log.info(
                "HTTP {} {} desde {} -> {} ({} ms)",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                response.getStatus(),
                elapsedMillis);

        if (exception != null) {
            log.warn("La solicitud {} terminó con error", request.getRequestURI(), exception);
        }
    }
}
