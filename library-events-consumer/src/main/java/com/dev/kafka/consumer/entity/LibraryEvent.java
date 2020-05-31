package com.dev.kafka.consumer.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.dev.kafka.consumer.enums.LibraryEventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class LibraryEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -153393572063632896L;

	@Id
	@GeneratedValue
	private Integer libraryEventId;
	
	@Enumerated(EnumType.STRING)
	private LibraryEventType libraryEventType;
	
	@OneToOne(mappedBy = "libraryEvent", cascade = CascadeType.ALL)
	@ToString.Exclude
	private Book book; 
}
