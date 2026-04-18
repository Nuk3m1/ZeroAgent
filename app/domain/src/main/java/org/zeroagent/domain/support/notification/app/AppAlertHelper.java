package org.zeroagent.domain.support.notification.app;

import org.zeroagent.common.utils.json.JSON;

/**
 * APP通知接口，可挂载  飞书、微信、QQ等信息通知渠道
 * @author Nuk3m1
 * @version 2026年04月09日  14时05分
 */

public interface AppAlertHelper {
    default void alert(String title, AlertMessage message) {
        String template = title + "\n{}";
        this.alertText(template, JSON.toPrettyJSONString(message));
    }

    default void alert(String title, AlertMessage message, Throwable e) {
        String template = title + "\n{}";
        this.alertText(template, JSON.toPrettyJSONString(message), e);
    }

    void alertText(String template, Object... args);
}
