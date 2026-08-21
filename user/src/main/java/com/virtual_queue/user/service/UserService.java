package com.virtual_queue.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.virtual_queue.user.dto.ResponseStructure;
import com.virtual_queue.user.entity.Users;
import com.virtual_queue.user.enums.ROLE;
import com.virtual_queue.user.exceptions.IdNotAvailableException;
import com.virtual_queue.user.exceptions.InvalidInputException;
import com.virtual_queue.user.exceptions.NoReccordAvailableException;
import com.virtual_queue.user.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	public ResponseEntity<ResponseStructure<Users>> createUser(Users user) {
		if(userRepository.existsByEmail(user.getEmail())) {
			throw new InvalidInputException("email is already Exists");
		}
		else if(userRepository.existsByPhone(user.getPhone())) {
			throw new InvalidInputException("Phone Number is Already Exists");
		}
		
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());
		Users u = userRepository.save(user);
		u.setPassword("*******************");
		ResponseStructure<Users> res = new ResponseStructure<Users>();
		res.setData(u);
		res.setMessage("User created successfully");
		res.setStatusCode(HttpStatus.ACCEPTED.value());
		return new ResponseEntity<ResponseStructure<Users>>(res,HttpStatus.ACCEPTED);
	}

	
	public ResponseEntity<ResponseStructure<Users>> getUserById(Integer id) {
		Optional<Users> opt = userRepository.findById(id);
		if(opt.isEmpty()) {
			throw new InvalidInputException("Id Not Available ");
		}
		ResponseStructure<Users> res = new ResponseStructure<Users>();
		res.setData(opt.get());
		res.setMessage("User Retrived Successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<Users>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<List<Users>>> getAllUsers() {
		List<Users> users = userRepository.findAll();
		if(users.isEmpty()) {
			throw new NoReccordAvailableException("no Users Available");
		}
		ResponseStructure<List<Users>> res = new ResponseStructure<List<Users>>();
		res.setData(users);
		res.setMessage("All users Retrived Successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<List<Users>>>(res,HttpStatus.OK);
	}


	public ResponseEntity<ResponseStructure<Users>> updateUser(Integer id, Map<String, Object> map) {
		Optional<Users> opt = userRepository.findById(id);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Id not found ");
		}
		Users users = opt.get();
		
		for(Map.Entry<String, Object> e:map.entrySet()) {
			String key = e.getKey();
			switch(key) {
			case "name": users.setName((String)e.getValue());
			break;
			case "email" : users.setEmail((String) e.getValue());
			break;
			case "password" : users.setPassword((String) e.getValue());
			break;
			case "phone" : users.setPhone((String) e.getValue());
			break;
			case "role" : users.setRole(ROLE.valueOf((String) e.getValue()));
			break;
			} 
		}
		users.setUpdatedAt(LocalDateTime.now());
		ResponseStructure<Users> res = new ResponseStructure<Users>();
		res.setData(userRepository.save(users));
		res.setMessage("User deatails Updated Successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<Users>>(res,HttpStatus.OK);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
