package org.rockhopper.smarhome.hsb.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HomeAssistantRestClientConfig {

	@Autowired
	private HaConfig haConfig;
	
	@Bean
	public RestClient haRestClient() {
		return RestClient.builder()
				  .requestFactory(new HttpComponentsClientHttpRequestFactory())
				  // .messageConverters(converters -> converters.add(new MyCustomMessageConverter()))
				  .baseUrl("%s://%s:%s/api".formatted(haConfig.getProtocol(),haConfig.getHost(),haConfig.getPort()))
				  // .defaultUriVariables(Map.of("variable", "foo"))
				  .defaultHeader("Authorization", "Bearer %s".formatted(haConfig.getBearer()))
				  .defaultHeader("Content-Type","application/json")
				  // .requestInterceptor(myCustomInterceptor)
				  // .requestInitializer(myCustomInitializer)
				  .build();		
	}
}
