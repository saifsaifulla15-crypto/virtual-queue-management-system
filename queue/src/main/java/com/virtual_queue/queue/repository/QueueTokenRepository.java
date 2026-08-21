package com.virtual_queue.queue.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virtual_queue.queue.entity.QueueToken;
import com.virtual_queue.queue.enums.TokenStatus;

public interface QueueTokenRepository extends JpaRepository<QueueToken, Integer> {

	Boolean existsByUserIdAndQueueIdAndStatusIn(Integer userId,Integer Qid,List<TokenStatus> statuses);

	Integer countByQueueIdAndStatusIn(Integer queueId, List<TokenStatus> status);
	
	Optional<QueueToken> findFirstByQueueIdAndStatus(Integer queueId, TokenStatus status);
	
	Long countByQueueIdAndStatusAndTokenNumberLessThan(Integer queueId, TokenStatus status, Integer tokenNumber );
	
	Optional<QueueToken> findFirstByQueueIdAndStatusOrderByTokenNumberAsc(Integer queueId, TokenStatus status);
	
	Integer countByQueueIdAndStatus(Integer queueId,TokenStatus status);
}
