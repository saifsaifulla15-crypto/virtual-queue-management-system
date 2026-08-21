package com.virtual_queue.queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class QueueApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueueApplication.class, args);
	}

}
