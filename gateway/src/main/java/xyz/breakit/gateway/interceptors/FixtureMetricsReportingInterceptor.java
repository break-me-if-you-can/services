package xyz.breakit.gateway.interceptors;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.opencensus.common.Scope;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import xyz.breakit.gateway.FixtureLine;
import xyz.breakit.gateway.FixtureResponse;

/**
 * Server interceptor that reports fixture metrics: returned number of lines,
 * geese and clouds.
 */
public class FixtureMetricsReportingInterceptor implements ServerInterceptor {

    private static final Tagger tagger = Tags.getTagger();
    private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();

    private static final MeasureLong LINE_COUNT =
            MeasureLong.create("breakit.xyz/gateway/fixture_response/line_count",
                    "", "");
    private static final MeasureLong GEESE_COUNT =
            MeasureLong.create("breakit.xyz/gateway/fixture_response/geese_count",
                    "", "");
    private static final MeasureLong CLOUDS_COUNT =
            MeasureLong.create("breakit.xyz/gateway/fixture_response/clouds_count",
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

                    try (Scope scope = tagger.withTagContext(tagger.emptyBuilder().build())) {
                        statsRecorder.newMeasureMap().put(LINE_COUNT, response.getLinesCount()).record();
                        response.getLinesList().stream()
                                .forEach(FixtureMetricsReportingInterceptor::reportLineCounters);
                    }
                }

                super.sendMessage(message);
            }
        }, headers);
    }

    private static void reportLineCounters(FixtureLine line) {
        MeasureMap measureMap = statsRecorder.newMeasureMap();
        measureMap.put(GEESE_COUNT, line.getGoosePositionsCount());
        measureMap.put(CLOUDS_COUNT, line.getCloudPositionsCount());
        measureMap.record();
    }

}
