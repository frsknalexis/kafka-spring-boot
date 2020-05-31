package com.dev.kafka.consumer.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.dev.kafka.consumer.service.LibraryEventsService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LibraryEventsConsumer {
	
	@Autowired
	@Qualifier("libraryEventsService")
	private LibraryEventsService libraryEventsService;

	@KafkaListener(topics = { "library-events" })
	public void onMessages(ConsumerRecord<Integer, String> consumerRecord) {
		libraryEventsService.processLibraryEvent.accept(consumerRecord);
		log.info("ConsumerRecord -> {}", consumerRecord);
	}
}