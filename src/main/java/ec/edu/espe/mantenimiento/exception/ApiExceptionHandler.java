package ec.edu.espe.mantenimiento.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }
    @ExceptionHandler({ConflictException.class, InvalidStateTransitionException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> conflict(RuntimeException ex, HttpServletRequest req) {
        String message = ex instanceof DataIntegrityViolationException ? "La operación entra en conflicto con datos existentes" : ex.getMessage();
        return response(HttpStatus.CONFLICT, message, req);
    }
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> businessRule(BusinessRuleException ex, HttpServletRequest req) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining("; "));
        return response(HttpStatus.BAD_REQUEST, message, req);
    }
    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> routeNotFound(NoResourceFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", req);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue(), req);
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> methodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "El método " + ex.getMethod() + " no está permitido en esta ruta", req);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest req) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno", req);
    }
    private ResponseEntity<ApiError> response(HttpStatus status, String message, HttpServletRequest req) {
        ApiError body = new ApiError(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
