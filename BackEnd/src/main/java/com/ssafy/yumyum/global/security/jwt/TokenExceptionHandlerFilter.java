package com.ssafy.yumyum.global.security.jwt;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;
import com.ssafy.yumyum.global.response.ResponseBody;
import com.ssafy.yumyum.global.response.ResponseUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenExceptionHandlerFilter extends OncePerRequestFilter {
    private static final String ALLOWED_METHODS = String.join(", ", 
            HttpMethod.GET.name(), 
            HttpMethod.POST.name(), 
            HttpMethod.PUT.name(), 
            HttpMethod.PATCH.name(), 
            HttpMethod.DELETE.name(), 
            HttpMethod.OPTIONS.name()
    );

    private static final String ALLOWED_HEADERS = String.join(", ", 
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_TYPE
    );

    private static final String VARY_HEADERS = String.join(", ",
            HttpHeaders.ORIGIN,
            HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
            HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BusinessException e) {
            handleBusinessException(request, response, e);
        }
    }
    
    private void handleBusinessException(HttpServletRequest request, HttpServletResponse response, BusinessException e) throws IOException {
        ExceptionType exceptionType = e.getExceptionType();
        response.setStatus(exceptionType.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8);

        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin == null || origin.isEmpty()) {
            origin = "http://localhost:8080";
        }

        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE.toString());
        response.setHeader(HttpHeaders.VARY, VARY_HEADERS);
        
        ResponseBody<Void> body = ResponseUtil.createFailureResponse(exceptionType);
        writeErrorResponse(response, body);
    }

    private void writeErrorResponse(HttpServletResponse response, ResponseBody<Void> body) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(body);
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(json);
            writer.flush();
        }
    }
}
