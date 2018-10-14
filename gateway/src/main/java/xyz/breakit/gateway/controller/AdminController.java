package xyz.breakit.gateway.controller;

import com.google.protobuf.util.Durations;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.breakit.admin.AddedLatencySpec;
import xyz.breakit.admin.InjectFailureRequest;
import xyz.breakit.admin.InjectFailureResponse;
import xyz.breakit.gateway.admin.GatewayAdminService;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardAdminClient;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Admin controller for enabling and disabling partial degradation, retries etc.
 */
@RestController
public class AdminController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    private final LeaderboardAdminClient lbAdminClient;
    private final GatewayAdminService gwAdminService;
    private final LeaderboardClient lbClient;

    @Autowired
    public AdminController(LeaderboardAdminClient lbAdminClient, GatewayAdminService gwAdminService, LeaderboardClient lbClient) {
        this.lbAdminClient = lbAdminClient;
        this.gwAdminService = gwAdminService;
        this.lbClient = lbClient;
    }

    @PostMapping("/admin/set_mode/1_pre_demo")
    public void preDemoMode() {

        CompletableFuture<Object> geeseResult = injectFailureInto("geese", 1.0, 100000);
        CompletableFuture<Object> cloudsResult = injectFailureInto("clouds", 1.0, 100000);

        try {
            lbAdminClient.breakService().get(1, TimeUnit.SECONDS);
            lbAdminClient.clear().get(1, TimeUnit.SECONDS);
            lbClient.disableRetries();

            geeseResult.get(1, TimeUnit.SECONDS);
            cloudsResult.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting predemo mode", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/set_mode/2_demo_with_failures")
    public void demoWithFailures() {

        CompletableFuture<Object> geeseResult = injectFailureInto("geese", 0.0, 0);
        CompletableFuture<Object> cloudsResult = injectFailureInto("clouds", 0.7, 700);

        try {
            lbAdminClient.breakService().get(1, TimeUnit.SECONDS);
            lbClient.disableRetries();

            geeseResult.get(1, TimeUnit.SECONDS);
            cloudsResult.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting demo_with_failures mode", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/set_mode/3_demo_with_retries")
    public void demoWithRetries() {

        CompletableFuture<Object> geeseResult = injectFailureInto("geese", 0.0, 0);
        CompletableFuture<Object> cloudsResult = injectFailureInto("clouds", 0.7, 700);

        try {
            lbAdminClient.breakService().get(1, TimeUnit.SECONDS);
            lbClient.enableRetriesWithNoBackoff();

            geeseResult.get(1, TimeUnit.SECONDS);
            cloudsResult.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting demo_with_failures mode", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/set_mode/4_demo_with_backoff")
    public void demoWithBackoff() {

        CompletableFuture<Object> geeseResult = injectFailureInto("geese", 0.0, 0);
        CompletableFuture<Object> cloudsResult = injectFailureInto("clouds", 0.0, 0);

        try {
            lbAdminClient.unbreakService().get(1, TimeUnit.SECONDS);
            lbClient.enableRetriesWithBackoff();

            geeseResult.get(1, TimeUnit.SECONDS);
            cloudsResult.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting demo_with_failures mode", e);
            throw new RuntimeException(e);
        }
    }


    private CompletableFuture<Object> injectFailureInto(String service,
                                                        double failureProbability,
                                                        long failureDurationMs) {
        CompletableFuture<Object> cloudsResult = new CompletableFuture<>();

        gwAdminService.injectFailure(
                InjectFailureRequest.newBuilder()
                        .setServiceName(service)
                        .setAddedLatency(AddedLatencySpec.newBuilder()
                                .setProbability(failureProbability)
                                .setDuration(Durations.fromMillis(failureDurationMs))
                                .build())
                        .build(),
                new StreamObserver<InjectFailureResponse>() {
                    @Override
                    public void onNext(InjectFailureResponse value) {
                        cloudsResult.complete(null);
                    }

                    @Override
                    public void onError(Throwable t) {
                        cloudsResult.completeExceptionally(t);

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        return cloudsResult;
    }
}
