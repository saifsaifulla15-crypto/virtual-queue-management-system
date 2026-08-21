package com.business.business.dto;

import java.time.LocalDateTime;
import com.business.business.enums.ROLE;
import lombok.Data;

@Data
public class Users {

	private Integer id;
	private String name;
	private String email;
	private String password;
	private String phone;
	private ROLE role;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
