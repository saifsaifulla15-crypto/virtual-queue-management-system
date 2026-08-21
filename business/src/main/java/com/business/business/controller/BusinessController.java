package com.business.business.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.business.business.dto.ResponseStructure;
import com.business.business.entiry.Business;
import com.business.business.service.BusinessService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/business")
public class BusinessController {

	@Autowired
	private BusinessService businessService;
	
	@PostMapping("/register")
	public ResponseEntity<ResponseStructure<Business>> createBussiness(@Valid @RequestBody Business business){
		return businessService.createBussiness(business);
	}
	
	@GetMapping
	public ResponseEntity<ResponseStructure<List<Business>>> getAllBusinesses(){
		return businessService.getAllBusinesses();
	}
	@GetMapping("/get/{id}")
	public ResponseEntity<ResponseStructure<Business>> getBusinessById(@PathVariable Integer id){
		return businessService.getBusinessById(id);
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity<ResponseStructure<Business>> updateBusiness(@PathVariable Integer id, @RequestBody Map<String, Object> map){
		return businessService.updateBusiness(id,map);
	}
}
