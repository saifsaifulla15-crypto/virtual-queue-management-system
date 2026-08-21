package com.business.business.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.business.business.dto.ResponseStructure;
import com.business.business.dto.Users;


@FeignClient("USER")
public interface UserServiceClient {

	@GetMapping("/user/get/{id}")
	public ResponseEntity<ResponseStructure<Users>> getUserById(@PathVariable Integer id);
}
