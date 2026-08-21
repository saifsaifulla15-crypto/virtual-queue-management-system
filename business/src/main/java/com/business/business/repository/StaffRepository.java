package com.business.business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.business.business.entiry.Staff;

public interface StaffRepository extends JpaRepository<Staff, Integer>{

	List<Staff> findByBusiness_IdAndActiveTrue(Integer id);

}
