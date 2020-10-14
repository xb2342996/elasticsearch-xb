package com.xxbb.elasticsearch.repositories;

import com.xxbb.elasticsearch.entities.Person;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PersonRepositoryTest {
    @Resource
    private PersonRepository repository;

    @Resource
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    public void testSaveAll() {
        List<Person> persons = createPersonEntities(20);
        repository.saveAll(persons);
    }

    @Test
    public void testDelete() {
        repository.deleteAll();
    }

    private List<Person> createPersonEntities(int numberOfEntity) {
        List<Person> list = new ArrayList<>();
        for (int i = 0; i < numberOfEntity; i++) {
            Person person = new Person();
            person.setId((long)i);
            person.setName("P" + i);
            person.setCreateTime(new Date());
            list.add(person);
        }
        return list;
    }
}