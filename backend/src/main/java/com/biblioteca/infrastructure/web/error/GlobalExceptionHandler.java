package com.biblioteca.infrastructure.web.error;

import com.biblioteca.shared.domain.BusinessRuleException;
import com.biblioteca.shared.domain.DomainException;
import com.biblioteca.shared.domain.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail noEncontrado(NotFoundException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail reglaDeNegocio(BusinessRuleException ex) {
        return problema(HttpStatus.CONFLICT, "Regla de negocio", ex);
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail dominio(DomainException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Datos invalidos", ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "La peticion tiene campos invalidos");
        problema.setTitle("Validacion");
        problema.setProperty("codigo", "VALIDACION");
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ProblemDetail peticionInvalida(Exception ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "La peticion no se pudo interpretar");
        problema.setTitle("Peticion invalida");
        problema.setProperty("codigo", "PETICION_INVALIDA");
        return problema;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception ex) {
        log.error("Error no controlado", ex);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado");
        problema.setTitle("Error interno");
        problema.setProperty("codigo", "ERROR_INTERNO");
        return problema;
    }

    private ProblemDetail problema(HttpStatus status, String titulo, DomainException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problema.setTitle(titulo);
        problema.setProperty("codigo", ex.codigo());
        return problema;
    }
}
