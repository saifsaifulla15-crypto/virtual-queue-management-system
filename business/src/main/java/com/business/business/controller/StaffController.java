package com.business.business.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.business.business.dto.ResponseStructure;
import com.business.business.dto.StaffDto;
import com.business.business.entiry.Staff;
import com.business.business.service.StaffService;

@RestController
@RequestMapping("/staff")
public class StaffController {

	@Autowired
	private StaffService staffService;
	
	@PostMapping("/business/{businessId}/{userId}")
	public ResponseEntity<ResponseStructure<StaffDto>> addStaff(@PathVariable Integer businessId, @PathVariable Integer userId){
		return staffService.addStaff(businessId,userId);
	}
	@GetMapping("/business/{businessId}/staff")
	public ResponseEntity<ResponseStructure<List<StaffDto>>> getStaffByBussinessId(@PathVariable Integer businessId){
		return staffService.getStaffByBussinessId(businessId);
	}
	
	@PatchMapping("/status/{staffId}")
	public ResponseEntity<ResponseStructure<StaffDto>> updateStatus(@PathVariable Integer staffId){
		return staffService.updateStatus(staffId);
	}
	@GetMapping("/{staffId}")
	public ResponseEntity<ResponseStructure<StaffDto>> getStaffById(@PathVariable Integer staffId){
		return staffService.getStaffById(staffId);
	}
}
