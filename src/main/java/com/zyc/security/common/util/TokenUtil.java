package com.zyc.security.common.util;


import com.zyc.security.common.constant.Security;
import com.zyc.security.common.constant.StringConstant;
import com.zyc.security.common.constant.enums.RedisKey;
import com.zyc.security.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * token工具类
 *
 * @author zyc
 */
@Slf4j
public class TokenUtil {

    private static final RedisUtil REDIS_UTIL = SpringUtil.getBean(RedisUtil.class);

    public static void generateToken(User user) {
        String token = Jwts.builder()
                .setHeaderParam(StringConstant.TYP, StringConstant.JWT)
                .claim(StringConstant.USER_ID, user.getId())
                .claim(StringConstant.MARK, user.getMark())
                .signWith(SignatureAlgorithm.HS512, Security.KEY)
                .compact();
        user.setToken(token);
        REDIS_UTIL.set(user.getId(), user, RedisKey.USER);
    }

    public static User getUser(String token) {
        log.info("token:" + token);
        return Optional
                .ofNullable(token)
                .filter(StringUtils::isNotBlank)
                .map(s -> {
                    String userId = parseClaim(s, StringConstant.USER_ID, String.class);
                    return REDIS_UTIL.get(userId, RedisKey.USER, User.class);
                })
                .orElseThrow(() -> new RuntimeException("缺失用户信息"));
    }

    public static <T> T parseClaim(String token, String claim, Class<T> clazz) {
        return Jwts.parser()
                .setSigningKey(Security.KEY)
                .parseClaimsJws(token)
                .getBody()
                .get(claim, clazz);
    }
}
