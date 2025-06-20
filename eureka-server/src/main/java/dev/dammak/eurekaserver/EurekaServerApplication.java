package dev.dammak.eurekaserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

	public static void main(String[] args) throws UnknownHostException {
		SpringApplication app = new SpringApplication(EurekaServerApplication.class);
		Environment env = app.run(args).getEnvironment();

		String serverPort = env.getProperty("server.port");
		String hostAddress = InetAddress.getLocalHost().getHostAddress();

		log.info("""
            
            ----------------------------------------------------------
            	Eureka Server is running! Access URLs:
            	Local: 		http://localhost:{}
            	External: 	http://{}:{}
            	Profile(s): 	{}
            ----------------------------------------------------------
            """,
				serverPort,
				hostAddress,
				serverPort,
				env.getActiveProfiles().length == 0 ?
						env.getDefaultProfiles() : env.getActiveProfiles()
		);
	}

}
