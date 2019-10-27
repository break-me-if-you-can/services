package xyz.breakit.game.leaderboard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.breakit.game.leaderboard.rest.LimiterFilter;


@Configuration
public class ConcurrencyLimitConfiguration {

    @Bean
    public LimiterFilter limiterFilter() {
        return new LimiterFilter();
    }
}
