package org.zeroagent.domain.core.grapherror.service;

import org.zeroagent.domain.core.grapherror.model.GraphErrorLog;

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
}
