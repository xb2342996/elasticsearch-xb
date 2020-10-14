package com.xxbb.springframework.data.elasticsearch.core.index;

import com.xxbb.springframework.data.elasticsearch.annotations.*;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;

public final class MappingParameters {
    static final String FIELD_PARAM_COEREC = "coerec";
    static final String FIELD_PARAM_COPY_TO = "copy_to";
    static final String FIELD_PARAM_DOC_VALUES = "doc_values";
    static final String FIELD_PARAM_DATA = "fielddata";
    static final String FIELD_PARAM_FORMAT = "format";
    static final String FIELD_PARAM_IGNORE_ABOVE = "ignore_above";
    static final String FIELD_PARAM_IGNORE_MALFORMED = "ignore_malformed";
    static final String FIELD_PARAM_INDEX = "index";
    static final String FIELD_PARAM_INDEX_OPTIONS = "index_options";
    static final String FIELD_PARAM_INDEX_PHRASES = "index_phrases";
    static final String FIELD_PARAM_INDEX_PREFIXES = "index_prefixes";
    static final String FIELD_PARAM_INDEX_PREFIXES_MIN_CHARS = "min_chars";
    static final String FIELD_PARAM_INDEX_PREFIXES_MAX_CHARS = "max_chars";
    static final String FIELD_PARAM_INDEX_ANALYZER = "analyzer";
    static final String FIELD_PARAM_NORMALIZER = "normalizer";
    static final String FIELD_PARAM_NORMS = "norms";
    static final String FIELD_PARAM_NULL_VALUE = "null_value";
    static final String FIELD_PARAMETER_NAME_POSITION_INCREMENT_GAP = "position_increment_gap";
    static final String FIELD_PARAM_SCALING_FACTOR = "scaling_factor";
    static final String FIELD_PARAM_SEARCH_ANALYZER = "search_analyzer";
    static final String FIELD_PARAM_STORE = "store";
    static final String FIELD_PARAM_SIMILARITY = "similarity";
    static final String FIELD_PARAM_TERM_VECTOR = "term_vector";
    static final String FIELD_PARAM_TYPE = "type";
    static final String FIELD_PARAM_MAX_SHINGLE_SIZE = "max_shingle_size";
    static final String FIELD_PARAM_POSITIVE_SCROE_IMPACT = "positive_score_impact";

    private boolean index = true;
    private boolean store = false;
    private boolean fielddata = false;
    private FieldType type = null;
    private DateFormat dateFormat = null;
    private String datePattern = null;
    private String analyzer = null;
    private String searchAnalyzer = null;
    private String normalizer = null;
    private String[] copyTo = null;
    private Integer ignoreAbove = null;
    private boolean coerce = true;
    private boolean docValues = true;
    private boolean ignoreMalformed = false;
    private IndexOptions indexOperations = null;
    boolean indexPhrases = false;
    private IndexPrefixes indexPrefixes = null;
    private boolean norms = true;
    private String nullValue = null;
    private Integer positionIncrementGap = null;
    private Similarity similarity = Similarity.Default;
    private TermVector termVector = TermVector.none;
    private double scalingFactor =  1.0;
    @Nullable private Integer maxShingleSize;
    private boolean positiveScoreImpact = true;

    public static MappingParameters from(Annotation annotation) {
        Assert.notNull(annotation, "annotation must not be null");

        if (annotation instanceof Field) {
            return new MappingParameters((Field) annotation);
        } else if (annotation instanceof InnerField) {
            throw new UnsupportedOperationException("@InnerField is not supported at this moment!");
        } else {
            throw new IllegalArgumentException("annotation must be an instance of @Field or @InnerField");
        }
    }

    public MappingParameters(Field field) {
        this.index = field.index();
        this.store = field.store();
        this.fielddata = field.fielddata();
        this.type = field.type();
        this.dateFormat = field.format();
        this.datePattern = field.pattern();
        this.analyzer = field.analyzer();
        this.searchAnalyzer = field.searchAnalyzer();
        this.normalizer = field.normalize();
        this.copyTo = field.copyTo();
//        this.ignoreAbove = ;
//        this.coerce = coerce;
//        this.docValues = docValues;
//        this.ignoreMalformed = ignoreMalformed;
//        this.indexOperations = indexOperations;
//        this.indexPhrases = indexPhrases;
//        this.indexPrefixes = indexPrefixes;
//        this.norms = norms;
//        this.nullValue = nullValue;
//        this.positionIncrementGap = positionIncrementGap;
//        this.similarity = similarity;
//        this.termVector = termVector;
//        this.scalingFactor = scalingFactor;
//        this.maxShingleSize = maxShingleSize;

//        Assert.isTrue(type != FieldType.Search_As_You_Type);
    }

    public boolean isStore() {
        return store;
    }

    public void writeTypeAndParametersTo(XContentBuilder builder) throws IOException {
        Assert.notNull(builder, "builder must not be null");

        if (fielddata) {
            builder.field(FIELD_PARAM_DATA, fielddata);
        }

        if (type != FieldType.Auto) {
            builder.field(FIELD_PARAM_TYPE, type.name().toLowerCase());
            if (type == FieldType.Date && dateFormat != DateFormat.none) {
                builder.field(FIELD_PARAM_FORMAT, dateFormat == DateFormat.custom ? datePattern : dateFormat.toString());
            }
        }

        if (!index) {
            builder.field(FIELD_PARAM_INDEX, index);
        }

        if (!StringUtils.isEmpty(analyzer)) {
            builder.field(FIELD_PARAM_INDEX_ANALYZER, analyzer);
        }

        if (!StringUtils.isEmpty(searchAnalyzer)) {
            builder.field(FIELD_PARAM_SEARCH_ANALYZER, searchAnalyzer);
        }

        if (!StringUtils.isEmpty(normalizer)) {
            builder.field(FIELD_PARAM_NORMALIZER, normalizer);
        }

        if (copyTo != null && copyTo.length > 0) {
            builder.field(FIELD_PARAM_COPY_TO, copyTo);
        }
    }
}
