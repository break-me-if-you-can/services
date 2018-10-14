package xyz.breakit.gateway.controller;

import com.google.protobuf.util.Durations;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.breakit.admin.*;
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

        CompletableFuture<Object> degradationResult = partialDegradation(false);
        CompletableFuture<Object> geeseResult = injectLatencyInto("geese", 0.0, 700);
        CompletableFuture<Object> cloudsResult = injectLatencyInto("clouds", 0.0, 700);

        try {
            lbAdminClient.breakService().get(1, TimeUnit.SECONDS);
            lbAdminClient.clear().get(1, TimeUnit.SECONDS);
            lbClient.disableRetries();

            CompletableFuture.allOf(degradationResult,geeseResult, cloudsResult)
                    .get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting predemo mode", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/set_mode/2_demo_with_failures")
    public void demoWithFailures() {

        CompletableFuture<Object> degradationResult = partialDegradation(false);
        CompletableFuture<Object> geeseResult = injectLatencyInto("geese", 0.0, 0);
        CompletableFuture<Object> cloudsResult = injectLatencyInto("clouds", 0.7, 700);

        try {
            lbAdminClient.breakService().get(1, TimeUnit.SECONDS);
            lbClient.disableRetries();

            CompletableFuture.allOf(degradationResult,geeseResult, cloudsResult)
                    .get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting demo_with_failures mode", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/set_mode/3_demo_with_retries")
    public void demoWithRetries() {

        CompletableFuture<Object> degradationResult = partialDegradation(true);
        CompletableFuture<Object> geeseResult = injectLatencyInto("geese", 0.0, 0);
        CompletableFuture<Object> cloudsResult = injectLatencyInto("clouds", 0.7, 700);

        try {
            lbAdminClient.breakService().get(1, TimeUnit.SECONDS);
            lbClient.enableRetriesWithNoBackoff();

            CompletableFuture.allOf(degradationResult,geeseResult, cloudsResult)
                    .get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting demo_with_retries mode", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/set_mode/4_demo_with_backoff")
    public void demoWithBackoff() {
        CompletableFuture<Object> degradationResult = partialDegradation(true);
        CompletableFuture<Object> geeseResult = injectLatencyInto("geese", 0.0, 0);
        CompletableFuture<Object> cloudsResult = injectLatencyInto("clouds", 0.0, 0);

        try {
            lbAdminClient.unbreakService().get(1, TimeUnit.SECONDS);
            lbClient.enableRetriesWithBackoff();


            CompletableFuture.allOf(degradationResult,geeseResult, cloudsResult)
                    .get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting demo_with_backoff mode", e);
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Object> partialDegradation(boolean enable) {
        CompletableFuture<Object> degradationResult = new CompletableFuture<>();
        gwAdminService.managePartialDegradation(PartialDegradationRequest.newBuilder().setEnable(enable).build(),
                new StreamObserver<PartialDegradationResponse>() {
                    @Override
                    public void onNext(PartialDegradationResponse value) {
                        degradationResult.complete(null);
                    }

                    @Override
                    public void onError(Throwable t) {
                        degradationResult.completeExceptionally(t);

                    }

                    @Override
                    public void onCompleted() {

                    }
                }
        );
        return degradationResult;
    }


    private CompletableFuture<Object> injectLatencyInto(String service,
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
