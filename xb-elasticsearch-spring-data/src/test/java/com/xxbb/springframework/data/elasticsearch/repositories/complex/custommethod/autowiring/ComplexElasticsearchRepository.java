package com.xxbb.springframework.data.elasticsearch.repositories.complex.custommethod.autowiring;

import com.xxbb.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ComplexElasticsearchRepository extends ElasticsearchRepository<ComplexCustomMethodRepositoryTests.SampleEntity, String>, ComplexElasticsearchRepositoryCustom {
}
