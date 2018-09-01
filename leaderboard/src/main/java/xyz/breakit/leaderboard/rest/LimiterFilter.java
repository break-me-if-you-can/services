package xyz.breakit.leaderboard.rest;


import com.netflix.concurrency.limits.limit.FixedLimit;
import com.netflix.concurrency.limits.limiter.DefaultLimiter;
import com.netflix.concurrency.limits.servlet.ConcurrencyLimitServletFilter;
import com.netflix.concurrency.limits.strategy.SimpleStrategy;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LimiterFilter implements Filter {

    private boolean enabled = false;
    private Filter limitingFilter = new ConcurrencyLimitServletFilter(
            DefaultLimiter.newBuilder()
                    .limit(FixedLimit.of(10))
                    .minWindowTime(1000, TimeUnit.MILLISECONDS)
                    .maxWindowTime(1000, TimeUnit.MILLISECONDS)
                    .build(new SimpleStrategy<>()));

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (enabled) {
            limitingFilter.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {

    }

    public void enable(int limit) {
        enabled = true;
        limitingFilter = new ConcurrencyLimitServletFilter(
                DefaultLimiter.newBuilder()
                        .limit(FixedLimit.of(limit))
                        .minWindowTime(1000, TimeUnit.MILLISECONDS)
                        .maxWindowTime(1000, TimeUnit.MILLISECONDS)
                        .build(new SimpleStrategy<>()));

    }

    public void disable() {
        enabled = false;
    }
}
