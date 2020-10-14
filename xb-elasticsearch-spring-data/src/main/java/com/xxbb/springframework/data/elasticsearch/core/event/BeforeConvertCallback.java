package com.xxbb.springframework.data.elasticsearch.core.event;

import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.mapping.callback.EntityCallback;

public interface BeforeConvertCallback<T> extends EntityCallback<T> {
    T onBeforeConvert(T entity, IndexCoordinates index);
}
