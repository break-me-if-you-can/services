package xyz.breakit.common.instrumentation.census;

import io.opencensus.common.Duration;
import io.opencensus.contrib.zpages.ZPageHandlers;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsConfiguration;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import io.opencensus.stats.Stats;

import java.io.IOException;

import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.*;

/**
 * Utility to integrate gRPC stats with census and report stats to Stackdriver.
 */
public final class GrpcCensusReporter {

    private static final int STACK_DRIVER_EXPORT_INTERVAL_MILLIS = 10000;

    /**
     * Registers gRPC stats and starts ZPages and enables reporting to Stackdriver.
     * Requires {@code GCP_PROJECTID} environment variable.
     */
    public static void registerAndExportViews(int zpagesPort) throws IOException {

        Stats.getViewManager().registerView(GRPC_SERVER_SERVER_LATENCY_VIEW);
        Stats.getViewManager().registerView(GRPC_SERVER_COMPLETED_RPC_VIEW);
        Stats.getViewManager().registerView(RPC_CLIENT_ERROR_COUNT_MINUTE_VIEW);

        String gcpProjectId = System.getenv().get("GCP_PROJECTID");
        StackdriverStatsExporter.createAndRegister(
                StackdriverStatsConfiguration.builder()
                        .setProjectId(gcpProjectId)
                        .setExportInterval(Duration.fromMillis(STACK_DRIVER_EXPORT_INTERVAL_MILLIS))
                        .build());

        ZPageHandlers.startHttpServerAndRegisterAll(zpagesPort);
    }

}
