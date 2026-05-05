package org.zeroagent.domain.core.grapherror.service;

import org.zeroagent.domain.core.grapherror.model.GraphErrorLog;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLogStatus;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月18日  21时03分
 */
public interface GraphErrorLogRepository {
    long create(GraphErrorLog graphErrorLog);
    void update(GraphErrorLog graphErrorLog);
    Optional<GraphErrorLog> findById(long id);
    void batchInsert(List<GraphErrorLog> graphErrorLogs);
    List<GraphErrorLog> fetchBatchByStatus(int limit, GraphErrorLogStatus status);
    void updateStatusById(long id, GraphErrorLogStatus status);
}
