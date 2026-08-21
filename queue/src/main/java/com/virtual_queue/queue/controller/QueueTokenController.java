package com.virtual_queue.queue.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtual_queue.queue.Service.QueueTokenService;
import com.virtual_queue.queue.dto.QueueStatusResponse;
import com.virtual_queue.queue.dto.ResponseStructure;
import com.virtual_queue.queue.entity.QueueToken;

import jakarta.ws.rs.POST;

@RestController
@RequestMapping("/token")
public class QueueTokenController {

	@Autowired
	private QueueTokenService queueTokenService;
	
	@PostMapping("/{queueId}/join/{userId}")
	public ResponseEntity<ResponseStructure<QueueToken>> joinQueue(@PathVariable Integer queueId, @PathVariable Integer userId){
		return queueTokenService.joinQueue(queueId,userId);
	}
	
	@GetMapping("/{tokenId}")
	public ResponseEntity<ResponseStructure<QueueStatusResponse>> getUpdatedQueueStatus(@PathVariable Integer tokenId) {
		return queueTokenService.getUpdatedQueueStatus(tokenId);
	}
	
	@PostMapping("/{queueId}/next")
	public ResponseEntity<ResponseStructure<QueueToken>> callNextToken(@PathVariable Integer queueId){
		return queueTokenService.callNextToken(queueId);
	}
	
	@PostMapping("/{tokenId}/start")
	public ResponseEntity<ResponseStructure<QueueToken>> startService(@PathVariable Integer tokenId){
		return queueTokenService.startService(tokenId);
	}
	@PostMapping("/{tokenId}/complete/{staffId}")
	public ResponseEntity<ResponseStructure<QueueToken>> completeService(@PathVariable Integer tokenId, @PathVariable Integer staffId){
		return queueTokenService.completeService(tokenId,staffId);
	}
	
	@PatchMapping("/{tokenId}/cancel")
	public ResponseEntity<ResponseStructure<QueueToken>> cancelToken(@PathVariable Integer tokenId ){
		return queueTokenService.cancelToken(tokenId);
	}
	
	@PostMapping("/{tokenId}/skip")
	public ResponseEntity<ResponseStructure<QueueToken>> skipToken(@PathVariable Integer tokenId ){
		return queueTokenService.skipToken(tokenId);
	}
	
	
	
	
}
