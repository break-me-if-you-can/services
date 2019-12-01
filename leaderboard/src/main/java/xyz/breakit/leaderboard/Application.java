package xyz.breakit.leaderboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.SpanAdjuster;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	@Bean
	public SpanAdjuster customSpanAdjuster() {
		return span -> span.toBuilder().name("#leaderboard/" + span.name().replace("http:/", "")).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
