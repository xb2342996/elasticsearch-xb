package com.xxbb.springframework.data.elasticsearch.repository.query;

import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.SearchHit;
import com.xxbb.springframework.data.elasticsearch.core.SearchHitSupport;
import com.xxbb.springframework.data.elasticsearch.core.SearchHits;
import com.xxbb.springframework.data.elasticsearch.core.convert.DateTimeConverters;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.core.query.StringQuery;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableInstant;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.util.StreamUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.NumberUtils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElasticsearchStringQuery extends AbstractElasticsearchRepositoryQuery{

    private static final Pattern PARAMETER_PLACEHOLDER = Pattern.compile("\\?(\\d+)");
    private String query;

    private final GenericConversionService conversionService = new GenericConversionService();

    {
        if (!conversionService.canConvert(Date.class, String.class)) {
            conversionService.addConverter(DateTimeConverters.JavaDateConverter.INSTANCE);
        }
        if (ClassUtils.isPresent("org.joda.time.DateTimeZone", ElasticsearchStringQuery.class.getClassLoader())) {
            if (!conversionService.canConvert(ReadableInstant.class, String.class)) {
                conversionService.addConverter(DateTimeConverters.JodaDateTimeConverter.INSTANCE);
            }
            if (!conversionService.canConvert(LocalDateTime.class, String.class)) {
                conversionService.addConverter(DateTimeConverters.JodaLocalDateTimeConverter.INSTANCE);
            }
        }
    }

    public ElasticsearchStringQuery(ElasticsearchQueryMethod queryMethod, ElasticsearchOperations elasticsearchOperations, String query) {
        super(queryMethod, elasticsearchOperations);
        Assert.notNull(query, "query must not be null");
        this.query = query;
    }

    @Override
    public Object execute(Object[] parameters) {
        Class<?> clazz = queryMethod.getEntityInformation().getJavaType();
        ParametersParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);

        StringQuery stringQuery = createQuery(accessor);

        Assert.notNull(stringQuery, "unsupported query");

        if (queryMethod.hasAnnotatedHighlight()) {
            stringQuery.setHighlightQuery(queryMethod.getAnnotatedHighlightQuery());
        }

        IndexCoordinates index = elasticsearchOperations.getIndexCoordinatesFor(clazz);

        Object result = null;
        if (queryMethod.isPageQuery()) {
            stringQuery.setPageable(accessor.getPageable());
            SearchHits<?> searchHits = elasticsearchOperations.search(stringQuery, clazz, index);
            result = SearchHitSupport.page(searchHits, stringQuery.getPageable());
        } else if (queryMethod.isStreamQuery()) {
            if (accessor.getPageable().isUnpaged()) {
                stringQuery.setPageable(PageRequest.of(0, DEFAULT_STREAM_BATCH_SIZE));
            } else {
                stringQuery.setPageable(accessor.getPageable());
            }
            result = StreamUtils.createStreamFromIterator(elasticsearchOperations.searchForStream(stringQuery, clazz, index));
        } else if (queryMethod.isCollectionQuery()) {
            if (accessor.getPageable().isPaged()) {
                stringQuery.setPageable(accessor.getPageable());
            }
            result = elasticsearchOperations.search(stringQuery, clazz, index);
        } else {
            result = elasticsearchOperations.searchOne(stringQuery, clazz, index);
        }
        return queryMethod.isNotSearchHitMethod() ? SearchHitSupport.unwrapSearchHits(result) : result;
    }

    protected StringQuery createQuery(ParametersParameterAccessor parameterAccessor) {
        String queryString = replacePlaceHolder(this.query, parameterAccessor);
        return new StringQuery(queryString);
    }

    private String replacePlaceHolder(String input, ParametersParameterAccessor accessor) {
        Matcher matcher = PARAMETER_PLACEHOLDER.matcher(input);
        String result = input;

        while (matcher.find()) {
            String placeholder = Pattern.quote(matcher.group()) + "(?!\\d+)";
            int index = NumberUtils.parseNumber(matcher.group(1), Integer.class);
            result = result.replaceAll(placeholder, getParameterWithIndex(accessor, index));
        }
        return result;
    }

    private String getParameterWithIndex(ParametersParameterAccessor accessor, int index) {
        Object parameter = accessor.getBindableValue(index);

        if (parameter == null) {
            return "null";
        }
        if (conversionService.canConvert(parameter.getClass(), String.class)) {
            return conversionService.convert(parameter, String.class);
        }
        return parameter.toString();
    }
}
