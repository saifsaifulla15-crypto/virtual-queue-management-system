package com.business.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.business.business.entiry.Business;

public interface BusinessRepository extends JpaRepository<Business, Integer>{

	Boolean existsByEmail(String mail);
	
	Boolean existsByPhone(String phone);
}
