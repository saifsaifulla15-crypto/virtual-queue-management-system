package com.virtual_queue.queue.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.virtual_queue.queue.enums.CapacityType;
import com.virtual_queue.queue.enums.QueueStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = {"queueTokens"})
public class Queue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@NotBlank(message = "Name is required")
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	private String name;
	
	@Enumerated(EnumType.STRING)
	private QueueStatus status;
	private String description;
	
	@Enumerated(EnumType.STRING)
	private CapacityType capacityType;
	
	@NotNull(message = "Maximum capacity is required")
	@Min(value = 1, message = "Maximum capacity must be at least 1")
	private Integer maxCapacity;
	private Integer currentTokenNumber;
	
	@NotNull(message = "Default service time is required")
	@Min(value = 1, message = "Default service time must be at least 1 minute")
	private Integer defaultServiceTimeMinutes;
	private Double averageServiceTimeMinutes;
	
	@NotNull(message = "Business ID is required")
	private Integer businessId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	@OneToMany(mappedBy = "queue")
	@JsonIgnore
	private List<QueueToken> queueTokens;
}
