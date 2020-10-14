package com.xxbb.springframework.data.elasticsearch.core.convert;

import com.xxbb.springframework.data.elasticsearch.annotations.DateFormat;
import com.xxbb.springframework.data.elasticsearch.annotations.Field;
import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import com.xxbb.springframework.data.elasticsearch.core.document.Document;
import com.xxbb.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import com.xxbb.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;
import lombok.*;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.*;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.*;
import static org.skyscreamer.jsonassert.JSONAssert.*;

public class MappingElasticsearchConverterTest {

    static final String JSON_STRING = "{\"_class\":\"com.xxbb.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverterTest$Car\",\"name\":\"Grat\",\"model\":\"Ford\"}";
    static final String CAR_MODEL = "Ford";
    static final String CAR_NAME = "Grat";

    Person sarahConnor;
    Person kyleReese;
    Person t800;

    MappingElasticsearchConverter mappingElasticsearchConverter;

    Inventory gun = new Gun("Glock 19", 33);
    Inventory grenade = new Grenade("40 mm");
    Inventory rifle = new Rifle("AR-18 Assault Rifle", 3.17, 40);
    Inventory shotGun = new ShotGun("Ithaca 37 Pump Shotgun");

    Address observatoryRoad;
    Place bigBunsCafe;

    Document sarahAsMap;
    Document t800AsMap;
    Document kyleAsMap;
    Document gratiotAveAsMap;
    Document gunAsMap;
    Document grenadeAsMap;
    Document rifleAsMap;
    Document shotGunAsMap;
    Document bigBunsCafeAsMap;
    Document notificationAsMap;

    @BeforeEach
    public void init() {
        SimpleElasticsearchMappingContext context = new SimpleElasticsearchMappingContext();
        context.setInitialEntitySet(Collections.singleton(Rifle.class));
        context.afterPropertiesSet();

        mappingElasticsearchConverter = new MappingElasticsearchConverter(context, new GenericConversionService());
        mappingElasticsearchConverter.setConversions(new ElasticsearchCustomConversions(Arrays.asList(new ShotGunToMapConverter(), new MapToShotGunConverter())));
        mappingElasticsearchConverter.afterPropertiesSet();

        sarahConnor = new Person();
        sarahConnor.id = "sarah";
        sarahConnor.name = "Sarah Connor";
        sarahConnor.gender = Gender.MAN;

        kyleReese = new Person();
        kyleReese.id = "kyle";
        kyleReese.name = "Kyle Reese";
        kyleReese.gender = Gender.MAN;

        t800 = new Person();
        t800.id = "t800";
        t800.name = "T-800";
        t800.gender = Gender.MACHINE;

        t800AsMap = Document.create();
        t800AsMap.put("id", "t800");
        t800AsMap.put("name", "T-800");
        t800AsMap.put("gender", "MACHINE");
        t800AsMap.put("_class", "com.xxbb.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverterTest$Person");

        sarahAsMap = Document.create();
        sarahAsMap.put("id", "sarah");
        sarahAsMap.put("name", "Sarah Connor");
        sarahAsMap.put("gender", "MAN");
        sarahAsMap.put("_class",
                "com.xxbb.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverterTest$Person");

        observatoryRoad = new Address();
        observatoryRoad.city = "Los Angeles";
        observatoryRoad.street = "2800 East Observatory Road";

        bigBunsCafe = new Place();
        bigBunsCafe.name = "Big Buns Cafe";
        bigBunsCafe.city = "Los Angeles";
        bigBunsCafe.street = "15 South Fremont Avenue";

        kyleAsMap = Document.create();
        kyleAsMap.put("id", "kyle");
        kyleAsMap.put("gender", "MAN");
        kyleAsMap.put("name", "Kyle Reese");

        gunAsMap = Document.create();
        gunAsMap.put("label", "Glock 19");
        gunAsMap.put("shotsPerMagazine", 33);
        gunAsMap.put("_class", Gun.class.getName());

        grenadeAsMap = Document.create();
        grenadeAsMap.put("label", "40 mm");
        grenadeAsMap.put("_class", Grenade.class.getName());

        rifleAsMap = Document.create();
        rifleAsMap.put("label", "AR-18 Assault Rifle");
        rifleAsMap.put("weight", 3.17D);
        rifleAsMap.put("maxShotsPerMagazine", 40);
        rifleAsMap.put("_class", "rifle");

        shotGunAsMap = Document.create();
        shotGunAsMap.put("model", "Ithaca 37 Pump Shotgun");
        shotGunAsMap.put("_class", ShotGun.class.getName());

        gratiotAveAsMap = Document.create();
        gratiotAveAsMap.put("city", "Los Angeles");
        gratiotAveAsMap.put("street", "2800 East Observatory Road");

        bigBunsCafeAsMap = Document.create();
        bigBunsCafeAsMap.put("name", "Big Buns Cafe");
        bigBunsCafeAsMap.put("city", "Los Angeles");
        bigBunsCafeAsMap.put("street", "15 South Fremont Avenue");
        bigBunsCafeAsMap.put("_class",
                "com.xxbb.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverterTest$Place");

        notificationAsMap = Document.create();
        notificationAsMap.put("id", 1L);
        notificationAsMap.put("fromEmail", "from@email.com");
        notificationAsMap.put("toEmail", "to@email.com");
        Map<String, Object> data = new HashMap<>();
        data.put("documentType", "abc");
        data.put("content", null);
        notificationAsMap.put("params", data);
        notificationAsMap.put("_class",
                "com.xxbb.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverterTest$Notification");

    }

