package org.zeroagent.domain.common.utils.crypto;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.zeroagent.domain.common.env.RuntimeEnv;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月27日  17时45分
 */
@Component
@RequiredArgsConstructor
public class AuthTokenHelper {
    private static final String CLAIM_TYPE              = "token_type";
    private static final String CLAIM_TYPE_ANONYMOUS    = "anonymous";

    private final RuntimeEnv runtimeEnv;

    private ZonedDateTime getExpireTime(ZonedDateTime now) {
        if (runtimeEnv.isProd()) {
            // 生产环境失效时间， 与缓存失效时间无关
            return now.plusDays(3);
        }
        // 非生产环境 可以设置长期有效
        return now.plusDays(3);
    }

    public String generateToken(String userName) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expireTime = this.getExpireTime(now);
        return Jwts.builder()
                .subject(userName)
                .claim(JwtUtils.CLAIM_USERNAME, userName)
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(expireTime.toInstant()))
                .signWith(JwtUtils.FINAL_SECRET_KEY)
                .compact();
    }
}
