package com.jlwl.controller;

import com.jlwl.common.R;
import com.jlwl.entity.TokenEntity;
import com.jlwl.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return R.fail("账号或密码不能为空");
        }
        TokenEntity token = usersService.login(username, password);
        if (token == null) {
            return R.fail(401, "账号或密码错误");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("token", token.getToken());
        data.put("userId", token.getUserId());
        data.put("username", token.getUsername());
        data.put("userType", token.getUserType());
        data.put("expireTime", token.getExpireTime());
        return R.ok(data);
    }

    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest req) {
        String token = req.getHeader("token");
        if (token != null) usersService.logout(token);
        return R.ok();
    }

    @GetMapping("/session-test")
    public R<String> sessionTest() {
        return R.ok("ok");
    }

    @GetMapping("/info")
    public R<Map<String, Object>> info(HttpServletRequest req) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", req.getAttribute("currentUserId"));
        data.put("username", req.getAttribute("currentUsername"));
        data.put("userType", req.getAttribute("currentUserType"));
        return R.ok(data);
    }
}
