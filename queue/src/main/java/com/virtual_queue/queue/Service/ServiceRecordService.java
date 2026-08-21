package com.virtual_queue.queue.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.virtual_queue.queue.dto.QueueAnalytics;
import com.virtual_queue.queue.dto.ResponseStructure;
import com.virtual_queue.queue.entity.Queue;
import com.virtual_queue.queue.entity.QueueToken;
import com.virtual_queue.queue.entity.ServiceRecord;
import com.virtual_queue.queue.enums.TokenStatus;
import com.virtual_queue.queue.exceptions.IdNotAvailableException;
import com.virtual_queue.queue.exceptions.NoReccordAvailableException;
import com.virtual_queue.queue.repository.QueueRepository;
import com.virtual_queue.queue.repository.QueueTokenRepository;
import com.virtual_queue.queue.repository.ServiceRecordRepository;

@Service
public class ServiceRecordService {

	@Autowired
	private ServiceRecordRepository serviceRecordRepository;
	
	@Autowired
	private QueueRepository queueRepository;

	@Autowired
	private QueueTokenRepository queueTokenRepository;
	
	public ResponseEntity<ResponseStructure<ServiceRecord>> getServiceRecordById(Integer id) {

		Optional<ServiceRecord> opt = serviceRecordRepository.findById(id);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Service Record id not Found");
		}
		ResponseStructure<ServiceRecord> res = new ResponseStructure<ServiceRecord>();
		res.setData(opt.get());
		res.setMessage("service record retrived successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<ServiceRecord>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<List<ServiceRecord>>> getServiceRecordsByQueueId(Integer queueId) {

		List<ServiceRecord> records = serviceRecordRepository.findByQueueTokenQueueId(queueId);
		if(records.isEmpty()) {
			throw new NoReccordAvailableException("No Service Records Available");
		}
		ResponseStructure<List<ServiceRecord>> res = new ResponseStructure<List<ServiceRecord>>();
		res.setData(records);
		res.setMessage("service reccords based on queueId retrived successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<List<ServiceRecord>>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<List<ServiceRecord>>> getserviceRecordsByBusienssID(Integer businessId) {

		List<Queue> queues = queueRepository.findByBusinessId(businessId);
		if(queues.isEmpty()) {
			throw new NoReccordAvailableException("no Queues Available for this business");
		}
		List<ServiceRecord> records = new ArrayList<ServiceRecord>();
		for(Queue q : queues) {
			records.addAll(serviceRecordRepository.findByQueueTokenQueueId(q.getId()));
		}
		if(records.isEmpty()) {
			throw new NoReccordAvailableException("No records Available for this business");
		}
		ResponseStructure<List<ServiceRecord>> res = new ResponseStructure<List<ServiceRecord>>();
		res.setData(records);
		res.setMessage("service Records retrived successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<List<ServiceRecord>>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<List<ServiceRecord>>> getTodayRecords(Integer queueId) {

		List<ServiceRecord> records = serviceRecordRepository.findByQueueTokenQueueIdAndCreatedAtBetween(queueId, LocalDate.now().atStartOfDay(),LocalDate.now().plusDays(1).atStartOfDay());
		if(records.isEmpty()) {
			throw new NoReccordAvailableException("No Services done today");
		}
		ResponseStructure<List<ServiceRecord>> res = new ResponseStructure<List<ServiceRecord>>();
		res.setData(records);
		res.setMessage("Today Records Retrived Successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<List<ServiceRecord>>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<QueueAnalytics>> analyseQueue(Integer queueId) {
		
		QueueAnalytics analytics = new QueueAnalytics();
		analytics.setTotalCustomersServed(serviceRecordRepository.countByQueueTokenQueueId(queueId));
		analytics.setCancelled(queueTokenRepository.countByQueueIdAndStatus(queueId, TokenStatus.CANCELLED));
		analytics.setSkipped(queueTokenRepository.countByQueueIdAndStatus(queueId, TokenStatus.SKIPPED));
		analytics.setTotalTokens(analytics.getTotalCustomersServed()+analytics.getCancelled()+analytics.getSkipped());
		analytics.setAverageServingTime(serviceRecordRepository.findAverageServiceTimeByQueueId(queueId));
		analytics.setAverageWaitingTime(serviceRecordRepository.findAverageWaitingTimeByQueueId(queueId));
		analytics.setMaximumWaitingTime(serviceRecordRepository.findMaximumWaitingTimeByQueueId(queueId));
		analytics.setMinimumWaitingTime(serviceRecordRepository.findMinimumWaitingTimeByQueueId(queueId));
		
		ResponseStructure<QueueAnalytics> res = new ResponseStructure<QueueAnalytics>();
		res.setData(analytics);
		res.setMessage("Analysed all details of the queue");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<QueueAnalytics>>(res,HttpStatus.OK);
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
