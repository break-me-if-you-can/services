package xyz.breakit.common.instrumentation.census;

import io.opencensus.common.Duration;
import io.opencensus.contrib.zpages.ZPageHandlers;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsConfiguration;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import io.opencensus.stats.Stats;

import java.io.IOException;

import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_FINISHED_COUNT_CUMULATIVE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_SERVER_LATENCY_VIEW;

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

        Stats.getViewManager().registerView(RPC_SERVER_SERVER_LATENCY_VIEW);
        Stats.getViewManager().registerView(RPC_SERVER_FINISHED_COUNT_CUMULATIVE_VIEW);

        String gcpProjectId = System.getenv().get("GCP_PROJECTID");
        StackdriverStatsExporter.createAndRegister(
                StackdriverStatsConfiguration.builder()
                        .setProjectId(gcpProjectId)
                        .setExportInterval(Duration.fromMillis(STACK_DRIVER_EXPORT_INTERVAL_MILLIS))
                        .build());

        ZPageHandlers.startHttpServerAndRegisterAll(zpagesPort);
    }

}
