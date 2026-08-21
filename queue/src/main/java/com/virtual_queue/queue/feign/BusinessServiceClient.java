package com.virtual_queue.queue.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.virtual_queue.queue.dto.Business;
import com.virtual_queue.queue.dto.ResponseStructure;
import com.virtual_queue.queue.dto.StaffDto;



@FeignClient("BUSINESS")
public interface BusinessServiceClient {

	@GetMapping("/business/get/{id}")
	public ResponseEntity<ResponseStructure<Business>> getBusinessById(@PathVariable Integer id);
	
	@GetMapping("/staff/{staffId}")
	public ResponseEntity<ResponseStructure<StaffDto>> getStaffById(@PathVariable Integer staffId);
}
