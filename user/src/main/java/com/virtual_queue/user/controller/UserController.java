package com.virtual_queue.user.controller;

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

import com.virtual_queue.user.dto.ResponseStructure;
import com.virtual_queue.user.entity.Users;
import com.virtual_queue.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	@PostMapping("/register")
	public ResponseEntity<ResponseStructure<Users>> createUser(@Valid @RequestBody Users user){
		return userService.createUser(user);
	}
	
	@GetMapping("/get/{id}")
	public ResponseEntity<ResponseStructure<Users>> getUserById(@PathVariable Integer id){
		return userService.getUserById(id);
	}
	
	@GetMapping
	public ResponseEntity<ResponseStructure<List<Users>>> getAllUsers(){
		return userService.getAllUsers();
	}
	
	@PatchMapping("/update/{id}")
	public ResponseEntity<ResponseStructure<Users>> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> map){
		return userService.updateUser( id,map);
	}
}
