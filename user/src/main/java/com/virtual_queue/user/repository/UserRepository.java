package com.virtual_queue.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virtual_queue.user.entity.Users;

public interface UserRepository extends JpaRepository<Users, Integer>{

	boolean existsByEmail(String mail);
	
	boolean existsByPhone(String phone);
}
