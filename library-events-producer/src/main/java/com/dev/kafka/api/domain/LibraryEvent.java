package com.dev.kafka.api.domain;

import java.io.Serializable;

import com.dev.kafka.api.enums.LibraryEventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LibraryEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1538193372069395146L;

	private Integer libraryEventId;
	
	private LibraryEventType libraryEventType;
	
	private Book book;
}
