package com.virtual_queue.queue.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
public class Business {
	
	private Integer id;
	private String name;
	private String description;
	private String address;
	private String phone;
	private String email;
	private LocalTime openingTime;
	private LocalTime closingTime;
	private Integer ownerId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<Staff> staff;
}
