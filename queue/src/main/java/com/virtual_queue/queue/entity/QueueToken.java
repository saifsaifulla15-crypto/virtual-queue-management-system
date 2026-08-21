package com.virtual_queue.queue.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.virtual_queue.queue.enums.TokenStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class QueueToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Integer tokenNumber;
	
	@Enumerated(EnumType.STRING)
	private TokenStatus status;
	private Integer userId;
	private LocalDateTime joinedAt;
	private LocalDateTime calledAt;
	private LocalDateTime serviceStartedAt;
	private LocalDateTime serviceCompletedAt;
	
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "queue_id", nullable = false)
	private Queue queue;
	
	@JsonIgnore
	@OneToOne(mappedBy = "queueToken")
	private ServiceRecord serviceRecord;
	
}