    @Test
    public void shouldFailToInitializeGivenMappingContextIsNull() {
        assertThatThrownBy(() -> new MappingElasticsearchConverter(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldReturnMappingContextWithWhichItWasInitialized() {
        MappingContext context = new SimpleElasticsearchMappingContext();
        MappingElasticsearchConverter converter = new MappingElasticsearchConverter(context);

        assertThat(converter.getMappingContext()).isNotNull();
        assertThat(converter.getMappingContext()).isEqualTo(context);
    }

    @Test
    public void shouldReturnDefaultConversionService() {
        MappingElasticsearchConverter converter = new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext());
        ConversionService service = converter.getConversionService();

        assertThat(service).isNotNull();
    }

    @Test
    public void shouldMapObjectToJsonString() {
        String jsonResult = mappingElasticsearchConverter.mapObject(Car.builder().model(CAR_MODEL).name(CAR_NAME).build()).toJson();
        System.out.println(jsonResult);
        assertThat(jsonResult).isEqualTo(JSON_STRING);
    }

    @Test
    public void shouldReadJsonStringToObject() {
        Car result = mappingElasticsearchConverter.read(Car.class, Document.parse(JSON_STRING));

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(CAR_NAME);
        assertThat(result.getModel()).isEqualTo(CAR_MODEL);
    }

    @Test
    public void ignoresReadOnlyProperties() {
        Sample sample = new Sample();
        sample.readOnly = "readOnly";
        sample.property = "property";
        sample.javaTransientProperty = "javaTransient";
        sample.annotatedTransientProperty = "transient";

        String result = mappingElasticsearchConverter.mapObject(sample).toJson();

        assertThat(result).contains("\"property\"");
        assertThat(result).contains("\"javaTransient\"");
        assertThat(result).doesNotContain("\"readOnly\"");
        assertThat(result).doesNotContain("\"transient\"");
    }

    @Test
    public void writesNestedEntity() {
        Person person = new Person();
        person.birthDate = LocalDate.now();
        person.gender = Gender.MAN;
        person.address = observatoryRoad;
        Map<String, Object> sink = writeToMap(person);
        assertThat(sink.get("address")).isEqualTo(gratiotAveAsMap);
    }

    @Test
    public void writesConcreteList() {
        Person ginger = new Person();
        ginger.id = "ginger";
        ginger.gender = Gender.MAN;

        sarahConnor.coWorker = Arrays.asList(kyleReese, ginger);
        Map<String, Object> target = writeToMap(sarahConnor);
        assertThat((List) target.get("coWorker")).hasSize(2).contains(kyleAsMap);
    }

    @Test
    public void writesInterfaceList() {
        Inventory gun = new Gun("Glock 19", 33);
        Inventory grenade = new Grenade("40 mm");

        sarahConnor.inventoryList = Arrays.asList(gun, grenade);
        Map<String, Object> target = writeToMap(sarahConnor);
        assertThat((List) target.get("inventoryList")).containsExactly(gunAsMap, grenadeAsMap);
    }

    @Test
    public void readTypeCorrectly() {
        Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);
        assertThat(target).isEqualTo(sarahConnor);
    }


    @Test
    public void readListOfConcreteTypesCorrectly() {
        sarahAsMap.put("coWorker", Collections.singletonList(kyleAsMap));

        Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);
        assertThat(target.getCoWorker()).contains(kyleReese);
    }

