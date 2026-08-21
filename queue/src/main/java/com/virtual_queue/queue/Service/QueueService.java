package com.virtual_queue.queue.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.virtual_queue.queue.dto.Business;
import com.virtual_queue.queue.dto.ResponseStructure;
import com.virtual_queue.queue.entity.Queue;
import com.virtual_queue.queue.enums.QueueStatus;
import com.virtual_queue.queue.exceptions.IdNotAvailableException;
import com.virtual_queue.queue.exceptions.InvalidInputException;
import com.virtual_queue.queue.exceptions.NoReccordAvailableException;
import com.virtual_queue.queue.feign.BusinessServiceClient;
import com.virtual_queue.queue.repository.QueueRepository;

import feign.FeignException;

@Service
public class QueueService {

	@Autowired
	private QueueRepository queueRepository;
	
	@Autowired
	private BusinessServiceClient businessServiceClient;

	public ResponseEntity<ResponseStructure<Queue>> createQueue(Queue queue) {
		Business business;
		try {
			ResponseEntity<ResponseStructure<Business>> response = businessServiceClient.getBusinessById(queue.getBusinessId());
			if(response.getBody() == null || response.getBody().getData() == null) {
				throw new IdNotAvailableException("Business Id not Found");
			}
			business = response.getBody().getData();
		}
		catch(FeignException.NotFound e) {
			throw new IdNotAvailableException("Business Id not Found");
		}
		
		queue.setCurrentTokenNumber(0);
		queue.setAverageServiceTimeMinutes(0.0);
		queue.setBusinessId(business.getId());
		queue.setCreatedAt(LocalDateTime.now());
		queue.setUpdatedAt(LocalDateTime.now());
		
		ResponseStructure<Queue> res = new ResponseStructure<Queue>();
		res.setData(queueRepository.save(queue));
		res.setMessage("Queue Created Successfully");
		res.setStatusCode(HttpStatus.CREATED.value());
		return new ResponseEntity<ResponseStructure<Queue>>(res,HttpStatus.CREATED);
	}

	public ResponseEntity<ResponseStructure<Queue>> getQueueById(Integer queueId) {

		Optional<Queue> opt = queueRepository.findById(queueId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Queue id not Found");
		}
		ResponseStructure<Queue> res =new  ResponseStructure<Queue>();
		res.setData(opt.get());
		res.setMessage("Queue is retrived Based on id");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<Queue>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<List<Queue>>> getAllQueues() {
		List<Queue> queues = queueRepository.findAll();
		if(queues.isEmpty()) {
			throw new NoReccordAvailableException("No Queues Available");
		}
		ResponseStructure<List<Queue>> res = new ResponseStructure<List<Queue>>();
		res.setData(queues);
		res.setMessage("All queues Retrived Successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<List<Queue>>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<List<Queue>>> getQueueByBusinessId(Integer businessId) {
		List<Queue> opt = queueRepository.findByBusinessId(businessId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("No QUeue FOund With these business id ");
		}
		ResponseStructure<List<Queue>> res =new  ResponseStructure<>();
		res.setData(opt);
		res.setMessage("Queue is retrived Based on business id");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<List<Queue>>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<Queue>> updateQUeue(Integer queueId, Map<String, Object> map) {

		Optional<Queue> opt = queueRepository.findById(queueId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Queue Id not Found");
		}
		Queue queue = opt.get();
		
		for(Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object val = entry.getValue();
			switch(key) {
			case "name" :  String name = (String) val;

		    if (name == null || name.isBlank()) {
		        throw new InvalidInputException("Name is required");
		    }

		    if (name.length() < 2 || name.length() > 100) {
		        throw new InvalidInputException(
		            "Name must be between 2 and 100 characters"
		        );
		    }

		    queue.setName(name);
			break;
			case "description" : queue.setDescription((String)val);
			break;
			case "maxCapacity" : Integer maxCapacity = ((Number) val).intValue();

		    if (maxCapacity < 1) {
		        throw new InvalidInputException(
		            "Maximum capacity must be at least 1"
		        );
		    }

		    queue.setMaxCapacity(maxCapacity);
			break;
			case "defaultServiceTimeMinutes" : Integer serviceTime = ((Number) val).intValue();

		    if (serviceTime < 1) {
		        throw new InvalidInputException(
		            "Default service time must be at least 1 minute"
		        );
		    }

		    queue.setDefaultServiceTimeMinutes(serviceTime);
			break;
			}
		}
		queue.setUpdatedAt(LocalDateTime.now());
		
		ResponseStructure<Queue> res = new ResponseStructure<Queue>();
		res.setData(queueRepository.save(queue));
		res.setMessage("Queue updated Successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<Queue>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<Queue>> updateQUeueStatus(Integer queueId, QueueStatus newStatus) {
		
		Optional<Queue> opt = queueRepository.findById(queueId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("QUeue id Is not Available");
		}
//		else if(newStatus != QueueStatus.OPEN || newStatus != QueueStatus.PAUSED || newStatus != QueueStatus.CLOSED) {
//			throw new InvalidInputException("Status can't be updated");
//		}
		Queue queue = opt.get();
		if (queue.getStatus() == newStatus) {
	        throw new InvalidInputException(
	                "You are trying to update the same status which already exists");
	    }

	    // CLOSED cannot be changed
	    if (queue.getStatus() == QueueStatus.CLOSED) {
	        throw new InvalidInputException(
	                "Closed status cannot be updated");
	    }

	    // OPEN can only go to PAUSED or CLOSED
	    if (queue.getStatus() == QueueStatus.OPEN &&
	            newStatus != QueueStatus.PAUSED &&
	            newStatus != QueueStatus.CLOSED) {

	        throw new InvalidInputException(
	                "Open status can only be updated to PAUSED or CLOSED");
	    }

	    // PAUSED can only go to OPEN or CLOSED
	    if (queue.getStatus() == QueueStatus.PAUSED &&
	            newStatus != QueueStatus.OPEN &&
	            newStatus != QueueStatus.CLOSED) {

	        throw new InvalidInputException(
	                "Paused status can only be updated to OPEN or CLOSED");
	    }

		
		queue.setStatus(newStatus);
		
		ResponseStructure<Queue> res = new ResponseStructure<Queue>();
		res.setData(queueRepository.save(queue));
		res.setMessage("Queue updated Successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<Queue>>(res,HttpStatus.OK);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
