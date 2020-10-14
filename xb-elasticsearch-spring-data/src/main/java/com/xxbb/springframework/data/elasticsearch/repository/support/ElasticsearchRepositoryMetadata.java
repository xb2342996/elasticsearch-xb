package com.xxbb.springframework.data.elasticsearch.repository.support;

import com.xxbb.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.stream.Stream;

public class ElasticsearchRepositoryMetadata extends DefaultRepositoryMetadata {

    public ElasticsearchRepositoryMetadata(Class<?> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    public Class<?> getReturnedDomainClass(Method method) {

        Class<?> returnedDomainClass = super.getReturnedDomainClass(method);

        if (SearchHit.class.isAssignableFrom(returnedDomainClass)) {
            try {
                ParameterizedType methodGenericReturnType = ((ParameterizedType) method.getGenericReturnType());
                if (isAllowedGenericType(methodGenericReturnType)) {
                    ParameterizedType collectionTypeArgument = (ParameterizedType) methodGenericReturnType.getActualTypeArguments()[0];
                    if (SearchHit.class.isAssignableFrom((Class<?>) collectionTypeArgument.getRawType())) {
                        returnedDomainClass = (Class<?>) collectionTypeArgument.getActualTypeArguments()[0];
                    }
                }
            }catch (Exception ignored) {}
        }

        return returnedDomainClass;
    }

    protected boolean isAllowedGenericType(ParameterizedType methodGenericReturnType) {
        return Collection.class.isAssignableFrom((Class<?>) methodGenericReturnType.getRawType())
                || Stream.class.isAssignableFrom((Class<?>) methodGenericReturnType.getRawType());
    }
}
