package xyz.breakit.gateway.clients.leaderboard;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@TestPropertySource("classpath:test.properties")
@RunWith(SpringRunner.class)
public class LeaderboardClientTest {

    @Autowired
    private LeaderboardClient client;

    @Test
    public void testTop5() throws Exception {
        client.top5().get(1000, TimeUnit.MILLISECONDS);
    }
}