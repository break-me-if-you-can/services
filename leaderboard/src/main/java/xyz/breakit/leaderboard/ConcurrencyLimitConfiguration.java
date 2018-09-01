package xyz.breakit.leaderboard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.breakit.leaderboard.rest.LimiterFilter;

import javax.servlet.Filter;


@Configuration
public class ConcurrencyLimitConfiguration {

    @Bean
    public LimiterFilter limiterFilter() {
        return new LimiterFilter();
    }
}
