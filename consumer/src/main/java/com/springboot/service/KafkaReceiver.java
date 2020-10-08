package com.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.springboot.model.CurrencyRate;

@Service
public class KafkaReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaReceiver.class);

	@KafkaListener(topics = "${kafka.topic.name}", group = "${kafka.consumer.group.id}")
	public void receiveData(CurrencyRate currencyRate) {
		LOGGER.info("Data - " + currencyRate + " received");
	}
}
