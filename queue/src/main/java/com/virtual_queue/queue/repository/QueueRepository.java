package com.virtual_queue.queue.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;

import com.virtual_queue.queue.entity.Queue;

public interface QueueRepository extends JpaRepository<Queue, Integer> {

	List<Queue> findByBusinessId(Integer id);
	
}
