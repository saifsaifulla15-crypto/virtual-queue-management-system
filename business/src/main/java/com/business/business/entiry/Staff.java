package com.business.business.entiry;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class Staff {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@NotNull(message = "UserId is Required")
	private Integer userId;
	private Boolean active;
	private LocalDate joinedAt;
	private LocalDateTime updatedAt;
	
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "business_id", nullable = false)
	private Business business;
}
