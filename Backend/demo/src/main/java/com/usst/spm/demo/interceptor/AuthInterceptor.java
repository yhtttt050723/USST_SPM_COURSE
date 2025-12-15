package com.usst.spm.demo.interceptor;

import com.usst.spm.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 跨域 OPTIONS 请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取 Header 中的 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.getWriter().write("header 缺失或无效");
            return false;
        }

        // 获取 Token
        String token = authHeader.substring(7);

        try {
            // 验证 Token
            Claims claims = jwtUtil.extractAllClaims(token);
            String studentNo = claims.getSubject();

            if (jwtUtil.isTokenExpired(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                response.getWriter().write("Token expired.");
                return false;
            }

            // Token 有效，将用户学号存入 Request 属性中，供后续 Controller 使用
            request.setAttribute("studentNo", studentNo);

            return true;
        } catch (JwtException e) {
            // Token 解析失败
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.getWriter().write("Invalid token: " + e.getMessage());
            return false;
        }
    }
}