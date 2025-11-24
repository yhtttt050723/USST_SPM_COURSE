package com.usst.spm.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // 签名密钥
    private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 密钥有效期1小时
    private static final long EXPIRATION_TIME = 3600000;

    //生成 JWT Token
    //@param studentNo 用户学号
    //@param role 用户角色
    //@return 生成的 JWT 字符串
    public String generateToken(String studentNo, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(studentNo)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY)
                .compact();
    }

    //验证并解析 JWT Token
    //@param token JWT Token
    //@return Claims 对象，包含有效载荷
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //
    public String getStudentNoFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    //检查 Token 是否过期

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // 验证 Token 是否有效
    public boolean validateToken(String token, String studentNo) {
        final String tokenStudentNo = getStudentNoFromToken(token);
        return (tokenStudentNo.equals(studentNo) && !isTokenExpired(token));
    }
}