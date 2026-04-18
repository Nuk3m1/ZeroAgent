package org.zeroagent.infra.dal.adaptor.cardgraph;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.zeroagent.domain.core.grapherror.model.GraphErrorLog;
import org.zeroagent.domain.core.grapherror.service.GraphErrorLogRepository;
import org.zeroagent.infra.dal.tables.daos.GraphSyncErrorLogDao;
import org.zeroagent.infra.dal.tables.pojos.GraphSyncErrorLog;
import org.zeroagent.infra.dal.tables.records.GraphSyncErrorLogRecord;

import java.util.Objects;
import java.util.Optional;

import static org.zeroagent.infra.dal.tables.GraphSyncErrorLog.GRAPH_SYNC_ERROR_LOG;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月18日  21时47分
 */
@Repository
@RequiredArgsConstructor
public class GraphErrorLogRepositoryImpl implements GraphErrorLogRepository {
    private final GraphErrorLogPojoMapper               graphErrorLogMapper;
    private final DSLContext                            dsl;
    private final GraphSyncErrorLogDao                  graphSyncErrorLogDao;



    @Override
    public long create(GraphErrorLog graphErrorLog) {
        GraphSyncErrorLog entity = graphErrorLogMapper.toEntity(graphErrorLog);
        graphSyncErrorLogDao.insert(entity);
        return Objects.requireNonNull(entity.getId());
    }

    @Override
    public void update(GraphErrorLog graphErrorLog) {
        GraphSyncErrorLogRecord updatingRecord = graphErrorLogMapper.toUpdatingRecord(graphErrorLog);
        if (!updatingRecord.changed()) {
            return;
        }
        dsl.update(GRAPH_SYNC_ERROR_LOG)
                .set(updatingRecord)
                .where(GRAPH_SYNC_ERROR_LOG.ID.eq(updatingRecord.getId()))
                .execute();
    }

    @Override
    public Optional<GraphErrorLog> findById(long id) {
        return graphSyncErrorLogDao.fetchOptionalById(id).map(graphErrorLogMapper::toModel);
    }
}
