package com.usst.spm.demo.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理所有异常，并在控制台输出详细日志
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常（ResponseStatusException）
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {
        
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        
        // 在控制台输出详细错误信息
        logger.error("========== 业务异常 ==========");
        logger.error("请求路径: {}", request.getRequestURI());
        logger.error("请求方法: {}", request.getMethod());
        logger.error("HTTP状态码: {}", status.value());
        logger.error("错误消息: {}", message);
        logger.error("异常类型: {}", ex.getClass().getName());
        logger.error("堆栈跟踪:", ex);
        logger.error("==============================");
        
        Map<String, Object> errorResponse = buildErrorResponse(
                status.value(),
                message,
                request.getRequestURI()
        );
        
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * 处理参数验证异常（MethodArgumentNotValidException）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String message = "参数验证失败";
        if (ex.getBindingResult().hasFieldErrors()) {
            message = ex.getBindingResult().getFieldError().getDefaultMessage();
        }
        
        logger.error("========== 参数验证异常 ==========");
        logger.error("请求路径: {}", request.getRequestURI());
        logger.error("请求方法: {}", request.getMethod());
        logger.error("错误消息: {}", message);
        logger.error("验证错误详情: {}", ex.getBindingResult().getAllErrors());
        logger.error("异常类型: {}", ex.getClass().getName());
        logger.error("堆栈跟踪:", ex);
        logger.error("==================================");
        
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理参数异常（IllegalArgumentException）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        logger.error("========== 参数异常 ==========");
        logger.error("请求路径: {}", request.getRequestURI());
        logger.error("请求方法: {}", request.getMethod());
        logger.error("错误消息: {}", ex.getMessage());
        logger.error("异常类型: {}", ex.getClass().getName());
        logger.error("堆栈跟踪:", ex);
        logger.error("==============================");
        
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage() != null ? ex.getMessage() : "参数错误",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理空指针异常（NullPointerException）
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointerException(
            NullPointerException ex, HttpServletRequest request) {
        
        logger.error("========== 空指针异常 ==========");
        logger.error("请求路径: {}", request.getRequestURI());
        logger.error("请求方法: {}", request.getMethod());
        logger.error("错误消息: {}", ex.getMessage());
        logger.error("异常类型: {}", ex.getClass().getName());
        logger.error("堆栈跟踪:", ex);
        logger.error("================================");
        
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "服务器内部错误：空指针异常",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理数据库访问异常（DataAccessException）
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request) {
        
        logger.error("========== 数据库访问异常 ==========");
        logger.error("请求路径: {}", request.getRequestURI());
        logger.error("请求方法: {}", request.getMethod());
        logger.error("错误消息: {}", ex.getMessage());
        logger.error("异常类型: {}", ex.getClass().getName());
        logger.error("堆栈跟踪:", ex);
        logger.error("====================================");
        
        // 检查是否是SQL异常
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof SQLException) {
            SQLException sqlEx = (SQLException) rootCause;
            logger.error("SQL状态码: {}", sqlEx.getSQLState());
            logger.error("错误代码: {}", sqlEx.getErrorCode());
        }
        
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "数据库操作失败",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理SQL异常（SQLException）
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleSQLException(
            SQLException ex, HttpServletRequest request) {
        
        logger.error("========== SQL异常 ==========");
        logger.error("请求路径: {}", request.getRequestURI());
        logger.error("请求方法: {}", request.getMethod());
        logger.error("SQL状态码: {}", ex.getSQLState());
        logger.error("错误代码: {}", ex.getErrorCode());
        logger.error("错误消息: {}", ex.getMessage());
        logger.error("异常类型: {}", ex.getClass().getName());
        logger.error("堆栈跟踪:", ex);
        logger.error("==============================");
        
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "数据库操作失败: " + ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理JWT异常（JwtException）
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(
            JwtException ex, HttpServletRequest request) {
        
        logger.error("========== JWT异常 ==========");
        logger.error("请求路径: {}", request.getRequestURI());
        logger.error("请求方法: {}", request.getMethod());
        logger.error("错误消息: {}", ex.getMessage());
        logger.error("异常类型: {}", ex.getClass().getName());
        logger.error("堆栈跟踪:", ex);
        logger.error("==============================");
        
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Token验证失败: " + ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 处理所有其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("========== 未捕获的异常 ==========");
        logger.error("请求路径: {}", request.getRequestURI());
        logger.error("请求方法: {}", request.getMethod());
        logger.error("错误消息: {}", ex.getMessage());
        logger.error("异常类型: {}", ex.getClass().getName());
        logger.error("堆栈跟踪:", ex);
        logger.error("==================================");
        
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "服务器内部错误: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 构建统一的错误响应格式
     */
    private Map<String, Object> buildErrorResponse(int status, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status);
        errorResponse.put("error", HttpStatus.valueOf(status).getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }
}

