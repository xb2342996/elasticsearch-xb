package com.xxbb.springframework.data.elasticsearch.repository.query.keywords;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.annotations.Field;
import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.IndexOperations;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.SpringIntegrationTest;
import com.xxbb.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.xxbb.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.xxbb.springframework.data.elasticsearch.utils.IndexInitializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringIntegrationTest
@ContextConfiguration(classes = {QueryKeywordsTests.Config.class})
public class QueryKeywordsTests {

    @Configuration
    @Import({ElasticsearchRestTemplateConfiguration.class})
    @EnableElasticsearchRepositories(considerNestedRepositories = true)
    static class Config {
    }

    @Autowired
    private ProductRepository repository;

    @Autowired
    ElasticsearchOperations operations;
    private IndexOperations indexOperations;

    @BeforeEach
    public void before() {
        indexOperations = operations.indexOps(Product.class);
        IndexInitializer.init(indexOperations);

        Product p1 = Product.builder().id("1").name("Sugar").text("Cane sugar").price(1.0f).available(false).sortName("sort5").build();
        Product p2 = Product.builder().id("2").name("Sugar").text("Cane sugar").price(1.2f).available(true).sortName("sort4").build();
        Product p3 = Product.builder().id("3").name("Sugar").text("Beet sugar").price(1.1f).available(true).sortName("sort3").build();
        Product p4 = Product.builder().id("4").name("Salt").text("Rock salt").price(1.9f).available(true).sortName("sort2").build();
        Product p5 = Product.builder().id("5").name("Salt").text("Sea salt").price(2.1f).available(false).sortName("sort1").build();
        Product p6 = Product.builder().id("6").name(null).text("no name").price(3.4f).available(false).sortName("sort0").build();

        repository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6));
    }

    @AfterEach
    public void after() {
        indexOperations.delete();
    }

    @Test
    public void shouldSupportAnd() {
        assertThat(repository.findByNameAndText("Sugar", "Cane sugar")).hasSize(2);
        assertThat(repository.findByNameAndPrice("Sugar", 1.1f)).hasSize(1);
    }

    @Test
    public void shouldSupportTrueAndFalse() {
        assertThat(repository.findByAvailableTrue()).hasSize(3);
        assertThat(repository.findByAvailableFalse()).hasSize(3);
    }

    @Test
    public void shouldSupportOr() {
        List<Product> products = repository.findByNameOrPrice("Sugar", 1.9f);
        assertThat(products).hasSize(4);
        assertThat(repository.findByNameOrText("Salt", "Beet sugar")).hasSize(3);
    }

    @Test
    public void shouldSupportInAndNotInAndNot() {
        assertThat(repository.findByPriceIn(Arrays.asList(1.2f, 1.1f))).hasSize(2);
        assertThat(repository.findByPriceNotIn(Arrays.asList(1.2f, 1.1f))).hasSize(4);
        assertThat(repository.findByPriceNot(1.2f)).hasSize(5);
    }

    @Test
    public void shouldWorkWithNotIn() {
        assertThat(repository.findByIdNotIn(Arrays.asList("2", "3"))).hasSize(4);
    }

    @Test
    public void shouldSupportBetween() {
        assertThat(repository.findByPriceBetween(1.0f, 2.0f)).hasSize(4);
    }

    @Test
    public void shouldSupportLessThanAndGreateThan() {
        assertThat(repository.findByPriceLessThan(1.1f)).hasSize(1);
        assertThat(repository.findByPriceLessThanEqual(1.1f)).hasSize(2);
        assertThat(repository.findByPriceGreaterThan(1.9f)).hasSize(2);
        assertThat(repository.findByPriceGreaterThanEqual(1.9f)).hasSize(3);
    }

    @Test
    public void shouldSupportSortOnFieldWithCustomFieldNameWithCriteria() {
        List<String> sortedIds = repository.findAllByNameOrderBySortName("Sugar").stream().map(it -> it.id).collect(Collectors.toList());

        assertThat(sortedIds).containsExactly("3", "2", "1");
    }

    @Test
    public void shouldSupportSortOnStandardFieldWithoutCriteria() {
        List<String> sortedIds = repository.findAllByOrderByText().stream().map(it -> it.text).collect(Collectors.toList());
        assertThat(sortedIds).containsExactly("Beet sugar", "Cane sugar", "Cane sugar", "Rock salt", "Sea salt", "no name");
    }

    @Test
    public void shouldReturnOneWithFindFirst() {
        Product product = repository.findFirstByName("Sugar");
        assertThat(product.name).isEqualTo("Sugar");
    }

    @Test
    public void shouldReturnTwoWithFindFirst2() {
        List<Product> products = repository.findFirst2ByName("Sugar");

        assertThat(products).hasSize(2);
        products.forEach(product -> assertThat(product.name).isEqualTo("Sugar"));
    }

    @Test
    public void shouldReturnTwoWithFindTop2() {
        List<Product> products = repository.findTop2ByName("Sugar");
        assertThat(products).hasSize(2);
        products.forEach(product -> assertThat(product.name).isEqualTo("Sugar"));
    }

    @Test
    public void shouldSearchForNullValue() {
        final List<Product> products = repository.findByName(null);
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getId()).isEqualTo("6");
    }

    @Test
    public void shouldDeleteWithNullValue() {
        repository.deleteByName(null);
        long count = repository.count();
        assertThat(count).isEqualTo(5);
    }

    @Test
    public void shouldReturnEmptyListOnFindByIdWithEmptyInputList() {
        Iterable<Product> products = repository.findAllById(new ArrayList<>());
        assertThat(products).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListOnDerivedMethodWithEmptyInputList() {
        Iterable<Product> products = repository.findAllByNameIn(new ArrayList<>());
        assertThat(products).isEmpty();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Document(indexName = "test-index-product-query-keywords", replicas = 0, refreshInterval = "-1")
    static class Product {
        @Id
        private String id;
        private String name;
        @Field(type = FieldType.Float)
        private Float price;
        @Field(type = FieldType.Keyword)
        private String text;
        private boolean available;
        @Field(name = "sort-name", type = FieldType.Keyword)
        private String sortName;
    }

    interface ProductRepository extends ElasticsearchRepository<Product, String> {
        List<Product> findByName(@Nullable String name);

        List<Product> findByNameAndText(String name, String text);

        List<Product> findByNameAndPrice(String name, Float price);

        List<Product> findByNameOrText(String name, String text);

        List<Product> findByNameOrPrice(String name, Float price);

        List<Product> findByAvailableTrue();

        List<Product> findByAvailableFalse();

        List<Product> findByPriceIn(List<Float> floats);

        List<Product> findByPriceNotIn(List<Float> floats);

        List<Product> findByPriceNot(float v);

        List<Product> findByPriceBetween(float v, float v1);

        List<Product> findByPriceLessThan(float v);

        List<Product> findByPriceLessThanEqual(float v);

        List<Product> findByPriceGreaterThan(float v);

        List<Product> findByPriceGreaterThanEqual(float v);

        List<Product> findByIdNotIn(List<String> strings);

        List<Product> findAllByNameOrderByText(String name);

        List<Product> findAllByNameOrderBySortName(String name);

        List<Product> findAllByOrderByText();

        List<Product> findAllByOrderBySortName();

        Product findFirstByName(String name);

        Product findTopByName(String name);

        List<Product> findFirst2ByName(String name);

        List<Product> findTop2ByName(String name);

        void deleteByName(@Nullable String name);

        List<Product> findAllByNameIn(List<String> name);
    }
}
