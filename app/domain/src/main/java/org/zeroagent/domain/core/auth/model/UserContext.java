package org.zeroagent.domain.core.auth.model;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * @author chenhua
 * @version 2026年03月04日  13时21分
 * @Description:
 */

@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@With
public class UserContext {
    /**
     * 登录会话用户id
     */
    private Long             sessionUid;
    /**
     * 当前用户名字
     */
    private String           sessionUserName;
    /**
     * 当前用户手机号
     */
    private String           phone;
    /**
     * 当前用户邮箱
     */
    private String           email;

    public long getUid() {
        return this.sessionUid;
    }
    public String getUserName() {
        return this.sessionUserName;
    }
}
