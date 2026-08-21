package com.virtual_queue.queue.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;


@Data
public class Staff {

	private Integer id;
	private Integer userId;
	private Boolean active;
	private LocalDate joinedAt;
	private LocalDateTime updatedAt;
	private Business business;
}
