package com.dev.kafka.api.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1515743848880683767L;

	private Integer bookId;
	
	private String bookName;
	
	private String bookAuthor;
}
