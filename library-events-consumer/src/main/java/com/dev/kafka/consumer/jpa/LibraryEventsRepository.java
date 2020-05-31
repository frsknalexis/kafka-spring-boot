package com.dev.kafka.consumer.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.dev.kafka.consumer.entity.LibraryEvent;

@Repository("libraryEventsRepository")
public interface LibraryEventsRepository extends CrudRepository<LibraryEvent, Integer> {

}