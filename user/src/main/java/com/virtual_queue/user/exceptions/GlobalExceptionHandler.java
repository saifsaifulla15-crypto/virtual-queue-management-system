package com.virtual_queue.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.virtual_queue.user.dto.ResponseStructure;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidInputException.class)
	public ResponseEntity<ResponseStructure<String>> handleIVIE(InvalidInputException exception){
		ResponseStructure<String> res = new ResponseStructure<String>();
		res.setStatusCode(HttpStatus.NOT_FOUND.value());
		res.setMessage(exception.getMessage());
		res.setData("Failure");
		return new ResponseEntity<>(res,HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(IdNotAvailableException.class)
	public ResponseEntity<ResponseStructure<String>> handleIDNA(IdNotAvailableException exception){
		ResponseStructure<String> res = new ResponseStructure<String>();
		res.setStatusCode(HttpStatus.NOT_FOUND.value());
		res.setMessage(exception.getMessage());
		res.setData("Failure");
		return new ResponseEntity<>(res,HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(NoReccordAvailableException.class)
	public ResponseEntity<ResponseStructure<String>> handleNRE(NoReccordAvailableException exception){
		ResponseStructure<String> res = new ResponseStructure<String>();
		res.setStatusCode(HttpStatus.NOT_FOUND.value());
		res.setMessage(exception.getMessage());
		res.setData("Failure");
		return new ResponseEntity<>(res,HttpStatus.NOT_FOUND);
	}
}
