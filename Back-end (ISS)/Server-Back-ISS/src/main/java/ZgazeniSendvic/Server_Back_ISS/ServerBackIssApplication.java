package ZgazeniSendvic.Server_Back_ISS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServerBackIssApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerBackIssApplication.class, args);
	}

}
