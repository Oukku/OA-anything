package com.jlwl.interceptor;

import com.jlwl.common.R;
import com.jlwl.dao.TokenDao;
import com.jlwl.entity.TokenEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Token 拦截器 - 校验请求头里的 token，对应数据库 token 表。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final TokenDao tokenDao;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${oa.token.header:token}")
    private String tokenHeader;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String token = request.getHeader(tokenHeader);
        if (token == null || token.isEmpty()) {
            writeJson(response, 401, R.unauthorized());
            return false;
        }
        try {
            List<TokenEntity> tokens = tokenDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TokenEntity>()
                    .eq("token", token)
                    .gt("expire_time", LocalDateTime.now())
            );
            if (tokens == null || tokens.isEmpty()) {
                writeJson(response, 401, R.unauthorized());
                return false;
            }
            // 注入用户信息到 request attribute
            TokenEntity t = tokens.get(0);
            request.setAttribute("currentUserId", t.getUserId());
            request.setAttribute("currentUserType", t.getUserType());
            request.setAttribute("currentUsername", t.getUsername());
            return true;
        } catch (Exception e) {
            log.error("token check error", e);
            writeJson(response, 500, R.fail("token 校验失败"));
            return false;
        }
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
