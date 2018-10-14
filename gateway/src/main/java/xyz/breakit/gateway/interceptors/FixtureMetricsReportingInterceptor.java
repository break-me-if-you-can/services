package xyz.breakit.gateway.interceptors;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import xyz.breakit.gateway.FixtureLine;
import xyz.breakit.gateway.FixtureResponse;

/**
 *
 */
public class FixtureMetricsReportingInterceptor implements ServerInterceptor {

    private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();
    private static final MeasureLong LINE_COUNT =
            MeasureLong.create("breakit/gateway/fixture_response/line_count",
                    "", "");
    private static final MeasureLong GEESE_COUNT =
            MeasureLong.create("breakit/gateway/fixture_response/geese_count",
                    "", "");
    private static final MeasureLong CLOUDS_COUNT =
            MeasureLong.create("breakit/gateway/fixture_response/clouds_count",
                    "", "");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendMessage(RespT message) {
                if (message instanceof FixtureResponse) {
                    FixtureResponse response = (FixtureResponse) message;

                    statsRecorder.newMeasureMap().put(LINE_COUNT, response.getLinesCount());

                    response.getLinesList().stream()
                            .forEach(FixtureMetricsReportingInterceptor::reportLineCounters);
                }

                super.sendMessage(message);
            }
        }, headers);
    }

    private static void reportLineCounters(FixtureLine line) {
        statsRecorder.newMeasureMap().put(GEESE_COUNT, line.getGoosePositionsCount());
        statsRecorder.newMeasureMap().put(CLOUDS_COUNT, line.getCloudPositionsCount());
    }

}
