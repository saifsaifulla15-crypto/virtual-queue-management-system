package com.virtual_queue.user.entity;

import java.time.LocalDateTime;

import com.virtual_queue.user.enums.ROLE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Entity
@Data
public class Users {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@NotBlank(message = "Name is required")
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	private String name;
	
	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	@Column(unique = true, nullable = false)
	private String email;
	
	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must contain at least 8 characters")
	private String password;
	
	@Column(unique = true, nullable = false)
	@NotBlank(message = "Phone number is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Phone number must contain exactly 10 digits")
	private String phone;
	
	@NotNull(message = "Role is required")
	@Enumerated(EnumType.STRING)
	private ROLE role;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
