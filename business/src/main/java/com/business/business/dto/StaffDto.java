package com.business.business.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StaffDto {

	private Integer id;
	private Integer userId;
	private Boolean active;
	private LocalDate joinedAt;
	private LocalDateTime updatedAt;
	private Integer businessId;
}
