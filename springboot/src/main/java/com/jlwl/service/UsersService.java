package com.jlwl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jlwl.dao.TokenDao;
import com.jlwl.dao.UsersDao;
import com.jlwl.entity.TokenEntity;
import com.jlwl.entity.UsersEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersDao usersDao;
    private final TokenDao tokenDao;

    @Value("${oa.token.expire-hours:12}")
    private int expireHours;

    /** 登录 - 校验账号密码，生成 token 写入数据库。 */
    @Transactional
    public TokenEntity login(String username, String password) {
        UsersEntity user = usersDao.selectOne(
            new QueryWrapper<UsersEntity>()
                .eq("username", username)
                .eq("password", password)
                .eq("del_flag", 0)
        );
        if (user == null) return null;

        TokenEntity token = new TokenEntity();
        token.setUserId(user.getId());
        token.setUsername(user.getUsername());
        token.setUserType("admin");
        token.setToken(UUID.randomUUID().toString().replace("-", ""));
        token.setExpireTime(LocalDateTime.now().plusHours(expireHours));
        token.setCreateTime(LocalDateTime.now());
        tokenDao.insert(token);
        return token;
    }

    public void logout(String token) {
        tokenDao.delete(new QueryWrapper<TokenEntity>().eq("token", token));
    }

    public TokenEntity validateToken(String token) {
        List<TokenEntity> list = tokenDao.selectList(
            new QueryWrapper<TokenEntity>()
                .eq("token", token)
                .gt("expire_time", LocalDateTime.now())
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public IPage<UsersEntity> page(int page, int limit, String username) {
        QueryWrapper<UsersEntity> qw = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) qw.like("username", username);
        qw.eq("del_flag", 0).orderByDesc("id");
        return usersDao.selectPage(new Page<>(page, limit), qw);
    }

    public UsersEntity get(Long id) {
        return usersDao.selectById(id);
    }

    public boolean save(UsersEntity u) {
        if (u.getId() == null) return usersDao.insert(u) > 0;
        return usersDao.updateById(u) > 0;
    }

    public boolean delete(Long id) {
        return usersDao.update(null,
            new UpdateWrapper<UsersEntity>().eq("id", id).set("del_flag", 1)
        ) > 0;
    }
}
