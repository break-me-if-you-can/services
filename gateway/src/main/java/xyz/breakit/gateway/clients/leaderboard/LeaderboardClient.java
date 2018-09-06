package xyz.breakit.gateway.clients.leaderboard;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LeaderboardClient {

    // one per core
    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(4);

    private static final CircuitBreaker CIRCUIT_BREAKER = new CircuitBreaker()
            .withFailureThreshold(3, 5);


    private final static RetryPolicy RETRY_POLICY = new RetryPolicy()
            .withBackoff(1, 30, TimeUnit.SECONDS, 2.0)
            .withJitter(300, TimeUnit.MILLISECONDS)
            .retryOn(Throwable.class)
            .withMaxRetries(5);

    private final String leaderboardUrl;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;


    public LeaderboardClient(String leaderboardUrl) {
        this.leaderboardUrl = leaderboardUrl;
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(500, TimeUnit.MILLISECONDS)
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .build();

        objectMapper = new ObjectMapper();
    }

    public CompletableFuture<List<LeaderboardEntry>> top5() throws IOException {
        Request request = new Request.Builder()
                .url(leaderboardUrl + "/top/5")
                .get()
                .build();


        return Failsafe
                .with(RETRY_POLICY)
                //.with(CIRCUIT_BREAKER)
                .with(EXECUTOR)
                .future(() -> enqueueHttpRequest(request));
    }

    private CompletableFuture<List<LeaderboardEntry>> enqueueHttpRequest(Request request) throws IOException {
        CompletableFuture<List<LeaderboardEntry>> result = new CompletableFuture<>();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                result.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                result.complete(
                        objectMapper.readValue(
                                response.body().bytes(),
                                new TypeReference<List<LeaderboardEntry>>() {
                                }
                        ));
            }
        });
        return result;
    }

    public void updateScore(LeaderboardEntry newScore) throws IOException {
        Request request = new Request.Builder()
                .url(leaderboardUrl + "/scores")
                .post(RequestBody.create(MediaType.get("application/json"), objectMapper.writeValueAsBytes(newScore)))
                .build();

        httpClient
                .newCall(request)
                .enqueue(new Callback() {
                             @Override
                             public void onFailure(Call call, IOException e) {
                                 throw new RuntimeException(e);
                             }

                             @Override
                             public void onResponse(Call call, Response response) {
                                 if (response.code() == 200) {
                                     throw new RuntimeException("Got error code while updating a score: " + response.toString());
                                 }
                             }
                         }

                );

    }


}
