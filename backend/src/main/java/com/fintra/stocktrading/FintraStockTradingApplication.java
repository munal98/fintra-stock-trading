package com.fintra.stocktrading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class FintraStockTradingApplication {

	public static void main(String[] args) {
		SpringApplication.run(FintraStockTradingApplication.class, args);
	}

}
