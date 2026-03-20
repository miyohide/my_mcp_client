package com.github.miyohide.mymcp;

import com.github.miyohide.mymcp.config.McpConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(McpConfig.class)
public class MymcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(MymcpApplication.class, args);
	}

}
