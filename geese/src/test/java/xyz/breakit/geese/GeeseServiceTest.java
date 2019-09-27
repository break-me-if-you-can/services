package xyz.breakit.geese;

import io.grpc.testing.GrpcServerRule;
import org.junit.Rule;
import org.junit.Test;
import xyz.breakit.common.instrumentation.failure.InjectedFailureProvider;
import xyz.breakit.geese.GeeseServiceGrpc.GeeseServiceBlockingStub;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
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
        Supplier<GooseType> staticGeeseTypeGenerator = () -> GooseType.GOOSE_TYPE_GREY_GOOSE;

        int numberOfGeese = 5;
        grpcServerRule.getServiceRegistry()
                .addService(new GeeseService((min, max) -> numberOfGeese,
                        sequentialGenerator, staticGeeseTypeGenerator, new InjectedFailureProvider()));

        GeeseServiceBlockingStub geeseClient = GeeseServiceGrpc.newBlockingStub(grpcServerRule.getChannel());

        GeeseResponse response = geeseClient.getGeese(GetGeeseRequest.newBuilder()
                .setLinesCount(1).setGooseWidth(10).setLineWidth(50).build());

        GeeseResponse geeseResponse = GeeseResponse.newBuilder()
                .addLines(GeeseLine.newBuilder()
                        .addAllGeeseLocators(asList(
                                GooseLocator.newBuilder().setGooseType(GooseType.GOOSE_TYPE_GREY_GOOSE).setGoosePosition(0).build(),
                                GooseLocator.newBuilder().setGooseType(GooseType.GOOSE_TYPE_GREY_GOOSE).setGoosePosition(10).build(),
                                GooseLocator.newBuilder().setGooseType(GooseType.GOOSE_TYPE_GREY_GOOSE).setGoosePosition(20).build(),
                                GooseLocator.newBuilder().setGooseType(GooseType.GOOSE_TYPE_GREY_GOOSE).setGoosePosition(30).build(),
                                GooseLocator.newBuilder().setGooseType(GooseType.GOOSE_TYPE_GREY_GOOSE).setGoosePosition(40).build()
                        ))
                        .build())
                .build();
        assertThat(response).isEqualTo(geeseResponse);
    }

    @Test
    public void shouldReturnLineFullOfGeeseIfFullFixtureFailureIsEnabled() {
        InjectedFailureProvider fixtureFailureProvider = new InjectedFailureProvider();
        fixtureFailureProvider.setFullFixtureEnabled(true);
        Supplier<GooseType> staticGeeseTypeGenerator = () -> GooseType.GOOSE_TYPE_GREY_GOOSE;

        grpcServerRule.getServiceRegistry()
                .addService(new GeeseService((min, max) -> 0,
                        index -> 0, staticGeeseTypeGenerator, fixtureFailureProvider));

        GeeseServiceBlockingStub geeseClient = GeeseServiceGrpc.newBlockingStub(grpcServerRule.getChannel());

        GeeseResponse response = geeseClient.getGeese(GetGeeseRequest.newBuilder()
                .setLinesCount(1).setGooseWidth(4).setLineWidth(21).build());

        GeeseResponse geeseResponse = GeeseResponse.newBuilder()
                .addLines(GeeseLine.newBuilder().addAllGeeseLocators(
                        asList(
                                GooseLocator.newBuilder().setGooseType(GooseType.GOOSE_TYPE_GREY_GOOSE).setGoosePosition(0).build(),
                                GooseLocator.newBuilder().setGooseType(GooseType.GOOSE_TYPE_GREY_GOOSE).setGoosePosition(4).build(),
                                GooseLocator.newBuilder().setGooseType(GooseType.GOOSE_TYPE_GREY_GOOSE).setGoosePosition(8).build(),
                                GooseLocator.newBuilder().setGooseType(GooseType.GOOSE_TYPE_GREY_GOOSE).setGoosePosition(12).build(),
                                GooseLocator.newBuilder().setGooseType(GooseType.GOOSE_TYPE_GREY_GOOSE).setGoosePosition(16).build(),
                                GooseLocator.newBuilder().setGooseType(GooseType.GOOSE_TYPE_GREY_GOOSE).setGoosePosition(20).build())
                ))
                .build();

        assertThat(response).isEqualTo(geeseResponse);
    }

}