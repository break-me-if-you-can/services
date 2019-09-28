import { GetFixtureRequest, GeneratePlayerIdRequest, TopScoresRequest, PlayerScore, UpdateScoreRequest } from '../generated/gateway_pb';

import { FixtureServicePromiseClient, PlayerIdServicePromiseClient, LeaderboardServicePromiseClient } from '../generated/gateway_grpc_web_pb';

// import { FixtureServiceClient, PlayerIdServiceClient, LeaderboardServiceClient } from '../generated/gateway_grpc_web_pb';

// showcase Clients.
// import * as Clients from '../generated/gateway_grpc_web_pb';

import { CONSTANTS } from './Constants';

export class Service {
    getFixture = () => {
        const request = new GetFixtureRequest();

        request.setLineWidth(CONSTANTS.FIELD_WIDTH);
        request.setLinesCount(CONSTANTS.LINES_COUNT);
        request.setGooseWidth(CONSTANTS.GOOSE_WIDTH);
        request.setCloudWidth(CONSTANTS.CLOUD_WIDTH);

        const metadata = this.getMetadata();

        return this.fixtureServicePromiseClient.getFixture(request, metadata);
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
        console.log('Deadline:', withDeadline);

        this.withDeadline = withDeadline;

        this.fixtureServicePromiseClient = new FixtureServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
        this.playerIdServicePromiseClient = new PlayerIdServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
        this.leaderboardServicePromiseClient = new LeaderboardServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
    }

    getMetadata = () => (this.withDeadline ? { deadline: this.getDeadline() } : {});

    getDeadline = (timeout = CONSTANTS.DEFAULT_TIMEOUT) => (new Date()).getTime() + timeout;
}
