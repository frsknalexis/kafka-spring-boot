package com.dev.kafka.consumer.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Book implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5406401932409958263L;
	
	@Id
	private Integer bookId;
	
	private String bookName;
	
	private String bookAuthor;
	
	@OneToOne
	@JoinColumn(name = "libraryEventId")
	private LibraryEvent libraryEvent;
}
