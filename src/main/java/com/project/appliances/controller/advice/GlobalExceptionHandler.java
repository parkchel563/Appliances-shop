package com.project.appliances.controller.advice;

import com.project.appliances.constants.MessageKeys;
import com.project.appliances.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public String handle404(Exception ex, HttpServletRequest request, Model model) {
        log.warn("HTTP 404 | Path '{}' not found", request.getRequestURI());

        return buildErrorPage(
                model,
                request,
                404,
                getMessage(MessageKeys.PAGE_NOT_FOUND)
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex,
                                         HttpServletRequest request,
                                         Model model) {
        log.warn("HTTP 404 | Resource not found on '{}': {}", request.getRequestURI(), ex.getMessageKey());

        return buildErrorPage(
                model,
                request,
                404,
                getMessage(ex.getMessageKey(), ex.getArgs())
        );
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public String handle403(AccessDeniedException ex, HttpServletRequest request, Model model) {
        log.warn("HTTP 403 | Access denied to '{}'", request.getRequestURI());

        return buildErrorPage(
                model,
                request,
                403,
                getMessage(MessageKeys.ACCESS_DENIED)
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String handle500(Exception ex, HttpServletRequest request, Model model) {
        log.error("HTTP 500 | Unexpected error on '{}'", request.getRequestURI(), ex);

        return buildErrorPage(
                model,
                request,
                500,
                getMessage(MessageKeys.UNEXPECTED_ERROR)
        );
    }

    private String buildErrorPage(Model model,
                                  HttpServletRequest request,
                                  int status,
                                  String message) {
        model.addAttribute("status", status);
        model.addAttribute("message", message);
        model.addAttribute("path", request.getRequestURI());
        return "error/error";
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
