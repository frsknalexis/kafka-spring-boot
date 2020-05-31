package com.dev.kafka.consumer.service;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.dev.kafka.consumer.entity.LibraryEvent;
import com.dev.kafka.consumer.jpa.LibraryEventsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service("libraryEventsService")
@Slf4j
public class LibraryEventsService {

	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	@Qualifier("libraryEventsRepository")
	private LibraryEventsRepository libraryEventsRepository;
	
	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;
	
	private Function<String, LibraryEvent> readValueFromJsonString = (s) -> {
		try {
			return objectMapper.readValue(s, LibraryEvent.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	};
	
	private <T,R> R consumerRecorToObject(T t, Function<T, R> function) {
		return function.apply(t);
	};
	
	private Consumer<LibraryEvent> save = (libraryEvent) -> {
		libraryEvent.getBook().setLibraryEvent(libraryEvent);
		libraryEventsRepository.save(libraryEvent);
		log.info("Successfully persisted Library Event -> {}", libraryEvent);
	};
	
	private Consumer<LibraryEvent> validate = (libraryEvent) -> {
		if (libraryEvent.getLibraryEventId() == null) {
			throw new IllegalArgumentException("Library Event Id is missing");
		}
		
		Optional<LibraryEvent> libraryEventOptional = libraryEventsRepository.findById(libraryEvent.getLibraryEventId());
		if (!libraryEventOptional.isPresent()) {
			throw new IllegalArgumentException("Not a valid library Event");
		}
		log.info("Validation is successful for the library Event -> {} ", libraryEventOptional.get());
	};
	
	public Consumer<ConsumerRecord<Integer, String>> processLibraryEvent = (consumerRecord) -> {
		LibraryEvent libraryEvent = readValueFromJsonString.apply(consumerRecord.value());
		log.info("libraryEvent -> {}", libraryEvent);
		
		/*
		if (libraryEvent.getLibraryEventId() == 000 && libraryEvent.getLibraryEventId() != null) {
			throw new RecoverableDataAccessException("Temporary Network Issue");
		}
		*/
		
		if (libraryEvent.getLibraryEventType().toString().equals("NEW")) {
			save.accept(libraryEvent);
		}
		else if (libraryEvent.getLibraryEventType().toString().equals("UPDATE")) {
			validate.accept(libraryEvent);
			save.accept(libraryEvent);
		}
	};
	
	public Consumer<ConsumerRecord<Integer, String>> handleRecovery = (consumerRecord) -> {
		
		Integer key = consumerRecorToObject(consumerRecord, (cR) -> {
			return cR.key();
		});
		
		String message = consumerRecorToObject(consumerRecord, (cR) -> {
			return cR.value();
		});
		
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, message);
		listenableFuture.addCallback((result) -> {
			handleSuccess(key, message, result);
		}, handleFailure::accept);
	};
	
	private static Consumer<Throwable> handleFailure = (t) -> {
		log.error("Error Sending the Message and the exception is {}", t.getMessage());
		try {
			throw t;
		} catch (Throwable throwable) {
			log.error("Error in OnFailure: {}", throwable.getMessage());
		}
	};
	
	private static void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message Sent SuccessFully for the key : {} and the value is {} , partition is {}", key, value, result.getRecordMetadata().partition());
    }
}