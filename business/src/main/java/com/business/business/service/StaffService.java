package com.business.business.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.business.business.dto.ResponseStructure;
import com.business.business.dto.StaffDto;
import com.business.business.dto.Users;
import com.business.business.entiry.Business;
import com.business.business.entiry.Staff;
import com.business.business.enums.ROLE;
import com.business.business.exceptions.IdNotAvailableException;
import com.business.business.exceptions.InvalidInputException;
import com.business.business.exceptions.NoReccordAvailableException;
import com.business.business.feign.UserServiceClient;
import com.business.business.repository.BusinessRepository;
import com.business.business.repository.StaffRepository;

import feign.FeignException;

@Service
public class StaffService {

	@Autowired
	private StaffRepository staffRepository;
	
	@Autowired
	private BusinessRepository businessRepository;
	
	@Autowired
	private UserServiceClient userServiceClient;
	
	public StaffDto mappToDto(Staff staff) {
		StaffDto  staffDto = new StaffDto();
		staffDto.setId(staff.getId());
		staffDto.setUserId(staff.getUserId());
		staffDto.setActive(staff.getActive());
		staffDto.setJoinedAt(staff.getJoinedAt());
		staffDto.setUpdatedAt(staff.getUpdatedAt());
		staffDto.setBusinessId(staff.getBusiness().getId());
		return staffDto;
	}

	public ResponseEntity<ResponseStructure<StaffDto>> addStaff(Integer businessId, Integer userId) {

		Optional<Business> opt = businessRepository.findById(businessId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Business id not found");
		}
		Users users;
		try {
			ResponseEntity<ResponseStructure<Users>> res = userServiceClient.getUserById(userId);
			if(res.getBody() == null || res.getBody().getData() == null) {
				throw new IdNotAvailableException("User id Not Found");
			}
			users = res.getBody().getData();
		}
		catch(FeignException.NotFound e) {
			throw new IdNotAvailableException("User id not Found");
		}
		if (users.getRole() != ROLE.STAFF) {
		    throw new InvalidInputException("User must have STAFF role");
		}
		Business business = opt.get();
		
		if(business.getStaff().stream().anyMatch(staff -> staff.getUserId().equals(userId))) {
			throw new InvalidInputException("User is already joined in your company");
		}
		
		Staff staff = new Staff();
		staff.setActive(true);
		staff.setJoinedAt(LocalDate.now());
		staff.setUpdatedAt(LocalDateTime.now());
		staff.setUserId(userId);
		staff.setBusiness(business);		
		ResponseStructure<StaffDto> res = new ResponseStructure<StaffDto>();
		res.setData(mappToDto(staffRepository.save(staff)));
		res.setMessage("Staff Added successfully");
		res.setStatusCode(HttpStatus.CREATED.value());
		return new ResponseEntity<ResponseStructure<StaffDto>>(res,HttpStatus.CREATED);
	}


	public ResponseEntity<ResponseStructure<List<StaffDto>>> getStaffByBussinessId(Integer businessId) {
		List<Staff> staff = staffRepository.findByBusiness_IdAndActiveTrue(businessId);
		if(staff.isEmpty()) {
			throw new NoReccordAvailableException("No Staff Available");
		}
		List<StaffDto> staffs = new ArrayList<StaffDto>();
		for(Staff s : staff) {
			staffs.add(mappToDto(s));
		}
		
		ResponseStructure<List<StaffDto>> res = new ResponseStructure<List<StaffDto>>();
		res.setData(staffs);
		res.setMessage("Staff Retrived successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<List<StaffDto>>>(res,HttpStatus.OK);
	}


	public ResponseEntity<ResponseStructure<StaffDto>> updateStatus(Integer staffId) {

		Optional<Staff> opt = staffRepository.findById(staffId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Id Not Available");
		}
		Staff staff = opt.get();
		if(staff.getActive()) {
			staff.setActive(false);
		}
		else {
			staff.setActive(true);
		}
		ResponseStructure<StaffDto> res = new ResponseStructure<StaffDto>();
		res.setData(mappToDto(staffRepository.save(staff)));
		res.setMessage("Staff Status Updated successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<StaffDto>>(res,HttpStatus.OK);
	}
	
	public ResponseEntity<ResponseStructure<StaffDto>> getStaffById( Integer staffId){
	
		Optional<Staff> opt = staffRepository.findById(staffId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("id Not Found");
		}
		ResponseStructure<StaffDto> res = new ResponseStructure<StaffDto>();
		res.setData(mappToDto(opt.get()));
		res.setMessage("Staff retrived based on Id");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<StaffDto>>(res,HttpStatus.OK);
	}	

}
