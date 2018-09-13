package xyz.breakit.leaderboard.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

public class FailureInjectionFilter implements Filter {

    private final double failureProbability;
    private final int leaderboardDelay;
    private final Random rnd = new Random();

    public FailureInjectionFilter(
            double failureProbability,
            int leaderboardDelay) {
        this.failureProbability = failureProbability;
        this.leaderboardDelay = leaderboardDelay;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpRespone = (HttpServletResponse) response;

        if (httpRequest.getHeader("enable_flaky_failures") != null && randomFailureIsTriggered()) {
            httpRespone.sendError(500);
            return;
        }

        if (httpRequest.getHeader("enable_failures") != null) {
            httpRespone.sendError(500);
            return;
        }

        if (httpRequest.getHeader("enable_flaky_delays") != null && randomFailureIsTriggered()) {
            introduceDelay();
        }

        if (httpRequest.getHeader("enable_delays") != null) {
            introduceDelay();
        }

        chain.doFilter(request, response);
    }

    private void introduceDelay() {
        try {
            Thread.sleep(leaderboardDelay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean randomFailureIsTriggered() {
        return (double) rnd.nextInt(101) / 100.0 <= failureProbability;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
