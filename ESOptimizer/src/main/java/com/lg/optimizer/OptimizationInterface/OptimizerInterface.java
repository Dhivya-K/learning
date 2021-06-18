package com.lg.optimizer.OptimizationInterface;

import java.io.IOException;

import org.elasticsearch.client.RestClient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface OptimizerInterface {
	
	public void changeShards() throws JsonParseException, JsonMappingException, IOException;
	public void getESshards(RestClient lowLevelClient,String index) throws Exception;

}
