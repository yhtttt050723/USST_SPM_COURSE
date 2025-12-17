package com.usst.spm.demo.Config;

import com.usst.spm.demo.interceptor.AuthInterceptor;
import com.usst.spm.demo.interceptor.TeacherAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final TeacherAuthInterceptor teacherAuthInterceptor;

    public WebConfig(AuthInterceptor authInterceptor, TeacherAuthInterceptor teacherAuthInterceptor) {
        this.authInterceptor = authInterceptor;
        this.teacherAuthInterceptor = teacherAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**") // 拦截所有 /api 路径下的请求
                // 排除认证相关的路径，允许用户在未登录时访问
                .excludePathPatterns("/api/auth/login", "/api/auth/register");

        // 教师权限拦截，依赖前置的 AuthInterceptor 已完成 token 校验
        registry.addInterceptor(teacherAuthInterceptor)
                .addPathPatterns("/api/**")
                // 登录注册不校验教师角色
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }
}