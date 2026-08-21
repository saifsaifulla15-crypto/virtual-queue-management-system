package com.virtual_queue.queue.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class ServiceRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Integer staffId;
	private LocalDateTime serviceStartTime;
	private LocalDateTime serviceEndTime;
	private Double serviceDurationMinutes;
	private Double waitingDurationMinutes;
	private LocalDateTime createdAt;
	
	@OneToOne
	@JoinColumn(name = "queue_token_id", nullable = false,  unique = true)
	private QueueToken queueToken;
	
}