    @Test
    public void readListOfInterfacesTypesCorrectly() {
        sarahAsMap.put("inventoryList", Arrays.asList(gunAsMap, grenadeAsMap));
        Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);
        assertThat(target.getInventoryList()).containsExactly(gun, grenade);
    }

    @Test
    public void writeMapOfConcreteType() {
        sarahConnor.shippingAddress = new LinkedHashMap<>();
        sarahConnor.shippingAddress.put("home", observatoryRoad);

        Map<String, Object> target = writeToMap(sarahConnor);
        assertThat(target.get("shippingAddress")).isInstanceOf(Map.class);
        assertThat(target.get("shippingAddress")).isEqualTo(Collections.singletonMap("home", gratiotAveAsMap));
    }

    @Test
    public void writeMapOfInterfaceType() {
        sarahConnor.inventoryMap = new LinkedHashMap<>();
        sarahConnor.inventoryMap.put("glock19", gun);
        sarahConnor.inventoryMap.put("40 mm grenade", grenade);

        Map<String, Object> target = writeToMap(sarahConnor);
        assertThat(target.get("inventoryMap")).isInstanceOf(Map.class);
        assertThat((Map) target.get("inventoryMap")).containsEntry("glock19", gunAsMap).containsEntry("40 mm grenade", grenadeAsMap);
    }

    @Test
    public void readConcreteMapCorrectly() {
        sarahAsMap.put("shippingAddress", Collections.singletonMap("home", gratiotAveAsMap));
        Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);

        assertThat(target.getShippingAddress()).hasSize(1).containsEntry("home", observatoryRoad);
    }

    @Test
    public void readInterfaceMapCorrectly() {
        sarahAsMap.put("inventoryMap", Collections.singletonMap("glock19", gunAsMap));
        Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);
        assertThat(target.getInventoryMap()).hasSize(1).containsEntry("glock19", gun);
    }

    @Test
    public void genericWriteList() {
        Skynet skynet = new Skynet();
        skynet.objectList = new ArrayList<>();
        skynet.objectList.add(t800);
        skynet.objectList.add(gun);

        Map<String, Object> target = writeToMap(skynet);

        assertThat((List<Object>) target.get("objectList")).containsExactly(t800AsMap, gunAsMap);
    }

    @Test
    public void readGenericList() {
        Document source = Document.create();
        source.put("objectList", Arrays.asList(t800AsMap, gunAsMap));
        Skynet target = mappingElasticsearchConverter.read(Skynet.class, source);

        assertThat(target.getObjectList()).hasSize(2).containsExactly(t800, gun);
    }


    @Test
    public void genericWriteListWithList() {
        Skynet skynet = new Skynet();
        skynet.objectList = new ArrayList<>();
        skynet.objectList.addAll(Arrays.asList(t800, gun));

        Map<String, Object> target = writeToMap(skynet);
        assertThat((List<Object>) target.get("objectList")).containsExactly(t800AsMap, gunAsMap);
    }

    @Test
    public void readGenericListList() {
        Document source = Document.create();
        source.put("objectList", Collections.singletonList(Arrays.asList(t800AsMap, gunAsMap)));
        Skynet target = mappingElasticsearchConverter.read(Skynet.class, source);

        assertThat(target.getObjectList()).containsExactly(Arrays.asList(t800, gun));
    }

    @Test
    public void writeGenericMap() {
        Skynet skynet = new Skynet();
        skynet.objectMap = new HashMap<>();
        skynet.objectMap.put("gun", gun);
        skynet.objectMap.put("grenade", grenade);

        Map<String, Object> target = writeToMap(skynet);
        assertThat((Map<String, Object>) target.get("objectMap")).hasSize(2).containsEntry("gun", gunAsMap).containsEntry("grenade", grenadeAsMap);
    }

    @Test
    public void readGenericMap() {
        Document source = Document.create();
        source.put("objectMap", Collections.singletonMap("glock19", gunAsMap));
        Skynet target = mappingElasticsearchConverter.read(Skynet.class, source);

        assertThat(target.getObjectMap()).containsEntry("glock19", gun);
    }

    @Test
    public void writeGenericMapMap() {
        Skynet skynet = new Skynet();
        skynet.objectMap = new LinkedHashMap<>();
        skynet.objectMap.put("inventory", Collections.singletonMap("glock19", gun));

        Map<String, Object> target = writeToMap(skynet);

        assertThat((Map<String, Object>) target.get("objectMap")).containsEntry("inventory", Collections.singletonMap("glock19", gunAsMap));
    }

    @Test
    public void readGenericMapMap() {
        Document source = Document.create();
        source.put("objectMap", Collections.singletonMap("inventory", Collections.singletonMap("glock19", gunAsMap)));

        Skynet target = mappingElasticsearchConverter.read(Skynet.class, source);
        assertThat(target.getObjectMap()).containsEntry("inventory", Collections.singletonMap("glock19", gun));
    }

    @Test
    public void readsNestedEntity() {
        sarahAsMap.put("address", gratiotAveAsMap);
        Person target = mappingElasticsearchConverter.read(Person.class, sarahAsMap);

        assertThat(target.getAddress()).isEqualTo(observatoryRoad);
    }

    @Test
    public void readsNestedObjectEntity() {
        Document source = Document.create();
        source.put("object", t800AsMap);
        Skynet target = mappingElasticsearchConverter.read(Skynet.class, source);

        assertThat(target.getObject()).isEqualTo(t800);
    }

    @Test
    public void writesAliased() {
        assertThat(writeToMap(rifle)).containsEntry("_class", "rifle").doesNotContainValue(Rifle.class.getName());
    }

    @Test
    public void writeNestedAliased() {
        t800.inventoryList = Collections.singletonList(rifle);
        Map<String, Object> target = writeToMap(t800);

        assertThat((List) target.get("inventoryList")).contains(rifleAsMap);
    }

    @Test
    public void readAliased() {
        assertThat(mappingElasticsearchConverter.read(Inventory.class, rifleAsMap)).isEqualTo(rifle);
    }

    @Test
    public void readsNestedAliased() {
        t800AsMap.put("inventoryList", Collections.singletonList(rifleAsMap));
        assertThat(mappingElasticsearchConverter.read(Person.class, t800AsMap).getInventoryList()).containsExactly(rifle);
    }

    @Test
    public void appliesCustomConverterForWrite() {
        assertThat(writeToMap(shotGun)).isEqualTo(shotGunAsMap);
    }

    @Test
    public void appliesCustomConverterForRead() {
        assertThat(mappingElasticsearchConverter.read(Inventory.class, shotGunAsMap)).isEqualTo(shotGun);
    }

    @Test
    public void shouldWriteLocalDate() throws JSONException {
        Person person = new Person();
        person.id = "4711";
        person.firstName = "John";
        person.lastName = "Doe";
        person.birthDate = LocalDate.of(2000, 10, 22);
        person.gender = Gender.MAN;

        String expected = "{" +
                "  \"id\": \"4711\"," +
                "  \"first-name\": \"John\"," +
                "  \"last-name\": \"Doe\"," +
                "  \"birth-date\": \"22.10.2000\"," +
                "  \"gender\": \"MAN\"" +
                "}";
        Document document = Document.create();
        mappingElasticsearchConverter.write(person, document);
        String json = document.toJson();

        assertEquals(expected, json, false);
    }

    @Test
    public void shouldWriteListOfLocalDate() throws JSONException {
        LocalDateEntity entity = new LocalDateEntity();
        entity.setId("4711");
        entity.setDates(Arrays.asList(LocalDate.of(1999, 4,12), LocalDate.of(2732, 2, 21)));
        String expected = "{\n" +
                "  \"id\": \"4711\"," +
                "  \"dates\": [\"12.04.1999\", \"21.02.2732\"]\n" +
                "}\n";

        Document document = Document.create();
        mappingElasticsearchConverter.write(entity, document);
        assertEquals(expected, document.toJson(), false);
    }
    
    @Test
    public void shouldReadLocalDate() {
        Document document = Document.create();
        document.put("id", "4711");
        document.put("first-name", "John");
        document.put("last-name", "Doe");
        document.put("birth-date", "22.09.2001");
        document.put("gender", "MAN");

        Person person = mappingElasticsearchConverter.read(Person.class, document);

        assertThat(person.getId()).isEqualTo("4711");
        assertThat(person.getBirthDate()).isEqualTo(LocalDate.of(2001,9,22));
        assertThat(person.getGender()).isEqualTo(Gender.MAN);
    }

    @Test
    public void shouldReadListOfLocalDate() {
        Document document = Document.create();
        document.put("id", "4711");
        document.put("dates", new String[] {"12.06.1988", "24.09.2055"});

        LocalDateEntity entity = mappingElasticsearchConverter.read(LocalDateEntity.class, document);
        assertThat(entity.getDates()).hasSize(2).containsExactly(LocalDate.of(1988,6,12),LocalDate.of(2055,9,24));
    }

    @Test
    public void writeEntityWithMapDataType() {
        Notification notification = new Notification();
        notification.fromEmail = "from@email.com";
        notification.toEmail = "to@email.com";
        Map<String, Object> data = new HashMap<>();
        data.put("documentType", "abc");
        data.put("content", null);
        notification.params = data;
        notification.id = 1L;

        Document document = Document.create();
        mappingElasticsearchConverter.write(notification, document);
        assertThat(document).isEqualTo(notificationAsMap);
    }

    @Test
    public void readEntityWithMapDataType() {
        Document document = Document.create();
        document.put("id", 1L);
        document.put("fromEmail", "from@email.com");
        document.put("toEmail", "to@email.com");
        Map<String, Object> data = new HashMap<>();
        data.put("documentType", "abc");
        data.put("content", null);
        document.put("params", data);
        Notification notification = mappingElasticsearchConverter.read(Notification.class, document);
        assertThat(notification.getParams().get("documentType")).isEqualTo("abc");
        assertThat(notification.getParams().get("content")).isNull();
    }

    @Test
    public void readGenericMapWithSimpleTypes() {
        Map<String, Object> mapWithSimpleValues = new HashMap<>();
        mapWithSimpleValues.put("int", 1);
        mapWithSimpleValues.put("string", "string");
        mapWithSimpleValues.put("boolean", true);

        Document document = Document.create();
        document.put("schemaLessObject", mapWithSimpleValues);

        SchemaLessObjectWrapper wrapper = mappingElasticsearchConverter.read(SchemaLessObjectWrapper.class, document);
        assertThat(wrapper.getSchemaLessObject()).isEqualTo(mapWithSimpleValues);
    }

    @Test
    public void shouldNotWriteSeqNoPrimaryTermProperty() {
        EntityWithSeqNoPrimaryTerm entity = new EntityWithSeqNoPrimaryTerm();
        entity.seqNoPrimaryTerm = new SeqNoPrimaryTerm(1L, 2L);
        Document document = Document.create();
        mappingElasticsearchConverter.write(entity, document);

        assertThat(document).doesNotContainValue("seqNoPrimaryTerm");
    }

    @Test
    public void shouldNotReadSeqNoPrimaryTermProperty() {
        Document document = Document.create().append("seqNoPrimaryTerm", emptyMap());
        EntityWithSeqNoPrimaryTerm entity = mappingElasticsearchConverter.read(EntityWithSeqNoPrimaryTerm.class, document);
        assertThat(entity.getSeqNoPrimaryTerm()).isNull();
    }

    @Test
    public void shouldWriteCollectionsWithNullValues() throws JSONException {
        EntityWithListProperty entity = new EntityWithListProperty();
        entity.setId("42");
        entity.setValues(Arrays.asList(null, "two", null, "four"));
        String expected = "{" +
                "  \"id\": \"42\"," +
                "  \"values\": [null, \"two\", null, \"four\"]" +
                "}";
        Document document = Document.create();
        mappingElasticsearchConverter.write(entity, document);
        assertEquals(expected, document.toJson(), false);
    }

    @Test
    public void shouldWriteEntityWithMapAsObject() throws JSONException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("foo", "bar");
        EntityWithObject entity = new EntityWithObject();
        entity.setId("42");
        entity.setObject(map);

        String expected = "{\n" +
                "  \"id\": \"42\",\n" +
                "  \"object\": {\n" +
                "     \"foo\": \"bar\"\n" +
                "  }\n" +
                "}\n";

        Document document = Document.create();
        mappingElasticsearchConverter.write(entity, document);
        assertEquals(expected, document.toJson(), false);
    }

    @Test
    public void shouldWriteNullValueIfConfigured() throws JSONException {
        EntityWithNullField entity = new EntityWithNullField();
        entity.setId("42");

        String expected = "{\n" +
                "  \"id\": \"42\"," +
                "  \"saved\": null\n" +
                "}\n";

        Document document = Document.create();
        mappingElasticsearchConverter.write(entity, document);
        assertEquals(expected, document.toJson(), false);
    }



    private Map<String, Object> writeToMap(Object source) {
        Document sink = Document.create();
        mappingElasticsearchConverter.write(source, sink);
        return sink;
    }



    public static class Sample {
        @Nullable
        public @ReadOnlyProperty String readOnly;
        @Nullable
        public @Transient String annotatedTransientProperty;
        @Nullable
        public transient String javaTransientProperty;
        @Nullable
        public String property;
    }

    @Data
    static class Person {
        @Id
        String id;

        String name;
        @Field(name = "first-name")
        String firstName;
        @Field(name = "last-name")
        String lastName;
        @Field(name = "birth-date", type = FieldType.Date, format = DateFormat.custom, pattern = "dd.MM.uuuu")
        LocalDate birthDate;
        Gender gender;
        Address address;

        List<Person> coWorker;
        List<Inventory> inventoryList;

        Map<String, Inventory> inventoryMap;
        Map<String, Address> shippingAddress;
    }

    @Data
    @Getter
    @Setter
    static class LocalDateEntity {
        @Id
        private String id;
        @Field(name = "dates", type = FieldType.Date, format = DateFormat.custom,pattern = "dd.MM.uuuu")
        private List<LocalDate> dates;
    }

    enum Gender {
        MAN("1"),MACHINE("0");
        String value;
        Gender(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    interface Inventory {
        String getLabel();
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    static class Gun implements Inventory {
        final String label;
        final int shotsPerMagazine;

        @Override
        public String getLabel() {
            return label;
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    static class Grenade implements Inventory {
        final String label;
        @Override
        public String getLabel() {
            return label;
        }
    }

    @TypeAlias("rifle")
    @RequiredArgsConstructor
    @EqualsAndHashCode
    static class Rifle implements Inventory {
        final String label;
        final double weight;
        final int maxShotsPerMagazine;
        @Override
        public String getLabel() {
            return label;
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    static class ShotGun implements Inventory {
        final String label;
        @Override
        public String getLabel() {
            return label;
        }
    }

    @Data
    static class Address {
        String street;
        String city;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    static class Place extends Address {
        String name;
    }

    @Data
    static class Skynet {
        Object object;
        List<Object> objectList;
        Map<String, Object> objectMap;
    }

    @Data
    static class Notification {
        Long id;
        String fromEmail;
        String toEmail;
        Map<String, Object> params;
    }

    @WritingConverter
    static class ShotGunToMapConverter implements Converter<ShotGun, Map<String, Object>> {

        @Override
        public Map<String, Object> convert(ShotGun source) {
            LinkedHashMap<String, Object> target = new LinkedHashMap<>();
            target.put("model", source.getLabel());
            target.put("_class", ShotGun.class.getName());
            return target;
        }
    }

    @ReadingConverter
    static class MapToShotGunConverter implements Converter<Map<String, Object>, ShotGun> {

        @Override
        public ShotGun convert(Map<String, Object> source) {
            return new ShotGun(source.get("model").toString());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class Car {
        private String name;
        private String model;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class SchemaLessObjectWrapper {
        private Map<String, Object> schemaLessObject;
    }

    @Data
    @com.xxbb.springframework.data.elasticsearch.annotations.Document(indexName = "test-index-entity-with-no-seq-no-primary-term-mapper")
    static class EntityWithSeqNoPrimaryTerm {
        @Nullable
        private SeqNoPrimaryTerm seqNoPrimaryTerm;
    }

    @Data
    static class EntityWithListProperty {
        @Id
        private String id;
        private List<String> values;
    }

    @Data
    static class EntityWithObject {
        @Id
        private String id;
        private Object object;
    }

    @Data
    static class EntityWithNullField {
        @Id
        private String id;
        @Field(type = FieldType.Text)
        private String notSaved;
        @Field(type = FieldType.Text, storeNullValue = true)
        private String saved;
    }
}