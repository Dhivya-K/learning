package com.lg.optimizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.lg.optimizer.OptimizerService.ESOptimzer;
import com.lg.optimizer.model.OptimizeIndex;

public class Optimizer {

	public static void main(String[] args) throws Exception {
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		ESOptimzer esOptimzer = new ESOptimzer();

		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("", ""));
		RestClientBuilder builder = RestClient.builder(new HttpHost("", 9200, ""))
				.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
					public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
						return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
					}
				});

		RestHighLevelClient client = new RestHighLevelClient(builder);
		RestClient lowLevelClient = client.getLowLevelClient();

		List<String> esindex = new ArrayList<String>();
		String indices = "pan_prd_stb_stability_memory_usage_v1,ie_prd_stb_flat_routing_main_v1,pan_prd_stb_stability_tuner_v1,pan_prd_stb_stability_connectivity_v1,gb_prd_dlar_knowsley_cadent_adrouter_log_v1,gb_prd_stb_topprocesses_v1,gb_prd_stb_flat_es_routing_auxiliary_2_v1,panpre_prd_oesp_ingress_v3,gb_uservices_logs";
		esindex = Arrays.asList(indices.split(","));
		for (String index : esindex) {

			esOptimzer.getESshards(lowLevelClient, index);
		}
		
		//dailyshards
		String index = "pan_prd_telemetry_juniper_devices_log_raw_v1";
		esOptimzer.getESDailyshards(lowLevelClient, index);
		client.close();
		System.exit(1);
//		extractedIndexInfo(index);
	}

	private static void extractedIndexInfo(String index) throws IOException {
		List<String> indices = new ArrayList<String>();
		indices.add(index);

		List<OptimizeIndex> optimizedIndices = new ArrayList<OptimizeIndex>();
		Document doc = Jsoup.connect("http://odh.obo.lgi.io/index-info").get();
		Element table = doc.select("table").first();
		Elements tbody = table.select("tbody");
		for (Element element : tbody) {
			Elements tr = element.select("tr");
			for (Element tds : tr) {

				Elements td = tds.select("td");
				for (String ind : indices) {
					if (td.text().contains(ind)) {
						OptimizeIndex optimizeIndex = new OptimizeIndex();
						for (Element data : td) {
							String text = data.text();
							if (data.hasClass("col2")) {
								optimizeIndex.granularity = text;
							}
							if (data.hasClass("col5")) {
								optimizeIndex.mem_Size = text;
								String size_string = text.replaceAll("[A-Za-z]", "");
								int size_int = (int) Float.parseFloat(size_string.trim());
								optimizeIndex.size = size_int;
								int modulo = size_int % 50;
								if (modulo > 5) {
									optimizeIndex.increase_shards = true;
								}
							}
							if (data.hasClass("col6")) {
								optimizeIndex.changegranularity = !(optimizeIndex.granularity.equalsIgnoreCase(text));
							}
							if (data.hasClass("col7")) {
								if (text.contains("add more shards")) {
									optimizeIndex.change_shards = true;
								}
							}

						}
//						if(optimizeIndex.increase_shards && optimizeIndex.change_shards) {
////							changeShards
//						}
						optimizedIndices.add(optimizeIndex);
					}
				}
			}
		}
		System.out.println(optimizedIndices.toString());
	}

}
