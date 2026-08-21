package com.virtual_queue.queue.dto;

import lombok.Data;

@Data
public class QueueAnalytics {

	private Integer totalCustomersServed;
	private Integer cancelled;
	private Integer skipped;
	private Double averageServingTime;
	private Double averageWaitingTime;
	private Double maximumWaitingTime;
	private Double minimumWaitingTime;
	private Integer totalTokens;
	
}
