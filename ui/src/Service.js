const grpc = {};
grpc.web = require('grpc-web');

import * as Requests from '../generated/gateway_pb';
import * as Clients from '../generated/gateway_grpc_web_pb';
// { PlayerScore, UpdateScoreRequest, TopScoresRequest, GeneratePlayerIdRequest, GetFixtureRequest } = require("../generated/gateway_pb");
//const { LeaderboardServiceClient, FixtureServiceClient, FixtureServicePromiseClient, PlayerIdServiceClient } = require("../generated/gateway_grpc_web_pb");

const REQUEST_CANCEL_TIMEOUT = 900;
export class Service {
  
    constructor(props) {
      //this.fixtureServiceClient = new FixtureServiceClient('http://' + GATEWAY_SERVICE_HOST);
      this.fixtureServiceClient = new Clients.FixtureServiceClient('http://35.233.196.238');
      this.playerIdServiceClient = new Clients.PlayerIdServiceClient('http://35.233.196.238');
      this.leaderboardServiceClient = new Clients.LeaderboardServiceClient('http://35.233.196.238');
    }
  
    getFixture = (callback) => {
        let getFixtureRequest = new Requests.GetFixtureRequest();
            getFixtureRequest.setLineWidth(767);
            getFixtureRequest.setLinesCount(1);
            getFixtureRequest.setGooseWidth(62);
            getFixtureRequest.setCloudWidth(62);

        let requestCancelTimeout = setTimeout(function() { call.cancel() }, REQUEST_CANCEL_TIMEOUT);
        this.fixtureServiceClient
            .getFixture(getFixtureRequest, { },
                function(err, response) {
                    clearTimeout(requestCancelTimeout);
                
                    if (err) {
                        console.log(err.code);
                        console.log(err.messge);
                    } else {
                        callback(response);
                    }
                }
            );
    }

    getPlayerId = (callback) => {
        let generatePlayerIdRequest = new Requests.GeneratePlayerIdRequest();

        this.playerIdServiceClient.generatePlayerId(generatePlayerIdRequest, { },
            function(err, response) {
                callback(response);
            }
        );
    }
    
    getPlayerId = (callback) => {
        let generatePlayerIdRequest = new Requests.GeneratePlayerIdRequest();

        this.playerIdServiceClient.generatePlayerId(generatePlayerIdRequest, { },
            function(err, response) {
                callback(response);
            }
        );
    }

    getTopPlayerScore = (callback) => {
        let topScoresRequest = new Requests.TopScoresRequest();
        topScoresRequest.setSize(5);

        this.leaderboardServiceClient.getTopScores(topScoresRequest, { },
            function(err, response) {
                callback(response);
            }
        );
    }
    
    updatePlayerScore = (data, callback) => {
        let playerScore = new Requests.PlayerScore();
        playerScore.setPlayerId(data.playerId);
        playerScore.setScore(data.score);
        
        let updateScoreRequest = new Requests.UpdateScoreRequest();
        updateScoreRequest.setPlayerScore(playerScore);

        this.leaderboardServiceClient.updateScore(updateScoreRequest, { },
            function(err, response) {
                callback(response);
            }
        );
    }
}