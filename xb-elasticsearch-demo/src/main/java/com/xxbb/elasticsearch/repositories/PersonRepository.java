package com.xxbb.elasticsearch.repositories;

import com.xxbb.elasticsearch.entities.Person;
import com.xxbb.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PersonRepository extends ElasticsearchRepository<Person, Long> {
}
