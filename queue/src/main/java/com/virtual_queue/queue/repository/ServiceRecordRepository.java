package com.virtual_queue.queue.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.virtual_queue.queue.entity.ServiceRecord;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Integer>{

	@Query("SELECT AVG(s.serviceDurationMinutes)FROM ServiceRecord s WHERE s.queueToken.queue.id = :queueId")
		Double findAverageServiceTimeByQueueId(Integer queueId);
	
	List<ServiceRecord> findByQueueTokenQueueId(Integer queueId);
	
	List<ServiceRecord> findByQueueTokenQueueIdAndCreatedAtBetween( Integer queueId, LocalDateTime start, LocalDateTime end	);
	
	Integer countByQueueTokenQueueId(Integer queueId);

	@Query("SELECT AVG(s.waitingDurationMinutes) FROM ServiceRecord s WHERE s.queueToken.queue.id = :queueId")
	Double findAverageWaitingTimeByQueueId(Integer queueId);

	@Query("SELECT MAX(s.waitingDurationMinutes) FROM ServiceRecord s WHERE s.queueToken.queue.id = :queueId")
	Double findMaximumWaitingTimeByQueueId(Integer queueId);

	@Query("SELECT MIN(s.waitingDurationMinutes) FROM ServiceRecord s WHERE s.queueToken.queue.id = :queueId")
	Double findMinimumWaitingTimeByQueueId(Integer queueId);
}
