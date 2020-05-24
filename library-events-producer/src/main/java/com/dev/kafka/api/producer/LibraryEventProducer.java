package com.dev.kafka.api.producer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import com.dev.kafka.api.domain.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LibraryEventProducer {

	@Autowired
	private KafkaTemplate<Integer, String> kafkaTemplate;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private String topic = "library-events";
	
	private Function<LibraryEvent, Integer> functionLibraryEventKey = LibraryEvent::getLibraryEventId;
	
	private Function<LibraryEvent, String> functionLibraryEventValue = (libraryEvent) -> {
		try {
			return objectMapper.writeValueAsString(libraryEvent);
		} catch (JsonProcessingException ex) {
			ex.getMessage();
		}
		return null;
	};
	
	private BiFunction<Integer, String, Map<Integer, String>> mapBiFunction = (k, v) -> {
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(k, v);
		return map;
	};
	
	public Consumer<LibraryEvent> sendLibraryEvent = (libraryEvent) -> {
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(functionLibraryEventKey.apply(libraryEvent), 
														functionLibraryEventValue.apply(libraryEvent));
		
		listenableFuture.addCallback((result) -> {
			handleSuccess.accept(mapBiFunction.apply(functionLibraryEventKey.apply(libraryEvent), 
									functionLibraryEventValue.apply(libraryEvent)), result);	
		}, (e) -> {
			handleFailure.accept(e);
		});
	};
	
	public Consumer<LibraryEvent> sendLibraryEventTwo = (libraryEvent) -> {
		ProducerRecord<Integer, String> producerRecord = buildProducerRecord(functionLibraryEventKey.apply(libraryEvent),
																				functionLibraryEventValue.apply(libraryEvent), topic);
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);
		
		listenableFuture.addCallback((result) -> {
			handleSuccess.accept(mapBiFunction.apply(functionLibraryEventKey.apply(libraryEvent),
														functionLibraryEventValue.apply(libraryEvent)), result);
		}, handleFailure::accept);
	};
	
	private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String topicName) {
		
		Function<Supplier<Header>, List<Header>> recordHeaders = (s) -> {
			return List.of(s.get());
		};
		return new ProducerRecord<Integer, String>(topicName, null, key, value, 
													recordHeaders.apply(() -> new RecordHeader("event-source", "scanner".getBytes())));
	}
	
	public Function<LibraryEvent, SendResult<Integer, String>> sendLibraryEventSynchronous = (libraryEvent) -> {
		
		SendResult<Integer, String> sendResult = null;
		
		try {
			sendResult = kafkaTemplate.sendDefault(functionLibraryEventKey.apply(libraryEvent), 
										functionLibraryEventValue.apply(libraryEvent)).get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("ExecutionException/InterruptedException Sending the Message and the exception is {}", e.getMessage());
		} catch (Exception e) {
			log.error("Exception Sending the Message and the exception is {}", e.getMessage());
			throw e;
		}
		return sendResult;
	};
	
	private static Consumer<Throwable> handleFailure = (t) -> {
		log.error("Error Sending the Message and the exception is {}", t.getMessage());
		try {
			throw t;
		} catch (Throwable throwable) {
			log.error("Error in OnFailure: {}", throwable.getMessage());
		}
	};
	
	private static BiConsumer<Map<Integer, String>, SendResult<Integer, String>> handleSuccess = (map, result) -> {
		map.forEach((k, v) -> {
			log.info("Message Sent SuccessFully for the key : {} and the value is {} , partition is {}", k, v, result.getRecordMetadata().partition());
		});
	};
}