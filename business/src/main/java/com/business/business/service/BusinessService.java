package com.business.business.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.business.business.dto.ResponseStructure;
import com.business.business.dto.Users;
import com.business.business.entiry.Business;
import com.business.business.enums.ROLE;
import com.business.business.exceptions.IdNotAvailableException;
import com.business.business.exceptions.InvalidInputException;
import com.business.business.exceptions.NoReccordAvailableException;
import com.business.business.feign.UserServiceClient;
import com.business.business.repository.BusinessRepository;

import feign.FeignException;
import jakarta.validation.Valid;

@Service
public class BusinessService {

	@Autowired
	private BusinessRepository businessRepository;
	
	@Autowired
	private UserServiceClient userServiceClient;

	public ResponseEntity<ResponseStructure<Business>> createBussiness(@Valid Business business) {
		if(businessRepository.existsByEmail(business.getEmail())) {
			throw new InvalidInputException("Email is already Exists");
		}
		else if(businessRepository.existsByPhone(business.getPhone())) {
			throw new InvalidInputException("Phone is Already Exists");
		}
		else if(business.getOwnerId() == null) {
			throw new InvalidInputException("to register owner is mandatory");
		}
		Users users;
		try {
			ResponseEntity<ResponseStructure<Users>> res = userServiceClient.getUserById(business.getOwnerId());
			
			if(res.getBody() == null || res.getBody().getData() == null) {
				throw new IdNotAvailableException("owner is not found");
			}
			users = res.getBody().getData();
		}
		catch(FeignException.NotFound e) {
			throw new IdNotAvailableException("owner id Not available");
		}
		if(users.getRole() != ROLE.OWNER) {
			throw new InvalidInputException("User must have OWNER role to register a business");
		}
		business.setCreatedAt(LocalDateTime.now());
		business.setUpdatedAt(LocalDateTime.now());
		ResponseStructure<Business> res = new ResponseStructure<Business>();
		res.setData(businessRepository.save(business));
		res.setMessage("business is registered successfully");
		res.setStatusCode(HttpStatus.CREATED.value());
		return new ResponseEntity<ResponseStructure<Business>>(res,HttpStatus.CREATED);
	}

	public ResponseEntity<ResponseStructure<List<Business>>> getAllBusinesses() {
		List<Business> businesses = businessRepository.findAll();
		if(businesses.isEmpty()) {
			throw new NoReccordAvailableException("No Businesses Available");
		}
		ResponseStructure<List<Business>> res = new ResponseStructure<List<Business>>();
		res.setData(businesses);
		res.setMessage("All Businesses are retrived Successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<List<Business>>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<Business>> getBusinessById(Integer id) {

		Optional<Business> opt = businessRepository.findById(id);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Business id Is Not available");
		}
		ResponseStructure<Business> res = new ResponseStructure<Business>();
		res.setData(opt.get());
		res.setMessage("Business retrived by given id");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<Business>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<Business>> updateBusiness(Integer id, Map<String, Object> map) {

		Optional<Business> opt = businessRepository.findById(id);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Business id Not FOund");
		}
		Business business = opt.get();
		for(Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object val = entry.getValue();
			switch(key) {
			case "name" : business.setName((String)val);
			break;
			case "description" : business.setDescription((String)val);
			break;
			case "phone" : business.setPhone((String) val);
			break;
			case "email" : business.setEmail((String)val);
			break;
			case "openingTime" : business.setOpeningTime(LocalTime.parse((String)val));
			break;
			case "closingTime" : business.setClosingTime(LocalTime.parse((String)val));
			break;
			case "address" : business.setAddress((String)val);
			break;
			}
		}
		business.setUpdatedAt(LocalDateTime.now());
		ResponseStructure<Business> res = new ResponseStructure<Business>();
		res.setData(businessRepository.save(business));
		res.setMessage("Business details updated successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<Business>>(res,HttpStatus.OK);
	}
}
