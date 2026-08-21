package com.virtual_queue.queue.controller;

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

import com.virtual_queue.queue.Service.QueueService;
import com.virtual_queue.queue.dto.ResponseStructure;
import com.virtual_queue.queue.entity.Queue;
import com.virtual_queue.queue.enums.QueueStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/queue")
public class QueueController {

	@Autowired
	private QueueService queueService;
	
	@PostMapping
	public ResponseEntity<ResponseStructure<Queue>> createQueue(@Valid @RequestBody Queue queue){
		return queueService.createQueue(queue);
	}
	
	@GetMapping("/{queueId}")
	public ResponseEntity<ResponseStructure<Queue>> getQueueById(@PathVariable Integer queueId){
		return queueService.getQueueById(queueId);
	}
	
	@GetMapping
	public ResponseEntity<ResponseStructure<List<Queue>>> getAllQueues(){
		return queueService.getAllQueues();
	}
	
	@GetMapping("/business/{businessId}")
	public ResponseEntity<ResponseStructure<List<Queue>>> getQueueByBusinessId(@PathVariable Integer businessId){
		return queueService.getQueueByBusinessId(businessId);
	}
	
	@PatchMapping("/{queueId}")
	public ResponseEntity<ResponseStructure<Queue>> updateQUeue(@PathVariable Integer queueId,@RequestBody Map<String, Object> map){
		return queueService.updateQUeue(queueId,map);
	}
	
	@PatchMapping("/status/{queueId}/{newStatus}")
	public ResponseEntity<ResponseStructure<Queue>> updateQUeueStatus(@PathVariable Integer queueId,@PathVariable QueueStatus newStatus){
		return queueService.updateQUeueStatus(queueId,newStatus);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
