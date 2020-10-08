package com.springboot.ctrl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.springboot.model.CurrencyRate;
import com.springboot.service.KafkaSender;

@RestController
public class KafkaProducerController {

	@Autowired
	private KafkaSender sender;
	
	private List<CurrencyRate> currencyRates;
	private String currencyUrl = "https://api.exchangeratesapi.io/latest";
	
	@RequestMapping("/kafkaProducer")
	public String sendData(@RequestParam(name = "currency") String currency) {
		if (this.currencyRates == null) {
			this.currencyRates = initAllCurrencyRates();
		}
		
		CurrencyRate notFound = new CurrencyRate();
		notFound.setCurrency(currency + " not found!");
		
		CurrencyRate currentCurrencyRate = this.currencyRates.stream()
				.filter(x -> x.getCurrency() != null && x.getCurrency().equalsIgnoreCase(currency))
				.findFirst()
				.orElse(notFound);
	
		sender.sendData(currentCurrencyRate);
		return "CurrencyRate has been sent to Kafka: " + currentCurrencyRate;
	}
	
	private List<CurrencyRate> initAllCurrencyRates() {
		String json = this.jsonGetRequest(this.currencyUrl);
		
		if (json == null) {
			return null;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = mapper.readTree(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<CurrencyRate> allCurrencyRates = new ArrayList<CurrencyRate>();
        JsonNode firstNode = rootNode.get("rates");
        
        Map<String, Double> map = new HashMap<>();
        addKeys("", firstNode, map, new ArrayList<>());

        for (Map.Entry<String, Double> entry : map.entrySet()) {
        	CurrencyRate currentCurrencyRate = new CurrencyRate();
        	currentCurrencyRate.setCurrency(entry.getKey());
        	currentCurrencyRate.setRate(entry.getValue());
        	currentCurrencyRate.setBase(rootNode.get("base").asText());
        	allCurrencyRates.add(currentCurrencyRate);
        }
               
        return allCurrencyRates;
	}
	
	private String jsonGetRequest(String urlQueryString) {
		String json = null;
		try {
			URL url = new URL(urlQueryString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.setDoOutput(true);
		    connection.setInstanceFollowRedirects(false);
		    connection.setRequestMethod("GET");
		    connection.setRequestProperty("Content-Type", "application/json");
		    connection.setRequestProperty("charset", "utf-8");
		    connection.connect();
		    InputStream inStream = connection.getInputStream();
		    json = streamToString(inStream); // input stream to string
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
			return json;
	}
	  
	  private String streamToString(InputStream inputStream) {
		  @SuppressWarnings("resource")
		  String text = new Scanner(inputStream, "UTF-8").useDelimiter("\\Z").next();
		  return text;
	  }
	  
	  private void addKeys(String currentPath, JsonNode jsonNode, Map<String, Double> map, List<Integer> suffix) {
		    if (jsonNode.isObject()) {
		        ObjectNode objectNode = (ObjectNode) jsonNode;
		        Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
		        String pathPrefix = currentPath.isEmpty() ? "" : currentPath + "-";

		        while (iter.hasNext()) {
		            Map.Entry<String, JsonNode> entry = iter.next();
		            addKeys(pathPrefix + entry.getKey(), entry.getValue(), map, suffix);
		        }
		    } else if (jsonNode.isArray()) {
		        ArrayNode arrayNode = (ArrayNode) jsonNode;

		        for (int i = 0; i < arrayNode.size(); i++) {
		            suffix.add(i + 1);
		            addKeys(currentPath, arrayNode.get(i), map, suffix);

		            if (i + 1 <arrayNode.size()){
		                suffix.remove(arrayNode.size() - 1);
		            }
		        }
		    } else if (jsonNode.isValueNode()) {
		        if (currentPath.contains("-")) {
		            for (int i = 0; i < suffix.size(); i++) {
		                currentPath += "-" + suffix.get(i);
		            }

		            suffix = new ArrayList<>();
		        }

		        ValueNode valueNode = (ValueNode) jsonNode;
		        map.put(currentPath, valueNode.asDouble());
		    }
		}
}
