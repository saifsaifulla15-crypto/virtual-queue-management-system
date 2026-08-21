package com.virtual_queue.queue.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtual_queue.queue.Service.ServiceRecordService;
import com.virtual_queue.queue.dto.QueueAnalytics;
import com.virtual_queue.queue.dto.ResponseStructure;
import com.virtual_queue.queue.entity.ServiceRecord;

@RestController
@RequestMapping("/serviceRecords")
public class ServiceRecordController {

	@Autowired
	private ServiceRecordService serviceRecordService;
	
	@GetMapping("/{id}")
	public ResponseEntity<ResponseStructure<ServiceRecord>> getServiceRecordById(@PathVariable Integer id){
		return serviceRecordService.getServiceRecordById(id);
	}
	
	@GetMapping("/queue/{queueId}")
	public ResponseEntity<ResponseStructure<List<ServiceRecord>>> getServiceRecordsByQueueId(@PathVariable Integer queueId){
		return serviceRecordService.getServiceRecordsByQueueId(queueId);
	}
	
	@GetMapping("/business/{businessId}")
	public ResponseEntity<ResponseStructure<List<ServiceRecord>>> getserviceRecordsByBusienssID(@PathVariable Integer businessId){
		return serviceRecordService.getserviceRecordsByBusienssID(businessId);
	}
	
	@GetMapping("/{queueId}/today")
	public ResponseEntity<ResponseStructure<List<ServiceRecord>>> getTodayRecords(@PathVariable Integer queueId){
		return serviceRecordService.getTodayRecords(queueId);
	}
	
	@GetMapping("/queue/analytics/{queueId}")
	public ResponseEntity<ResponseStructure<QueueAnalytics>> analyseQueue(@PathVariable Integer queueId){
		return serviceRecordService.analyseQueue(queueId);
	}
	
}
