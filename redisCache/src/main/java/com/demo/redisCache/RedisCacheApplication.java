package com.demo.redisCache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.Serializable;
import java.time.Instant;

@SpringBootApplication
@Slf4j
@EnableCaching
public class RedisCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisCacheApplication.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner (ExpensiveService es) {
		return event -> {
			var sw = new StopWatch();
			var input = 42;
			time(es, sw, input);
			time(es, sw, input);
		};
	}

	private static ResponseDto time(ExpensiveService es, StopWatch sw, double input) throws InterruptedException {
		sw.start();
		final ResponseDto responseDto = es.performExpensiveCalculation(input);
		sw.stop();
		log.info("Got response: {} after: {}-seconds", responseDto.toString(), sw.lastTaskInfo().getTimeSeconds());
		return responseDto;
	}

}

@Service
class ExpensiveService {
	@Cacheable("expensive")
	ResponseDto performExpensiveCalculation(double input) throws InterruptedException {
		Thread.sleep(10*1000);
		return new ResponseDto("Response from input " + input + " @ " + Instant.now());
	}
}

record ResponseDto(String message) implements Serializable { }