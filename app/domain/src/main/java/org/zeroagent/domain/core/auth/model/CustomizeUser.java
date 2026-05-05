package org.zeroagent.domain.core.auth.model;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.zeroagent.common.model.OperatorSource;

import java.util.Collection;

/**
 * 自定义实现Security User模型
 * @author Nuk3m1
 * @version 2026年04月27日  16时06分
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class CustomizeUser extends User {
    /**
     * 用户uid
     */
    private final Long   uid;
    /**
     * 客户端: appKey
     */
    private       String appKey;
    /**
     * 客户端:appKeyName
     */
    private       String appKeyName;
    /**
     * 手机号
     */
    private       String phone;
    /**
     * 邮箱
     */
    private       String email;
    /**
     * 请求来源
     */
    private       OperatorSource operatorSource;


    public CustomizeUser(Long uid, String userName, String password, Collection<? extends GrantedAuthority> authorities) {
        super(userName, password, authorities);
        this.uid = uid;
    }


}
