package org.zeroagent.infra.dal;

import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 *
 *
 * @author Nuk3m1
 * @version 2026年03月09日  17时38分
 */
@Configuration
public class TransactionManagerConfiguration {
    // 设置事务超时时间为 5 秒
    @Bean
    public TransactionManagerCustomizer<DataSourceTransactionManager> transactionManagerCustomizer() {
        return transactionManager -> {transactionManager.setDefaultTimeout(5);};
    }
}
