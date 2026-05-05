package org.zeroagent.domain.common.utils.crypto;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKey;

/**
 *  JWT 工具类
 * @author Nuk3m1
 * @version 2026年04月27日  00时34分
 */
@Slf4j
public class JwtUtils {
    public static final String    CLAIM_CHANNEL       = "channel";
    public static final String    CLAIM_CHANNEL_QQ    = "QQ";
    public static final String    CLAIM_USERNAME      = "username";
    public static final String    SECRET_KEY          = "oa2wiX5CVShgoWA34ijok[qk[okwDSef16";
    public static final SecretKey FINAL_SECRET_KEY    = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    /**
     *  校验Token 合法且未过期
     * @param token -
     * @return -
     */
    public static boolean validateToken(String token) {
        try {
            JwtParser parser = Jwts.parser().verifyWith(FINAL_SECRET_KEY).build();
            parser.parseSignedClaims(token);
            return true;

        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT Token");
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT Token");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT Token");
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty");
        } catch (Exception e) {
            log.warn("there is an error with the signature of your token");
        }
        return false;
    }

    public static String getUserName(String token) {
        JwtParser parser = Jwts.parser().verifyWith(FINAL_SECRET_KEY).build();
        return parser.parseSignedClaims(token).getPayload().getSubject();
    }

    /**
     * 检查是否来自QQ登陆的Token
     * @param token -
     * @return -
     */
    public static boolean isQQLoginToken(String token) {
        Jws<Claims> jwsClaims = Jwts.parser()
                .verifyWith(FINAL_SECRET_KEY)
                .build().parseSignedClaims(token);
        String channel = jwsClaims.getPayload().get(CLAIM_CHANNEL, String.class);
        return StringUtils.equals(CLAIM_CHANNEL_QQ, channel);
    }

    public static String resolveQQUnionId(String token) {
        Jws<Claims> jwsClaims = Jwts.parser().verifyWith(FINAL_SECRET_KEY)
                .build().parseSignedClaims(token);
        return jwsClaims.getPayload().get(CLAIM_USERNAME, String.class);
    }
}
