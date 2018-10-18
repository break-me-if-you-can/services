const grpc = {};
grpc.web = require('grpc-web');

import * as Requests from '../generated/gateway_pb';
import * as Clients from '../generated/gateway_grpc_web_pb';
import { CONSTANTS } from './Constants';

export class Service {
  
    constructor(props) {
      this.fixtureServiceClient = new Clients.FixtureServiceClient(CONSTANTS.GATEWAY_SERVICE_HOST);
      this.playerIdServiceClient = new Clients.PlayerIdServiceClient(CONSTANTS.GATEWAY_SERVICE_HOST);
      this.leaderboardServiceClient = new Clients.LeaderboardServiceClient(CONSTANTS.GATEWAY_SERVICE_HOST);
    }
  
    getFixture = (callback) => {
        let getFixtureRequest = new Requests.GetFixtureRequest();
            getFixtureRequest.setLineWidth(CONSTANTS.FIELD_WIDTH);
            getFixtureRequest.setLinesCount(CONSTANTS.LINES_COUNT);
            getFixtureRequest.setGooseWidth(CONSTANTS.GOOSE_WIDTH);
            getFixtureRequest.setCloudWidth(CONSTANTS.CLOUD_WIDTH);

        let call = null;
        let requestCancelTimeout = setTimeout(function() { if (call) { call.cancel() } }, CONSTANTS.CANCEL_TIMEOUT);

        call = this.fixtureServiceClient
            .getFixture(getFixtureRequest, { },
                function(err, response) {
                    clearTimeout(requestCancelTimeout);

                    if (err) {
                        console.log(err);
                    } else {
                        callback(response);
                    }
                }
            );
    }

    getPlayerId = (callback) => {
        let generatePlayerIdRequest = new Requests.GeneratePlayerIdRequest();

        let call = null;
        let requestCancelTimeout = setTimeout(function() { if (call) { call.cancel() } }, CONSTANTS.CANCEL_TIMEOUT);

        call = this.playerIdServiceClient
            .generatePlayerId(generatePlayerIdRequest, { },
                function(err, response) {
                    clearTimeout(requestCancelTimeout);

                    if (err) {
                        console.log(err);
                    } else {
                        callback(response);
                    }
                }
        );
    }

    getTopPlayerScore = (callback) => {
        let topScoresRequest = new Requests.TopScoresRequest();
        topScoresRequest.setSize();

        let call = null;
        let requestCancelTimeout = setTimeout(function() { if (call) { call.cancel() } }, CONSTANTS.CANCEL_TIMEOUT);

        call = this.leaderboardServiceClient
            .getTopScores(topScoresRequest, { },
                function(err, response) {
                    clearTimeout(requestCancelTimeout);

                    if (err) {
                        console.log(err);
                    } else {
                        callback(response);
                    }
                }
        );
    }

    updatePlayerScore = (data, callback) => {
        let playerScore = new Requests.PlayerScore();
        playerScore.setPlayerId(data.playerId);
        playerScore.setScore(data.score);
        
        let updateScoreRequest = new Requests.UpdateScoreRequest();
        updateScoreRequest.setPlayerScore(playerScore);

        let call = null;
        let requestCancelTimeout = setTimeout(function() { if (call) { call.cancel() } }, CONSTANTS.CANCEL_TIMEOUT);

        call = this.leaderboardServiceClient
            .updateScore(updateScoreRequest, { },
                function(err, response) {
                    clearTimeout(requestCancelTimeout);

                    if (err) {
                        console.log(err);
                    } else {
                        callback(response);
                    }
                }
        );
    }
}