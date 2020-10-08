package com.springboot.ctrl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.model.CurrencyRate;
import com.springboot.service.KafkaReceiver;

@RestController
public class KafkaConsumerController {

		@Autowired
		private KafkaReceiver receiver;
		
		@RequestMapping("/kafkaConsumer")
		public String consume(){
			CurrencyRate currencyRate = new CurrencyRate();
			currencyRate.setCurrency("RON");
			currencyRate.setRate(Double.MAX_VALUE);
			receiver.receiveData(currencyRate);
			return "Data received from Kafka" + currencyRate;
		}
}
