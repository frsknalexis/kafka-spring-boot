package com.dev.kafka.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.kafka.api.domain.LibraryEvent;
import com.dev.kafka.api.enums.LibraryEventType;
import com.dev.kafka.api.producer.LibraryEventProducer;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/library-events/v1")
@Slf4j
public class LibraryEventsController {
	
	@Autowired
	private LibraryEventProducer libraryEventProducer;

	@PostMapping(value = "/library-event")
	public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody LibraryEvent libraryEvent) {
		// invoke kafka producer
		libraryEvent.setLibraryEventType(LibraryEventType.NEW);
		libraryEventProducer.sendLibraryEventTwo.accept(libraryEvent);
		return ResponseEntity.status(HttpStatus.CREATED)
							.body(libraryEvent);
	}
}