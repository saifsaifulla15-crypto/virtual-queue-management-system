package com.business.business.entiry;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class Business {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@NotBlank(message = "Name is required")
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	private String name;
	private String description;
	
	@NotBlank(message = "Address is requird")
	private String address;
	
	@Column(unique = true, nullable = false)
	@NotBlank(message = "Phone number is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Phone number must contain exactly 10 digits")
	private String phone;
	
	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	@Column(unique = true, nullable = false)
	private String email;
	
	private LocalTime openingTime;
	private LocalTime closingTime;
	
	@NotNull(message ="owner is mandatory")
	private Integer ownerId;
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	@OneToMany(mappedBy = "business")
	private List<Staff> staff;
}
