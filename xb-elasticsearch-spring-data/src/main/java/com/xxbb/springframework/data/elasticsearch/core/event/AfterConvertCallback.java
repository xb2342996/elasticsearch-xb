package com.xxbb.springframework.data.elasticsearch.core.event;

import com.xxbb.springframework.data.elasticsearch.core.document.Document;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.mapping.callback.EntityCallback;

public interface AfterConvertCallback<T> extends EntityCallback<T> {

    T onAfterConvert(T entity, Document document, IndexCoordinates index);
}
