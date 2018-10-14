package xyz.breakit.gateway.controller;

import com.google.protobuf.Duration;
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

    @Autowired
    public AdminController(LeaderboardAdminClient lbAdminClient, GatewayAdminService gwAdminService) {
        this.lbAdminClient = lbAdminClient;
        this.gwAdminService = gwAdminService;
    }

    @PostMapping("/admin/set_mode/pre_demo")
    public void preDemoMode() {

        CompletableFuture<Object> result = new CompletableFuture<>();

        gwAdminService.injectFailure(
                InjectFailureRequest.newBuilder()
                        .setServiceName("geese")
                        .setAddedLatency(AddedLatencySpec.newBuilder()
                                .setProbability(0.0)
                                .setDuration(Duration.newBuilder().setSeconds(0).build())
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
        try {
            lbAdminClient.unbreakService().get(1, TimeUnit.SECONDS);
            lbAdminClient.clear().get(1, TimeUnit.SECONDS);
            result.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("Error while setting predemo mode", e);
            throw new RuntimeException(e);
        }
    }
}
