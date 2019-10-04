import { GetFixtureRequest, GeneratePlayerIdRequest } from '../generated/gateway_pb';

import { TopScoresRequest, PlayerScore, UpdateScoreRequest } from '../generated/leaderboard_shared_pb';

import { FixtureServicePromiseClient, PlayerIdServicePromiseClient,
    LeaderboardServicePromiseClient, StreamingLeaderboardServiceClient } from '../generated/gateway_grpc_web_pb';

import { CONSTANTS } from './Constants';

export class Service {
    getFixture = () => {
        const request = new GetFixtureRequest();

        request.setLineWidth(CONSTANTS.FIELD_WIDTH);
        request.setLinesCount(CONSTANTS.LINES_COUNT);
        request.setGooseWidth(CONSTANTS.GOOSE_WIDTH);
        request.setCloudWidth(CONSTANTS.CLOUD_WIDTH);

        return this.fixtureServicePromiseClient.getFixture(request, this.getMetadata());
    }

    getPlayerId = () => {
        const request = new GeneratePlayerIdRequest();

        const metadata = this.getMetadata();

        return this.playerIdServicePromiseClient.generatePlayerId(request, metadata);
    }

    getTopPlayerScore = () => {
        const request = new TopScoresRequest();

        request.setSize();

        const metadata = this.getMetadata();

        return this.leaderboardServicePromiseClient.getTopScores(request, metadata);
    }

    subscribeOnTopScoreStream = (handler) => {
        const request = new TopScoresRequest();

        request.setSize();

        var stream = this.streamingLeaderboardServiceClient.getTopScores(request, this.getMetadata());

        stream.on('data', (data) => {
            console.log('On data: ', data);
            // handler(data);
        });

        stream.on('status', (status) => console.log('On Status: ', status));

        stream.on('end', (end) => console.log('Signal end: ', end));
    }

    updatePlayerScore = ({ playerId, score }) => {
        const playerScore = new PlayerScore();

        playerScore.setPlayerId(playerId);
        playerScore.setScore(score);

        const updateScoreRequest = new UpdateScoreRequest();

        updateScoreRequest.setPlayerScore(playerScore);

        const metadata = this.getMetadata();

        return this.leaderboardServicePromiseClient.updateScore(updateScoreRequest, metadata);
    }

    constructor(withDeadline) {
        this.withDeadline = withDeadline;

        this.fixtureServicePromiseClient = new FixtureServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
        this.playerIdServicePromiseClient = new PlayerIdServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
        this.leaderboardServicePromiseClient = new LeaderboardServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);

        this.streamingLeaderboardServiceClient = new StreamingLeaderboardServiceClient(CONSTANTS.GATEWAY_SERVICE_HOST);
    }

    getMetadata = () => (this.withDeadline ? { deadline: this.getDeadline() } : {});

    getDeadline = (timeout = CONSTANTS.DEFAULT_TIMEOUT) => (new Date()).getTime() + timeout;
}
