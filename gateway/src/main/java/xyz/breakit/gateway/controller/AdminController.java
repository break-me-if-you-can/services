package xyz.breakit.gateway.controller;

import com.google.protobuf.util.Durations;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.breakit.admin.AddedLatencySpec;
import xyz.breakit.admin.FixtureFailureSpec;
import xyz.breakit.admin.InjectFailureRequest;
import xyz.breakit.admin.InjectFailureResponse;
import xyz.breakit.gateway.admin.GatewayAdminService;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardAdminClient;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardClient;
import xyz.breakit.gateway.flags.SettableFlags;

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
    private final SettableFlags flags;

    @Autowired
    public AdminController(
            LeaderboardAdminClient lbAdminClient,
            GatewayAdminService gwAdminService,
            LeaderboardClient lbClient,
            SettableFlags flags) {
        this.lbAdminClient = lbAdminClient;
        this.gwAdminService = gwAdminService;
        this.lbClient = lbClient;
        this.flags = flags;
    }

    @PostMapping("/admin/set_mode/1_pre_demo")
    public void preDemoMode() {

        flags.setPartialDegradationEnabled(false);
        CompletableFuture<Object> geeseResult = injectLatencyInto("geese", 0.0, 0, false);
        CompletableFuture<Object> cloudsResult = injectLatencyInto("clouds", 0.0, 0, false);

        try {
            lbAdminClient.unbreakService().get(1, TimeUnit.SECONDS);
            lbAdminClient.clear().get(1, TimeUnit.SECONDS);
            lbClient.disableRetries();

            CompletableFuture.allOf(geeseResult, cloudsResult).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting predemo mode", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/set_mode/2_demo_with_failures")
    public void demoWithFailures() {

        flags.setPartialDegradationEnabled(false);
        CompletableFuture<Object> geeseResult = injectLatencyInto("geese", 0.0, 0, false);
        CompletableFuture<Object> cloudsResult = injectLatencyInto("clouds", 1, 700, false);

        try {
            lbAdminClient.breakService().get(1, TimeUnit.SECONDS);
            lbClient.disableRetries();

            CompletableFuture.allOf(geeseResult, cloudsResult)
                    .get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting demo_with_failures mode", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/set_mode/3_demo_with_partial_degradation")
    public void demoWithPartialDegradation() {

        flags.setPartialDegradationEnabled(true);
        CompletableFuture<Object> geeseResult = injectLatencyInto("geese", 0.0, 0, false);
        CompletableFuture<Object> cloudsResult = injectLatencyInto("clouds", 1, 700, false);

        try {
            lbAdminClient.breakService().get(1, TimeUnit.SECONDS);
            lbClient.disableRetries();

            CompletableFuture.allOf(geeseResult, cloudsResult)
                    .get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting 3_demo_with_partial_degradation mode", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/set_mode/4_demo_with_retries")
    public void demoWithRetries() {

        flags.setPartialDegradationEnabled(true);
        CompletableFuture<Object> geeseResult = injectLatencyInto("geese", 0.0, 0, false);
        CompletableFuture<Object> cloudsResult = injectLatencyInto("clouds", 1, 700, false);

        try {
            lbAdminClient.breakService().get(1, TimeUnit.SECONDS);
            lbClient.enableRetriesWithNoBackoff();

            CompletableFuture.allOf(geeseResult, cloudsResult).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting 4_demo_with_retries mode", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/set_mode/5_general_demo")
    public void generalDemo() {
        flags.setPartialDegradationEnabled(true);
        CompletableFuture<Object> geeseResult = injectLatencyInto("geese", 0.0, 0, false);
        CompletableFuture<Object> cloudsResult = injectLatencyInto("clouds", 0.0, 0, false);

        try {
            lbAdminClient.unbreakService().get(1, TimeUnit.SECONDS);
            lbClient.disableRetries();


            CompletableFuture.allOf(geeseResult, cloudsResult).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting 5_general_demo mode", e);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/admin/set_mode/6_total_geese_demo")
    public void totalGeeseDemo() {
        flags.setPartialDegradationEnabled(true);
        CompletableFuture<Object> geeseResult = injectLatencyInto("geese", 0.0, 0, true);
        CompletableFuture<Object> cloudsResult = injectLatencyInto("clouds", 0.0, 0, false);

        try {
            lbAdminClient.unbreakService().get(1, TimeUnit.SECONDS);
            lbClient.disableRetries();


            CompletableFuture.allOf(geeseResult, cloudsResult).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting 5_general_demo mode", e);
            throw new RuntimeException(e);
        }
    }


    private CompletableFuture<Object> injectLatencyInto(String service,
                                                        double failureProbability,
                                                        long failureDurationMs,
                                                        boolean fixtureFailureEnabled) {
        CompletableFuture<Object> cloudsResult = new CompletableFuture<>();

        gwAdminService.injectFailure(
                InjectFailureRequest.newBuilder()
                        .setServiceName(service)
                        .setFixtureFailure(FixtureFailureSpec.newBuilder().setFullFixtureEnabled(fixtureFailureEnabled).build())
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
