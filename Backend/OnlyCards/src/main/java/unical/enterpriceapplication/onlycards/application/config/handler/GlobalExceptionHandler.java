package unical.enterpriceapplication.onlycards.application.config.handler;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.dto.ServiceError;
import unical.enterpriceapplication.onlycards.application.exception.BadRequestException;
import unical.enterpriceapplication.onlycards.application.exception.ConflictException;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.MissingParametersException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceAlreadyExistsException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceInUseException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;
import unical.enterpriceapplication.onlycards.application.exception.UnsupportedMediaTypeException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ServiceError defaultErrorHandler(WebRequest req, Exception ex) {
        // Log the exception details
        log.warn("Unhandled exception occurred: {}", ex.getMessage(), ex);
        return errorResponse(req, "Internal server error",ex);
    }
    @ExceptionHandler(UnexpectedTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onUnexpectedTypeException(WebRequest req, UnexpectedTypeException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, "Invalid data provided",ex);
    }
    @ExceptionHandler(HttpMediaTypeException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @Hidden
    public ServiceError onHttpMediaTypeException(WebRequest req, HttpMediaTypeException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(),ex);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onIllegalArgumentException(WebRequest req, IllegalArgumentException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, "Invalid data provided",ex);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onMethodArgumentTypeMismatchException(WebRequest req, MethodArgumentTypeMismatchException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, "Invalid data provided",ex);
    }
    @ExceptionHandler(LimitExceedException.class)
    @Hidden
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ServiceError onLimitExceedException(WebRequest req, LimitExceedException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(),ex);
    }
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onInvalidDataAccessApiUsageException(WebRequest req, InvalidDataAccessApiUsageException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, "Invalid data provided",ex);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    @Hidden
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ServiceError onNoResourceFoundException(WebRequest req, ResourceNotFoundException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(),ex);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @Hidden
    public ServiceError onNoResourceFoundException(WebRequest req, NoResourceFoundException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, "Resource not found",ex);
    }
    @Hidden
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ServiceError onUnauthorizedUser(WebRequest req, AccessDeniedException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(),ex);
    }
    @ExceptionHandler(ResourceInUseException.class)
    @Hidden
    @ResponseStatus(HttpStatus.CONFLICT)
    public ServiceError onResourceInUseException(WebRequest req, ResourceInUseException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(),ex);
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onMissingServletRequestParameterException(WebRequest req, MissingServletRequestParameterException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(),ex);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @Hidden
    @ResponseStatus(HttpStatus.CONFLICT)
    public ServiceError onResourceAlreadyExistsException(WebRequest req, ResourceAlreadyExistsException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(),ex);
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @Hidden
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ServiceError onHttpRequestMethodNotSupportedException(WebRequest req, HttpRequestMethodNotSupportedException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, "Method not allowed",ex);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onDataIntegrityViolationException(WebRequest req, DataIntegrityViolationException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, "invalid data provided", ex);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onHttpMessageNotReadableException(WebRequest req, HttpMessageNotReadableException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, "invalid data provided",  ex);
    }
    @ExceptionHandler(InsufficientAuthenticationException.class)
    @Hidden
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ServiceError onInsufficientAuthenticationException(WebRequest req, InsufficientAuthenticationException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, "Wrong or missing credentials", ex);
    }
    @ExceptionHandler(MissingParametersException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onMissingParametersException(WebRequest req, MissingParametersException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(), ex);
    }

    @ExceptionHandler(UnsupportedMediaTypeException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @Hidden    
    public ServiceError onUnsupportedMediaTypeException(WebRequest req, UnsupportedMediaTypeException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(), ex);
    }
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onIllegalStateException(WebRequest req, IllegalStateException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(), ex);
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @Hidden
    public ServiceError onMaxUploadSizeExceededException(WebRequest req, MaxUploadSizeExceededException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, "File size too large", ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onConstraintViolationException(WebRequest req, ConstraintViolationException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(), ex);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onValidationException(WebRequest req, MethodArgumentNotValidException ex) {
          // Estrai i messaggi di errore di validazione
          List<String> errorMessages = ex.getBindingResult()
          .getFieldErrors()
          .stream()
          .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
          .map(msg -> msg.replace("[", "").replace("]", "")) 
          .collect(Collectors.toList());
    if (errorMessages.isEmpty()) {
    errorMessages.add("Invalid data provided");
}
    // Logga solo i messaggi di errore
    log.info("Validation errors: " + String.join(", ", errorMessages));
    
    // Restituisci una risposta contenente gli errori
    return errorResponse(req, String.join(", ", errorMessages), ex);
    }
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServiceError onBadRequestException(WebRequest req, BadRequestException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(), ex);
    }
    @ExceptionHandler(ConflictException.class)
    @Hidden
    @ResponseStatus(HttpStatus.CONFLICT)
    public ServiceError onConflictException(WebRequest req, ConflictException ex) {
        log.info(ex.getMessage());
        return errorResponse(req, ex.getMessage(), ex);
    }



    private ServiceError errorResponse(WebRequest req, String message, Exception exception) {
        HttpServletRequest httpReq = (HttpServletRequest) req.resolveReference(WebRequest.REFERENCE_REQUEST);
        // Log the request URI and the error message
        String uri = httpReq.getRequestURI();
        String exceptionClass = exception.getClass().getName();
        log.info("Exception {} on URI: {}, message: {}", exceptionClass, uri, message);
        return new ServiceError(new Date(), uri, message);
    }
}
