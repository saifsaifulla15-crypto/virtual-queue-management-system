package com.virtual_queue.queue.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.virtual_queue.queue.dto.QueueStatusResponse;
import com.virtual_queue.queue.dto.ResponseStructure;
import com.virtual_queue.queue.dto.Staff;
import com.virtual_queue.queue.dto.StaffDto;
import com.virtual_queue.queue.dto.Users;
import com.virtual_queue.queue.entity.Queue;
import com.virtual_queue.queue.entity.QueueToken;
import com.virtual_queue.queue.entity.ServiceRecord;
import com.virtual_queue.queue.enums.CapacityType;
import com.virtual_queue.queue.enums.QueueStatus;
import com.virtual_queue.queue.enums.ROLE;
import com.virtual_queue.queue.enums.TokenStatus;
import com.virtual_queue.queue.exceptions.IdNotAvailableException;
import com.virtual_queue.queue.exceptions.InvalidInputException;
import com.virtual_queue.queue.feign.BusinessServiceClient;
import com.virtual_queue.queue.feign.UserServiceClient;
import com.virtual_queue.queue.repository.QueueRepository;
import com.virtual_queue.queue.repository.QueueTokenRepository;
import com.virtual_queue.queue.repository.ServiceRecordRepository;

import feign.FeignException;

@Service
public class QueueTokenService {

	@Autowired
	private QueueTokenRepository queueTokenRepository;
	
	@Autowired
	private QueueRepository queueRepository;

	@Autowired
	private UserServiceClient userServiceClient;
	
	@Autowired
	private BusinessServiceClient businessServiceClient;
	
	@Autowired
	private ServiceRecordRepository serviceRecordRepository;
	
	public ResponseEntity<ResponseStructure<QueueToken>> joinQueue(Integer queueId, Integer userId) {
		Optional<Queue> opt = queueRepository.findById(queueId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("QueueId not found");
		}
		Queue queue = opt.get();
		if(queue.getStatus() == QueueStatus.PAUSED) {
			throw new InvalidInputException("Cant join the queue when queue is paused");
		}
		else if(queue.getStatus() == QueueStatus.CLOSED) {
			throw new InvalidInputException("Cant join the queue, queue is closed");
		}
		else if(queue.getCapacityType() == CapacityType.TOKEN_LIMIT) {
			if(queue.getMaxCapacity()<= queue.getCurrentTokenNumber()) {
				throw new InvalidInputException("Queue is reached to maximum capacity");
			}
		}
		Integer activeTokens = queueTokenRepository.countByQueueIdAndStatusIn(queueId, List.of(TokenStatus.WAITING,TokenStatus.CALLED,TokenStatus.IN_SERVICE));
		if(queue.getCapacityType() == CapacityType.ACTIVE_CUSTOMERS) {
			if(queue.getMaxCapacity() <= activeTokens) {
				throw new InvalidInputException("Queue is reached to maximum active tokens plese try later");
			}
		}
		Users users;
		try {
			ResponseEntity<ResponseStructure<Users>> response = userServiceClient.getUserById(userId);
			if(response.getBody() == null || response.getBody().getData() == null) {
				throw new IdNotAvailableException("User ID not found");
			}
			users = response.getBody().getData();
		}
		catch(FeignException.NotFound e) {
			throw new IdNotAvailableException("User id not found");
		}
		if (queueTokenRepository.existsByUserIdAndQueueIdAndStatusIn(userId,queueId,List.of(TokenStatus.WAITING, TokenStatus.CALLED,TokenStatus.IN_SERVICE))) {

		    throw new InvalidInputException(
		        "User is already in the queue"
		    );
		}
		if(users.getRole() != ROLE.CUSTOMER) {
			throw new InvalidInputException("User is not a customer");
		}
		Integer currentToken = queue.getCurrentTokenNumber()+1;
		QueueToken token = new QueueToken();
		token.setTokenNumber(currentToken);
		token.setStatus(TokenStatus.WAITING);
		token.setUserId(userId);
		token.setJoinedAt(LocalDateTime.now());
		
		queue.setCurrentTokenNumber(currentToken);
		
		token.setQueue(queue);
		if(queue.getCapacityType() == CapacityType.TOKEN_LIMIT) {
			if(queue.getMaxCapacity() == currentToken) {
				queue.setStatus(QueueStatus.CLOSED);
			}
		}
		queueRepository.save(queue);
		
		ResponseStructure<QueueToken> res = new ResponseStructure<QueueToken>();
		res.setData(queueTokenRepository.save(token));
		res.setMessage("Token is taken , joined the queue successfully");
		res.setStatusCode(HttpStatus.CREATED.value());
		return new ResponseEntity<ResponseStructure<QueueToken>>(res,HttpStatus.CREATED);
	}

