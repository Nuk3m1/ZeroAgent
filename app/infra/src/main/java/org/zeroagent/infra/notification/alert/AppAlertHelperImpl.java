package org.zeroagent.infra.notification.alert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zeroagent.domain.common.async.AsyncTemplate;
import org.zeroagent.domain.support.notification.app.AppAlertHelper;
import org.zeroagent.domain.support.notification.app.AppSyncAlertHelper;

/**
 *
 * @author Nuk3m1
 * @version 2026年04月12日  14时06分
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppAlertHelperImpl implements AppAlertHelper {
    private final AppSyncAlertHelper appSyncAlertHelper;
    private final AsyncTemplate      asyncTemplate;
    @Override
    public void alertText(String template, Object... args) {

    }
}
