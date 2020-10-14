package com.xxbb.springframework.data.elasticsearch.core.event;

import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.Ordered;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.util.Assert;

public class AuditingEntityCallback implements BeforeConvertCallback<Object>, Ordered {

    private final ObjectFactory<IsNewAwareAuditingHandler> auditingHandlerFactory;

    public AuditingEntityCallback(ObjectFactory<IsNewAwareAuditingHandler> auditingHandlerFactory) {
        Assert.notNull(auditingHandlerFactory, "IsNewAwareAuditingHandler must not be null");
        this.auditingHandlerFactory = auditingHandlerFactory;
    }

    @Override
    public Object onBeforeConvert(Object entity, IndexCoordinates index) {
        return auditingHandlerFactory.getObject().markAudited(entity);
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
