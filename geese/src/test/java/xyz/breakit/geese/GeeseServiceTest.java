package xyz.breakit.geese;

import io.grpc.testing.GrpcServerRule;
import org.junit.Rule;
import org.junit.Test;
import xyz.breakit.common.instrumentation.failure.InjectedFailureProvider;
import xyz.breakit.geese.GeeseServiceGrpc.GeeseServiceBlockingStub;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static java.util.Arrays.asList;

/**
 * Test for {@link GeeseService}.
 */
public class GeeseServiceTest {

    @Rule
    public GrpcServerRule grpcServerRule = new GrpcServerRule().directExecutor();

    @Test
    public void shouldReturnNonOverlappingGeese() {

        AtomicInteger counter = new AtomicInteger();
        UnaryOperator<Integer> sequentialGenerator = i -> counter.getAndIncrement();

        int numberOfGeese = 5;
        grpcServerRule.getServiceRegistry()
                .addService(new GeeseService((min, max) -> numberOfGeese,
                        sequentialGenerator, new InjectedFailureProvider()));

        GeeseServiceBlockingStub geeseClient = GeeseServiceGrpc.newBlockingStub(grpcServerRule.getChannel());

        GeeseResponse response = geeseClient.getGeese(GetGeeseRequest.newBuilder()
                .setLinesCount(1).setGooseWidth(10).setLineWidth(50).build());

        GeeseResponse geeseResponse = GeeseResponse.newBuilder()
                .addLines(GeeseLine.newBuilder().addAllGeesePositions(asList(0, 10, 20, 30, 40)).build())
                .build();
        assertThat(response).isEqualTo(geeseResponse);
    }

    @Test
    public void shouldReturnLineFullOfGeeseIfFullFixtureFailureIsEnabled() {
        InjectedFailureProvider fixtureFailureProvider = new InjectedFailureProvider();
        fixtureFailureProvider.setFullFixtureEnabled(true);
        grpcServerRule.getServiceRegistry()
                .addService(new GeeseService((min, max) -> 0,
                        index -> 0, fixtureFailureProvider));

        GeeseServiceBlockingStub geeseClient = GeeseServiceGrpc.newBlockingStub(grpcServerRule.getChannel());

        GeeseResponse response = geeseClient.getGeese(GetGeeseRequest.newBuilder()
                .setLinesCount(1).setGooseWidth(4).setLineWidth(21).build());

        GeeseResponse geeseResponse = GeeseResponse.newBuilder()
                .addLines(GeeseLine.newBuilder().addAllGeesePositions(asList(0, 4, 8, 12, 16, 20)).build())
                .build();
        assertThat(response).isEqualTo(geeseResponse);
    }

}