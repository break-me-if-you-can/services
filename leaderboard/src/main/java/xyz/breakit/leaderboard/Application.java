package xyz.breakit.leaderboard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.sleuth.SpanAdjuster;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import xyz.breakit.leaderboard.rest.FailureInjectionFilter;
import xyz.breakit.leaderboard.rest.LimiterFilter;

@SpringBootApplication
public class Application {

	@Bean
	public SpanAdjuster customSpanAdjuster() {
		return span -> span.toBuilder().name("#leaderboard/" + span.name().replace("http:/", "")).build();
	}

	@Bean("limiterFilter")
	public FilterRegistrationBean<LimiterFilter> limiterFilter() {
        LimiterFilter limiterFilter = new LimiterFilter();
        FilterRegistrationBean<LimiterFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(limiterFilter);
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE - 11);
        registrationBean.setEnabled(true);
        return registrationBean;
    }

	@Bean("failureInjectionFilter")
	public FilterRegistrationBean<FailureInjectionFilter> failureInjectionFilter(
			@Value("${leaderboard_failure_probability:0.2}") double failureProbability,
			@Value("${leaderboard_delay:700}") int leaderboardDelay
	) {
        FailureInjectionFilter failureInjectionFilter = new FailureInjectionFilter(failureProbability, leaderboardDelay);
        FilterRegistrationBean<FailureInjectionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(failureInjectionFilter);
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        registrationBean.setEnabled(true);
        return registrationBean;

    }


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
