package org.zeroagent.infra.notification.alert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.tools.StringUtils;
import org.springframework.stereotype.Component;
import org.zeroagent.domain.support.notification.app.AppSyncAlertHelper;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月12日  14时07分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppSyncAlertHelperImpl implements AppSyncAlertHelper {


    @Override
    public void alertText(String template, Object... args) {
        if (StringUtils.isBlank(template)) {
            log.error("[alert] no content could be send !");
            return;
        }
        final Object[] messageArgs ;
        if (args != null && args.length > 0 && args[args.length - 1] instanceof Throwable) {
            messageArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, messageArgs, 0, args.length -1 );
        } else {
            messageArgs = args;
        }
        String format = "[alert]" + template;
        log.warn(format, messageArgs);
        // TODO 后续区分生产环境和开发环境，同时拓展飞书-微信-QQ等多渠道告警
    }
}
