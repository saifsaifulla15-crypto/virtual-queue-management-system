package com.virtual_queue.queue.dto;

import com.virtual_queue.queue.enums.TokenStatus;

import lombok.Data;

@Data
public class QueueStatusResponse {

	private Integer tokenNumber;
	private TokenStatus status;
	private Integer peopleAhead;
	private Double averageServiceTimeMinutes;
	private Integer estimatedWaitingMinutes;
}
