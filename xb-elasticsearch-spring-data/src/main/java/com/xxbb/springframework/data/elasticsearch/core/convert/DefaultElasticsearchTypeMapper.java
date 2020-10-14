package com.xxbb.springframework.data.elasticsearch.core.convert;

import org.springframework.data.convert.DefaultTypeMapper;
import org.springframework.data.convert.SimpleTypeInformationMapper;
import org.springframework.data.convert.TypeAliasAccessor;
import org.springframework.data.convert.TypeInformationMapper;
import org.springframework.data.mapping.Alias;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.lang.Nullable;
import org.w3c.dom.TypeInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DefaultElasticsearchTypeMapper extends DefaultTypeMapper<Map<String, Object>> implements ElasticsearchTypeMapper{

    private final @Nullable String typeKey;

    public DefaultElasticsearchTypeMapper(@Nullable String typeKey) {
        this(typeKey, Collections.singletonList(new SimpleTypeInformationMapper()));
    }

    public DefaultElasticsearchTypeMapper(@Nullable String typeKey, MappingContext<? extends PersistentEntity<?, ?>, ?> mappingContext) {
        this(typeKey, new MapTypeAliasAccessor(typeKey), mappingContext, Collections.singletonList(new SimpleTypeInformationMapper()));
    }

    public DefaultElasticsearchTypeMapper(@Nullable String typeKey, List<? extends TypeInformationMapper> mappers) {
        this(typeKey, new MapTypeAliasAccessor(typeKey), null, mappers);
    }

    public DefaultElasticsearchTypeMapper(@Nullable String typeKey, TypeAliasAccessor<Map<String, Object>> accessor, MappingContext<? extends PersistentEntity<?, ?>, ?> mappingContext, List<? extends TypeInformationMapper> additionalMappers) {
        super(accessor, mappingContext, additionalMappers);
        this.typeKey = typeKey;
    }

    @Override
    public boolean isTypeKey(String key) {
        return false;
    }

    public static class MapTypeAliasAccessor implements TypeAliasAccessor<Map<String, Object>> {
        private final @Nullable String typeKey;

        public MapTypeAliasAccessor(@Nullable String typeKey) {
            this.typeKey = typeKey;
        }

        @Override
        public Alias readAliasFrom(Map<String, Object> source) {
            return Alias.ofNullable(source.get(typeKey));
        }

        @Override
        public void writeTypeTo(Map<String, Object> sink, Object o) {
            if (typeKey == null) {
                return;
            }
            sink.put(typeKey, o);
        }
    }
}
