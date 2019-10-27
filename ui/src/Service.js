import { GetFixtureRequest } from '../generated/gateway_pb';

import { TopScoresRequest, PlayerScore, UpdateScoreRequest } from '../generated/leaderboard_pb';

import { GeneratePlayerIdRequest } from '../generated/playerid_pb';

import { FixtureServicePromiseClient } from '../generated/gateway_grpc_web_pb';

import { LeaderboardServicePromiseClient, StreamingLeaderboardServicePromiseClient } from '../generated/leaderboard_grpc_web_pb';

import { PlayerIdServicePromiseClient } from '../generated/playerid_grpc_web_pb';

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

        request.setSize(5);

        return this.leaderboardServicePromiseClient.getTopScores(request, this.getMetadata());
    }

    openTopScoreStream = () => {
        const request = new TopScoresRequest();

        request.setSize(5);

        return this.streamingLeaderboardServicePromiseClient.streamTopScores(request, this.getMetadata());
    }

    updatePlayerScore = ({ playerId, score }) => {
        const playerScore = new PlayerScore();

        playerScore.setPlayerId(playerId);
        playerScore.setScore(score);

        const updateScoreRequest = new UpdateScoreRequest();

        updateScoreRequest.setPlayerScore(playerScore);

        return this.leaderboardServicePromiseClient.updateScore(updateScoreRequest, this.getMetadata());
    }

    constructor(withDeadline) {
        this.withDeadline = withDeadline;

        this.fixtureServicePromiseClient = new FixtureServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
        this.playerIdServicePromiseClient = new PlayerIdServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
        this.leaderboardServicePromiseClient = new LeaderboardServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
        this.streamingLeaderboardServicePromiseClient = new StreamingLeaderboardServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
    }

    getMetadata = () => (this.withDeadline ? { deadline: this.getDeadline() } : {});

    getDeadline = (timeout = CONSTANTS.DEFAULT_TIMEOUT) => (new Date()).getTime() + timeout;
}
