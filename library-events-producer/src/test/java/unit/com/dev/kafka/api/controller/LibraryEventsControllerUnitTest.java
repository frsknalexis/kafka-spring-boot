package com.dev.kafka.api.controller;

import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.dev.kafka.api.domain.Book;
import com.dev.kafka.api.domain.LibraryEvent;
import com.dev.kafka.api.enums.LibraryEventType;
import com.dev.kafka.api.producer.LibraryEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(value = LibraryEventsController.class)
@AutoConfigureMockMvc
public class LibraryEventsControllerUnitTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@MockBean
	LibraryEventProducer libraryEventProducer;
	
	
	@Test
	/*
	public void postLibraryEvent() throws Exception {
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
		
		String json = objectMapper.writeValueAsString(libraryEvent);
		
		doNothing().when(libraryEventProducer)
				.sendLibraryEventTwo.accept(libraryEventMethod(isA(LibraryEvent.class)));
		
		mockMvc.perform(post("/api/library-events/v1/library-event")
			.content(json)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isCreated());
		
		
	}
	
	*/

	private LibraryEvent libraryEventMethod(Matcher<LibraryEvent> a) {
		LibraryEvent libraryEvent = new LibraryEvent();
		if (a.matches(libraryEvent))
			return libraryEvent;
		return null;
	}
}