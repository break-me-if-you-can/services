package xyz.breakit.game.gateway.controller;

import com.google.protobuf.util.Durations;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.breakit.admin.AddedLatencySpec;
import xyz.breakit.admin.FixtureFailureSpec;
import xyz.breakit.admin.InjectFailureRequest;
import xyz.breakit.admin.InjectFailureResponse;
import xyz.breakit.game.gateway.clients.leaderboard.LeaderboardRestClient;
import xyz.breakit.game.gateway.admin.GatewayAdminService;
import xyz.breakit.game.gateway.clients.leaderboard.LeaderboardAdminClient;
import xyz.breakit.game.gateway.flags.SettableFlags;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static xyz.breakit.game.gateway.admin.ServiceNames.*;

/**
 * Admin controller for enabling and disabling partial degradation, retries etc.
 */
@RestController
public class AdminController {

    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    private final LeaderboardAdminClient lbAdminClient;
    private final GatewayAdminService gwAdminService;
    private final LeaderboardRestClient lbClient;
    private final SettableFlags flags;

    @Autowired
    public AdminController(
            LeaderboardAdminClient lbAdminClient,
            GatewayAdminService gwAdminService,
            LeaderboardRestClient lbClient,
            SettableFlags flags) {
        this.lbAdminClient = lbAdminClient;
        this.gwAdminService = gwAdminService;
        this.lbClient = lbClient;
        this.flags = flags;
    }

    @GetMapping("/admin/set_mode/1_pre_demo")
    public void preDemoMode() {
        flags.setDiverseGeeseEnabled(false);
        flags.setPartialDegradationEnabled(false);
        flags.setRetryEnabled(true);
        CompletableFuture<Object> geeseResult = injectLatencyInto(GEESE_SERVICE, 0.0, 0, false);
        CompletableFuture<Object> cloudsResult = injectLatencyInto(CLOUDS_SERVICE, 0.0, 0, false);
        CompletableFuture<Object> gwResult = injectLatencyInto(GATEWAY_SERVICE, 0.0, 0, false);
        CompletableFuture<Object> playerIdResult = injectLatencyInto(PLAYER_ID_SERVICE, 0.0, 0, false);
        CompletableFuture<Object> lbResult = injectLatencyInto(LEADERBOARD_SERVICE, 0, 0, false);

        try {
            lbAdminClient.unbreakService().get(1, TimeUnit.SECONDS);
            lbAdminClient.initStrangeLeaderboard().get(1, TimeUnit.SECONDS);
            lbClient.disableRetries();

            CompletableFuture.allOf(geeseResult, cloudsResult, gwResult, playerIdResult, lbResult).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting predemo mode", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/admin/set_mode/2_demo_with_slow_generate_player_id")
    public void demoWithSlowGeneratePlayerId() {

        flags.setDiverseGeeseEnabled(false);
        flags.setPartialDegradationEnabled(false);
        flags.setRetryEnabled(false);

        CompletableFuture<Object> geeseResult = injectLatencyInto(GEESE_SERVICE, 0.0, 0, false);
        CompletableFuture<Object> cloudsResult = injectLatencyInto(CLOUDS_SERVICE, 0.0, 0, false);
        CompletableFuture<Object> gwResult = injectLatencyInto(GATEWAY_SERVICE, 1, 10000000, false);
        CompletableFuture<Object> lbResult = injectLatencyInto(LEADERBOARD_SERVICE, 0, 0, false);

        try {
            CompletableFuture.allOf(geeseResult, cloudsResult, gwResult, lbResult).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting 2_demo_with_slow_generate_player_id mode", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/admin/set_mode/2.1_new_demo_with_slow_generate_player_id")
    public void newDemoWithSlowGeneratePlayerId() {

        flags.setDiverseGeeseEnabled(false);
        flags.setPartialDegradationEnabled(false);
        flags.setRetryEnabled(false);

        CompletableFuture<Object> geeseResult = injectLatencyInto(GEESE_SERVICE, 0.0, 0, false);
        CompletableFuture<Object> cloudsResult = injectLatencyInto(CLOUDS_SERVICE, 0.0, 0, false);
        CompletableFuture<Object> gwResult = injectLatencyInto(GATEWAY_SERVICE, 0.0, 0, false);
        CompletableFuture<Object> playerIdResult = injectLatencyInto(PLAYER_ID_SERVICE, 1, 10000000, false);
        CompletableFuture<Object> lbResult = injectLatencyInto(LEADERBOARD_SERVICE, 0, 0, false);

        try {
            CompletableFuture.allOf(geeseResult, cloudsResult, playerIdResult, gwResult, lbResult).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting 2.1_new_demo_with_slow_generate_player_id mode", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/admin/set_mode/3_enable_diverse_geese")
    public void demoWithDiverseGeese() {
        flags.setDiverseGeeseEnabled(true);
        flags.setPartialDegradationEnabled(true);
        flags.setRetryEnabled(true);

        CompletableFuture<Object> geeseResult = injectLatencyInto(GEESE_SERVICE, 0.0, 0, false);
        CompletableFuture<Object> cloudsResult = injectLatencyInto(CLOUDS_SERVICE, 0.0, 0, false);
        CompletableFuture<Object> gwResult = injectLatencyInto(GATEWAY_SERVICE, 0, 0, false);
        CompletableFuture<Object> lbResult = injectLatencyInto(LEADERBOARD_SERVICE, 0, 0, false);
        CompletableFuture<Object> playerIdResult = injectLatencyInto(PLAYER_ID_SERVICE, 0.0, 0, false);

        try {
            CompletableFuture.allOf(geeseResult, cloudsResult, gwResult, playerIdResult, lbResult).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting 3_enable_diverse_geese mode", e);
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<Object> injectLatencyInto(String service,
                                                        double failureProbability,
                                                        long failureDurationMs,
                                                        boolean fixtureFailureEnabled) {
        CompletableFuture<Object> result = new CompletableFuture<>();

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
                        result.complete(null);
                    }

                    @Override
                    public void onError(Throwable t) {
                        result.completeExceptionally(t);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        return result;
    }
}
