package xyz.breakit.game.leaderboard.rest;


import com.netflix.concurrency.limits.limit.FixedLimit;
import com.netflix.concurrency.limits.limiter.SimpleLimiter;
import com.netflix.concurrency.limits.servlet.ConcurrencyLimitServletFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LimiterFilter implements Filter {

    private boolean enabled = false;
    private Filter limitingFilter = new ConcurrencyLimitServletFilter(
            new SimpleLimiter.Builder()
                    .limit(FixedLimit.of(10))
                    .build());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (enabled && !((HttpServletRequest) request).getRequestURI().startsWith("/magic")) {
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
        limitingFilter =  new ConcurrencyLimitServletFilter(
                new SimpleLimiter.Builder()
                        .limit(FixedLimit.of(limit))
                        .build());
    }

    public void disable() {
        enabled = false;
    }
}