	public ResponseEntity<ResponseStructure<QueueStatusResponse>> getUpdatedQueueStatus(Integer tokenId) {
		Optional<QueueToken> opt = queueTokenRepository.findById(tokenId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Token id not Found");
		}
		QueueToken token = opt.get();
		Integer QueueId = token.getQueue().getId();
		TokenStatus tokenStatus = token.getStatus();
		
		if(tokenStatus == TokenStatus.COMPLETED) throw new InvalidInputException("your Service is completed SuccessFully");
		else if(tokenStatus == TokenStatus.CANCELLED) throw new InvalidInputException("Your token is cancelled");
		else if(tokenStatus == TokenStatus.SKIPPED) throw new InvalidInputException("your token is skipped");
		else if(tokenStatus == TokenStatus.IN_SERVICE) throw new InvalidInputException("for you service is ongoing");
		else if(tokenStatus == TokenStatus.CALLED) throw new InvalidInputException("you are called, you are service is ready to start");
		
		
		Double time = token.getQueue().getAverageServiceTimeMinutes();
		if(time == null || time == 0) {
			time = token.getQueue().getDefaultServiceTimeMinutes() + 0.0;
		}

		Optional<QueueToken> currentToken = queueTokenRepository.findFirstByQueueIdAndStatus(QueueId,TokenStatus.IN_SERVICE);
		Long currentTime;
		if(currentToken.isEmpty()) {
			currentTime = (long) 0;
		}
		else {
			currentTime = Duration.between((currentToken.get().getServiceStartedAt() == null) ? LocalDateTime.now():currentToken.get().getServiceStartedAt(), LocalDateTime.now()).toMinutes();
		}
		
		Long peopleAhead =queueTokenRepository.countByQueueIdAndStatusAndTokenNumberLessThan(QueueId, TokenStatus.WAITING, token.getTokenNumber());
		
		Integer remainingTime = (int) Math.max(0,Math.round(time - currentTime));
		Integer estimatedTime =  (int) (remainingTime + (peopleAhead *time));
		
		QueueStatusResponse response = new QueueStatusResponse();
		response.setTokenNumber(token.getTokenNumber());
		response.setStatus(tokenStatus);
		response.setAverageServiceTimeMinutes(time);
		response.setEstimatedWaitingMinutes(estimatedTime);
		response.setPeopleAhead(peopleAhead.intValue());
		
		ResponseStructure<QueueStatusResponse> res = new ResponseStructure<QueueStatusResponse>();
		res.setData(response);
		res.setMessage("Estimated time calculated Successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<QueueStatusResponse>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<QueueToken>> callNextToken(Integer queueId) {

		Optional<Queue> opt = queueRepository.findById(queueId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Queue Id NotFOund");
		}
		
		Optional<QueueToken> copt = queueTokenRepository.findFirstByQueueIdAndStatus(queueId, TokenStatus.IN_SERVICE);
		if(copt.isPresent()) {
			throw new InvalidInputException("There is Customer in Service, when Serving you cant call Next customer");
		}
		Optional<QueueToken> topt = queueTokenRepository.findFirstByQueueIdAndStatusOrderByTokenNumberAsc(queueId, TokenStatus.WAITING);
		if(topt.isEmpty()) {
			throw new InvalidInputException("There is no next customer waiting");
		}
		QueueToken queueToken = topt.get();
		queueToken.setStatus(TokenStatus.CALLED);
		queueToken.setCalledAt(LocalDateTime.now());
		
		ResponseStructure<QueueToken> res = new ResponseStructure<QueueToken>();
		res.setData(queueTokenRepository.save(queueToken));
		res.setMessage("called the next customer successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<QueueToken>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<QueueToken>> startService(Integer tokenId) {
		Optional<QueueToken> opt = queueTokenRepository.findById(tokenId);
		if(opt.isEmpty()) {
			throw new InvalidInputException("TokenId Not Found");
		}
		
		QueueToken token = opt.get();
		
		if (token.getStatus() != TokenStatus.CALLED) {
		    throw new InvalidInputException("Only a called token can start service");
		}
		
		token.setStatus(TokenStatus.IN_SERVICE);
		token.setServiceStartedAt(LocalDateTime.now());
		ResponseStructure<QueueToken> res = new ResponseStructure<QueueToken>();
		res.setData(queueTokenRepository.save(token));
		res.setMessage("Service Started successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<QueueToken>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<QueueToken>> completeService(Integer tokenId, Integer staffId) {
		
		Optional<QueueToken> qopt = queueTokenRepository.findById(tokenId);
		if(qopt.isEmpty()) {
			throw new IdNotAvailableException("Token Id not Found");
		}
		QueueToken token = qopt.get();
		if (token.getStatus() != TokenStatus.IN_SERVICE) {
		    throw new InvalidInputException("Only a token currently in service can be completed");
		}
		Queue queue = token.getQueue();
		Integer businessId = queue.getBusinessId();
		StaffDto staff;
		try {
			ResponseEntity<ResponseStructure<StaffDto>> response = businessServiceClient.getStaffById(staffId);
			if(response.getBody() == null || response.getBody().getData() == null) {
				throw new IdNotAvailableException("Staff not found");
			}
			staff = response.getBody().getData();
		}
		catch(FeignException.NotFound e) {
			throw new IdNotAvailableException("Staff not found");
		}
		if (staff.getBusinessId() == null) {
		    throw new InvalidInputException("Staff is not associated with any business");
		}

		if(!businessId.equals(staff.getBusinessId())) {
			throw new InvalidInputException("staff is doesnot belong to this business");
		}
		else if(!Boolean.TRUE.equals(staff.getActive())) {
			throw new InvalidInputException("Staff is InAvative ");
		}
		
		token.setStatus(TokenStatus.COMPLETED);
		token.setServiceCompletedAt(LocalDateTime.now());
		
		ServiceRecord record = new ServiceRecord();
		record.setStaffId(staffId);
		record.setServiceStartTime(token.getServiceStartedAt());
		record.setServiceEndTime(token.getServiceCompletedAt());
		
		Double serviceDuaration = Duration.between(token.getServiceStartedAt(), token.getServiceCompletedAt()).toMinutes() + 0.0;
		Double waitingTime = Duration.between(token.getJoinedAt(), token.getServiceStartedAt()).toMinutes() +0.0;
		
		record.setServiceDurationMinutes(serviceDuaration);
		record.setWaitingDurationMinutes(waitingTime);
		record.setCreatedAt(LocalDateTime.now());
		record.setQueueToken(token);
		serviceRecordRepository.save(record);
		
		Double avgTime = serviceRecordRepository.findAverageServiceTimeByQueueId(queue.getId());
		
		queue.setAverageServiceTimeMinutes(avgTime);
		queueRepository.save(queue);
		
		record.setServiceDurationMinutes(serviceDuaration);
		record.setWaitingDurationMinutes(waitingTime);
		
		token.setServiceRecord(record);
		ResponseStructure<QueueToken> res = new ResponseStructure<QueueToken>();
		res.setData(queueTokenRepository.save(token));
		res.setMessage("Service completed SuccessFully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<QueueToken>>(res,HttpStatus.OK);
		
		
	}

	public ResponseEntity<ResponseStructure<QueueToken>> cancelToken(Integer tokenId) {

		Optional<QueueToken> opt = queueTokenRepository.findById(tokenId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Token Id not found");
		}
		
		QueueToken token = opt.get();
		if(token.getStatus() != TokenStatus.WAITING) {
			throw new InvalidInputException("we cant cancel the token at this stage");
		}
		token.setStatus(TokenStatus.CANCELLED);
		ResponseStructure<QueueToken> res = new ResponseStructure<QueueToken>();
		res.setData(queueTokenRepository.save(token));
		res.setMessage("token cancelled successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<QueueToken>>(res,HttpStatus.OK);
	}

	public ResponseEntity<ResponseStructure<QueueToken>> skipToken(Integer tokenId) {
		Optional<QueueToken> opt = queueTokenRepository.findById(tokenId);
		if(opt.isEmpty()) {
			throw new IdNotAvailableException("Token Id not found");
		}
		
		QueueToken token = opt.get();
		if(token.getStatus() != TokenStatus.CALLED) {
			throw new InvalidInputException("we cant skip  the token without calling");
		}
		token.setStatus(TokenStatus.SKIPPED);
		ResponseStructure<QueueToken> res = new ResponseStructure<QueueToken>();
		res.setData(queueTokenRepository.save(token));
		res.setMessage("token skipped successfully");
		res.setStatusCode(HttpStatus.OK.value());
		return new ResponseEntity<ResponseStructure<QueueToken>>(res,HttpStatus.OK);
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
