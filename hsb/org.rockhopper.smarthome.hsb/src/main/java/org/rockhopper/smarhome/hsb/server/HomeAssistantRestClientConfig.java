package org.rockhopper.smarhome.hsb.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HomeAssistantRestClientConfig {

	@Bean
	public RestClient haRestClient() {
		return RestClient.builder()
				  .requestFactory(new HttpComponentsClientHttpRequestFactory())
				  // .messageConverters(converters -> converters.add(new MyCustomMessageConverter()))
				  .baseUrl("https://ha.rockhopper.org:8123/api")
				  // .defaultUriVariables(Map.of("variable", "foo"))
				  .defaultHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiI2Y2E3ZWYwNzQ2ODI0OTJjODRhMGEzNTEzNzFjZjQzMCIsImlhdCI6MTcwNzc1NTQ2MywiZXhwIjoyMDIzMTE1NDYzfQ.5zhA43v7AKvdJ9DpdlZQineocp_OcBX-7fHgATO86A0")
				  .defaultHeader("Content-Type","application/json")
				  // .requestInterceptor(myCustomInterceptor)
				  // .requestInitializer(myCustomInitializer)
				  .build();		
	}
}
