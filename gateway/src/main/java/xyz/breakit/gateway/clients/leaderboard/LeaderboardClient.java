package xyz.breakit.gateway.clients.leaderboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LeaderboardClient {

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

    public List<LeaderboardEntry> top5() throws IOException {
        Request request = new Request.Builder()
                .url(leaderboardUrl+"/top/5")
                .get()
                .build();

        return objectMapper.readValue(
                httpClient.newCall(request).execute().body().string(),
                new TypeReference<List<LeaderboardEntry>> () {}
                );
    }

    public void updateScore(LeaderboardEntry newScore) throws IOException {
        Request request = new Request.Builder()
                .url(leaderboardUrl+"/scores")
                .post(RequestBody.create(MediaType.get("application/json"), objectMapper.writeValueAsBytes(newScore)))
                .build();

        Response response = httpClient.newCall(request).execute();
        if (response.code() == 200) {
            throw new RuntimeException("Got error code while updating a score: "+ response.toString());
        }
    }


}
