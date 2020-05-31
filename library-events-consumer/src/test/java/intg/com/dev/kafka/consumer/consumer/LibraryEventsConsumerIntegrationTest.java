package com.dev.kafka.consumer.consumer;

import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import com.dev.kafka.consumer.entity.Book;
import com.dev.kafka.consumer.entity.LibraryEvent;
import com.dev.kafka.consumer.enums.LibraryEventType;
import com.dev.kafka.consumer.jpa.LibraryEventsRepository;
import com.dev.kafka.consumer.service.LibraryEventsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@EmbeddedKafka(topics = { "library-events" }, partitions = 3)
@TestPropertySource(properties = { "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
						"spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsConsumerIntegrationTest {

	@Autowired
	EmbeddedKafkaBroker embeddedKafka;
	
	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;
	
	@Autowired
	KafkaListenerEndpointRegistry endPointRegistry;
	
	@SpyBean
	LibraryEventsService libraryEventsServiceSpy;
	
	@SpyBean
	LibraryEventsConsumer libraryEventsConsumerSpy;
	
	@Autowired
	@Qualifier("libraryEventsRepository")
	LibraryEventsRepository libraryEventsRepository;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@BeforeEach
	public void setUp() {
		endPointRegistry.getListenerContainers()
						.forEach((listenerContainer) -> {
							ContainerTestUtils.waitForAssignment(listenerContainer, 
												embeddedKafka.getPartitionsPerTopic());
						});
	}
	
	@AfterEach
	public void tearDown() {
		libraryEventsRepository.deleteAll();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void publishNewLibraryEvent() throws InterruptedException, ExecutionException {
		String json = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
		kafkaTemplate.sendDefault(json).get();
		
		CountDownLatch latch = new CountDownLatch(1);
		latch.await(3, TimeUnit.SECONDS);
		
		//verify(libraryEventsConsumerSpy, times(1)).onMessages((ConsumerRecord<Integer, String>) isA(ConsumerRecord.class));
		//verify(libraryEventsServiceSpy, times(1)).processLibraryEvent.accept((ConsumerRecord<Integer, String>) isA(ConsumerRecord.class));
		List<LibraryEvent> libraryEventList = (List<LibraryEvent>) libraryEventsRepository.findAll();
		assert libraryEventList.size() > 0;
		libraryEventList.stream()
						.forEach((libraryEvent) -> {
							assert libraryEvent.getLibraryEventId() != null;
							Assertions.assertEquals(1, libraryEvent.getLibraryEventId());
						});
	}
	
	@Test
	public void publishUpdateLibraryEvent() throws JsonMappingException, JsonProcessingException, InterruptedException, ExecutionException {
		String json = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
		LibraryEvent libraryEvent = objectMapper.readValue(json, LibraryEvent.class);
		libraryEvent.getBook().setLibraryEvent(libraryEvent);
		libraryEventsRepository.save(libraryEvent);
		
		Book updatedBook = Book.builder()
							.bookId(456)
							.bookName("Kafka Using Spring Boot 2.x")
							.bookAuthor("Dilip")
							.build();
		libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
		libraryEvent.setBook(updatedBook);
		
		String jsonUpdated = objectMapper.writeValueAsString(libraryEvent);
		kafkaTemplate.sendDefault(libraryEvent.getLibraryEventId(), jsonUpdated).get();
		
		CountDownLatch latch = new CountDownLatch(1);
		latch.await(3, TimeUnit.SECONDS);
		
		LibraryEvent persistedLibraryEvent = libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).get();
		Assertions.assertEquals("Kafka Using Spring Boot 2.x", persistedLibraryEvent.getBook().getBookName());
	}
}