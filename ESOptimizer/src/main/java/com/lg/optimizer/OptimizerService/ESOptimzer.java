package com.lg.optimizer.OptimizerService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lg.optimizer.OptimizationInterface.OptimizerInterface;

public class ESOptimzer implements OptimizerInterface {

	public void changeShards() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();

		JSONObject jsonObject = objectMapper.readValue(
				new File("/home/dkandasamy/ODH/ansible-odh-pipelines/roles/elasticsearch-config/uservices_logs.json"),
				JSONObject.class);

	}

	public void getESshards(RestClient lowLevelClient, String index) throws Exception {
		String response22 = getShardDetails(lowLevelClient, index + "-2021w22");
		String response23 = getShardDetails(lowLevelClient, index + "-2021w23");
		processShards(response22, response23, index);
	}

	public void getESDailyshards(RestClient lowLevelClient, String index) throws Exception {
		String response22 = getShardDetails(lowLevelClient, index + "-2021.06.16");
		String response23 = getShardDetails(lowLevelClient, index + "-2021.06.15");
		String response24 = getShardDetails(lowLevelClient, index + "-2021.06.14");
		String response25 = getShardDetails(lowLevelClient, index + "-2021.06.17");

		processShards(response22, response23,response24, response25, index);
	}

	private String getShardDetails(RestClient lowLevelClient, String index) throws IOException {
		Request request = new Request("GET", "_cat/indices/" + index + "?v&format=json");
		request.addParameter("pretty", "true");
		Response response = lowLevelClient.performRequest(request);
		System.out.println(index);

		String responseBody = EntityUtils.toString(response.getEntity());
		System.out.println(responseBody);
		return responseBody;
	}

	private void processShards(String httpEntity, String httpEntity2, String index) throws IOException {
		List jsonObject = jsonmapper(httpEntity);

//		JSONObject jsonObject = new JSONObject();
		List<String> myList = new ArrayList<String>(Arrays.asList(index));
		Map<String, Object> object = (Map<String, Object>) jsonObject.get(0);
//		object = object.put("index_patterns", myList);
		String size = String.valueOf(object.get("pri.store.size"));
		String shard_count = String.valueOf(object.get("pri"));
		float size_int = convertToInt(size);

		List jsonObject2 = jsonmapper(httpEntity2);
		Map<String, Object> object2 = (Map<String, Object>) jsonObject2.get(0);

		String size23 = String.valueOf(object2.get("pri.store.size"));
		float size23_int = convertToInt(size23);

		float avg = (size23_int + size_int) / 2;
		createTemplate(index, shard_count, avg);

	}
	
	private void processShards(String httpEntity, String httpEntity2, String response3, String response4, String index) throws IOException {
		List jsonObject = jsonmapper(httpEntity);

//		JSONObject jsonObject = new JSONObject();
		List<String> myList = new ArrayList<String>(Arrays.asList(index));
		Map<String, Object> object = (Map<String, Object>) jsonObject.get(0);
//		object = object.put("index_patterns", myList);
		String size = String.valueOf(object.get("pri.store.size"));
		String shard_count = String.valueOf(object.get("pri"));
		float size_int = convertToInt(size);

		List jsonObject2 = jsonmapper(httpEntity2);
		Map<String, Object> object2 = (Map<String, Object>) jsonObject2.get(0);

		String size23 = String.valueOf(object2.get("pri.store.size"));
		float size23_int = convertToInt(size23);
		List jsonObject3 = jsonmapper(response3);
		Map<String, Object> object3 = (Map<String, Object>) jsonObject3.get(0);

		String size24 = String.valueOf(object2.get("pri.store.size"));
		float size24_int = convertToInt(size24);
		List jsonObject4 = jsonmapper(response3);
		Map<String, Object> object4 = (Map<String, Object>) jsonObject4.get(0);

		String size25= String.valueOf(object2.get("pri.store.size"));
		float size25_int = convertToInt(size25);

		float avg = (size23_int + size_int+size24_int+size25_int) / 4;
		createTemplate(index, shard_count, avg);

	}

	private float convertToInt(String size) {
		String size_string = size.replaceAll("[A-Za-z]", "");
		float size_int = Float.parseFloat(size_string.trim());
		return size_int;
	}

	private List jsonmapper(String httpEntity) throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(Feature.ALLOW_COMMENTS, true);
		objectMapper.configure(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
		System.out.println("httpEntity");
		System.out.println(httpEntity);

		List jsonObject = objectMapper.readValue((httpEntity), List.class);
		return jsonObject;
	}

	private void createTemplate(String index, String shard_count, float avg) throws IOException {
		float modulo = avg % (float) 50.00;
		if (modulo > 10) {
			int new_shard = (int) (avg / 50) ;
//			if(new_shard < 7) {
			createTemplate(new_shard, index);
//			}
		}
	}

	private void changeGranularity(int new_shard, String index) {
		
	}

	private void createTemplate(int new_shard, String index) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();

		Map<String, Object> jsonObject = objectMapper.readValue(
				new FileReader("/home/dkandasamy/eclipse-workspace/ESOptimizer/src/main/resources/template.json"),
				Map.class);
//		JSONObject jsonObject = new JSONObject();
		List<String> myList = new ArrayList<String>(Arrays.asList(index + "*"));
		jsonObject.put("index_patterns", myList);
		Map<String, Object> settings = (Map<String, Object>) jsonObject.get("settings");
		Map<String, Object> indexMap = (Map<String, Object>) settings.get("index");
		indexMap.put("number_of_shards", new_shard);
		String template = "/home/dkandasamy/ODH/ansible-odh-pipelines/roles/elasticsearch-config/templates/es6_template/pipelines/" + index + "_sharding.json";

		// create object mapper instance
		ObjectMapper mapper = new ObjectMapper();

		// convert map to JSON file
		mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get(template).toFile(), jsonObject);
		System.out.println("file written to " + template);
		System.out.println(jsonObject);

	}

}
