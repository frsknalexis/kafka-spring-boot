package com.dev.kafka.api.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import com.dev.kafka.api.domain.Book;
import com.dev.kafka.api.domain.LibraryEvent;
import com.dev.kafka.api.enums.LibraryEventType;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = { "library-events" }, partitions = 3)
@TestPropertySource(properties = { "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
						"spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsControllerIntegrationTest {

	@Autowired
	TestRestTemplate restTemplate;
	
	private Consumer<Integer, String> kafkaConsumer;
	
	@Autowired
	EmbeddedKafkaBroker embeddedKafka;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@BeforeEach
	public void setUp() {
		Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafka));
		kafkaConsumer = new DefaultKafkaConsumerFactory(configs, 
							() -> new IntegerDeserializer(), () -> new StringDeserializer()).createConsumer();
		embeddedKafka.consumeFromAllEmbeddedTopics(kafkaConsumer);
	}
	
	@AfterEach
	public void tearDown() {
		kafkaConsumer.close();
	}
	
	@Test
	@Timeout(5)
	public void postLibraryEvent() throws InterruptedException {
		
		Book book = Book.builder()
						.bookId(123)
						.bookName("Kafka using Spring Boot")
						.bookAuthor("Alexis")
						.build();
		
		LibraryEvent libraryEvent = LibraryEvent.builder()
												.libraryEventId(null)
												.libraryEventType(LibraryEventType.NEW)
												.book(book)
												.build();
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("content-type", MediaType.APPLICATION_JSON.toString());
		HttpEntity<LibraryEvent> requestEntity = new HttpEntity<LibraryEvent>(libraryEvent, headers);
		
		ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange("/api/library-events/v1/library-event", HttpMethod.POST,
																				requestEntity, LibraryEvent.class);
		
		Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		
		ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(kafkaConsumer, "library-events");
		
		// Thread.sleep(3000);
		
		String value = consumerRecord.value();
		String expectedRecord = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka using Spring Boot\",\"bookAuthor\":\"Alexis\"}}";
		Assertions.assertEquals(expectedRecord, value);
	}
	
	@Test
	@Timeout(5)
	public void updateLibraryEvent() {
		Book book = Book.builder()
				.bookId(123)
				.bookName("Kafka using Spring Boot")
				.bookAuthor("Alexis")
				.build();

		LibraryEvent libraryEvent = LibraryEvent.builder()
										.libraryEventId(1)
										.libraryEventType(LibraryEventType.UPDATE)
										.book(book)
										.build();
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("content-type", MediaType.APPLICATION_JSON.toString());
		HttpEntity<LibraryEvent> requestEntity = new HttpEntity<LibraryEvent>(libraryEvent, headers);
		
		ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange("/api/library-events/v1/library-event", HttpMethod.PUT, 
																				requestEntity, LibraryEvent.class);
		
		Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(kafkaConsumer, "library-events");
		
		String value = consumerRecord.value();
		String expectedRecord = "{\"libraryEventId\":1,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka using Spring Boot\",\"bookAuthor\":\"Alexis\"}}";
		Assertions.assertEquals(expectedRecord, value);
	}
}