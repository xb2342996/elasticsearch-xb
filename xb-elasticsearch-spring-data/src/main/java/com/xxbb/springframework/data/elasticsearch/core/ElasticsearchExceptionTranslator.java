package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.NoSuchIndexException;
import com.xxbb.springframework.data.elasticsearch.UncategorizedElasticsearchException;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.common.ValidationException;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.rest.RestStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ElasticsearchExceptionTranslator implements PersistenceExceptionTranslator{
    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException e) {
        if (isSeqNoConflict(e)) {
            return new OptimisticLockingFailureException("Cannot index a document due to seq_no+primary_term conflict", e);
        }

        if (e instanceof ElasticsearchException) {
            ElasticsearchException elasticsearchException = (ElasticsearchException) e;
            if (!indexAvailable(elasticsearchException)) {
                return new NoSuchIndexException(ObjectUtils.nullSafeToString(elasticsearchException.getMetadata("es.index")), e);
            }
            return new UncategorizedElasticsearchException(e.getMessage(), e);
        }

        if (e instanceof ValidationException) {
            return new DataIntegrityViolationException(e.getMessage(), e);
        }
        Throwable cause = e.getCause();
        if (cause instanceof IOException) {
            return new DataAccessResourceFailureException(e.getMessage(), e);
        }
        return null;
    }

    private boolean isSeqNoConflict(Exception exception) {
        if (exception instanceof ElasticsearchStatusException) {
            ElasticsearchStatusException statusException = (ElasticsearchStatusException) exception;

            return statusException.status() == RestStatus.CONFLICT && statusException.getMessage() != null
                    && statusException.getMessage().contains("type=version_conflict_engine_exception")
                    && statusException.getMessage().contains("version conflict, required seqNo");
        }

        if (exception instanceof VersionConflictEngineException) {
            VersionConflictEngineException versionConflictEngineException = (VersionConflictEngineException) exception;
            return versionConflictEngineException.getMessage() != null
                    && versionConflictEngineException.getMessage().contains("version conflict, required seqNo");
        }

        return false;
    }

    private boolean indexAvailable(ElasticsearchException ex) {
        List<String> metadata = ex.getMetadata("es.index_uuid");
        if (metadata == null) {
            if (ex instanceof ElasticsearchStatusException) {
                return StringUtils.hasText(ObjectUtils.nullSafeToString(ex.getIndex()));
            }
            return false;
        }
        return !CollectionUtils.contains(metadata.iterator(), "_na_");
    }
}
