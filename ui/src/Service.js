const grpc = {};
grpc.web = require('grpc-web');

import {GetFixtureRequest, GeneratePlayerIdRequest,
            TopScoresRequest, PlayerScore, UpdateScoreRequest } from '../generated/gateway_pb';
import {FixtureServicePromiseClient, PlayerIdServicePromiseClient,
            LeaderboardServicePromiseClient} from '../generated/gateway_grpc_web_pb';
import {CONSTANTS } from './Constants';

export class Service {
    constructor() {
        this.fixtureServicePromiseClient = new FixtureServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
        this.playerIdServicePromiseClient = new PlayerIdServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
        this.leaderboardServicePromiseClient = new LeaderboardServicePromiseClient(CONSTANTS.GATEWAY_SERVICE_HOST);
    }

    getDeadline = (timeout) => (new Date()).getTime() + timeout;

    getFixture = () => {
        let request = new GetFixtureRequest();

        request.setLineWidth(CONSTANTS.FIELD_WIDTH);
        request.setLinesCount(CONSTANTS.LINES_COUNT);
        request.setGooseWidth(CONSTANTS.GOOSE_WIDTH);
        request.setCloudWidth(CONSTANTS.CLOUD_WIDTH);

        const deadline = this.getDeadline(CONSTANTS.DEFAULT_TIMEOUT);

        return this.fixtureServicePromiseClient.getFixture(request, { deadline });
    }

    getPlayerId = () => {
        let request = new GeneratePlayerIdRequest();

        const deadline = this.getDeadline(CONSTANTS.DEFAULT_TIMEOUT);

        return this.playerIdServicePromiseClient.generatePlayerId(request, { deadline });
    }

    getTopPlayerScore = () => {
        let request = new TopScoresRequest();
        request.setSize();

        const deadline = this.getDeadline(CONSTANTS.DEFAULT_TIMEOUT);

        return this.leaderboardServicePromiseClient.getTopScores(request, { deadline });
    }

    updatePlayerScore = ({playerId, score}) => {
        let playerScore = new PlayerScore();
        playerScore.setPlayerId(playerId);
        playerScore.setScore(score);

        let updateScoreRequest = new UpdateScoreRequest();
        updateScoreRequest.setPlayerScore(playerScore);

        const deadline = this.getDeadline(CONSTANTS.DEFAULT_TIMEOUT);

        return this.leaderboardServicePromiseClient.updateScore(updateScoreRequest, { deadline });
    }
}
