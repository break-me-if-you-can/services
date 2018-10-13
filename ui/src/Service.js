const grpc = {};
    grpc.web = require('grpc-web');

const { PlayerScore, UpdateScoreRequest, TopScoresRequest, GeneratePlayerIdRequest, GetFixtureRequest } = require("../generated/gateway_pb");
const { LeaderboardServiceClient, FixtureServiceClient, PlayerIdServiceClient } = require("../generated/gateway_grpc_web_pb");
const { InjectFailureRequest, PartialDegradationRequest } = require("../generated/admin/admin_pb");
const { AdminServiceClient } = require("../generated/admin/admin_grpc_web_pb");

export class Service {
  
    constructor(props) {
      //this.fixtureServiceClient = new FixtureServiceClient('http://' + GATEWAY_SERVICE_HOST);
      this.fixtureServiceClient = new FixtureServiceClient('http://35.233.196.238');
      this.playerIdServiceClient = new PlayerIdServiceClient('http://35.233.196.238');
      this.leaderboardServiceClient = new LeaderboardServiceClient('http://35.233.196.238');
      this.adminServiceClient = new AdminServiceClient('http://35.233.196.238');
    }
  
    getFixture = (callback) => {
        let getFixtureRequest = new GetFixtureRequest();
            getFixtureRequest.setLineWidth(767);
            getFixtureRequest.setLinesCount(1);
            getFixtureRequest.setGooseWidth(62);
            getFixtureRequest.setCloudWidth(62);

        this.fixtureServiceClient
            .getFixture(getFixtureRequest, { },
                function(err, response) {
                    callback(response);
                }
            );
    }

    getPlayerId = (callback) => {
        let generatePlayerIdRequest = new GeneratePlayerIdRequest();

        this.playerIdServiceClient.generatePlayerId(generatePlayerIdRequest, { },
            function(err, response) {
                callback(response);
            }
        );
    }
    
    getPlayerId = (callback) => {
        let generatePlayerIdRequest = new GeneratePlayerIdRequest();

        this.playerIdServiceClient.generatePlayerId(generatePlayerIdRequest, { },
            function(err, response) {
                callback(response);
            }
        );
    }

    getTopPlayerScore = (callback) => {
        let topScoresRequest = new TopScoresRequest();
        topScoresRequest.setSize(5);

        this.leaderboardServiceClient.getTopScores(topScoresRequest, { },
            function(err, response) {
                callback(response);
            }
        );
    }
    
    updatePlayerScore = (data, callback) => {
        let playerScore = new PlayerScore();
        playerScore.setPlayerId(data.playerId);
        playerScore.setScore(data.score);
        
        let updateScoreRequest = new UpdateScoreRequest();
        updateScoreRequest.setPlayerScore(playerScore);

        this.leaderboardServiceClient.updateScore(updateScoreRequest, { },
            function(err, response) {
                callback(response);
            }
        );
    }

    injectFailure = (callback) => {
        let injectFailureRequest = new InjectFailureRequest();

        this.adminServiceClient.injectFailure(injectFailureRequest, { }, 
            function(err, reposnse) {
                callback(response);
            }
        );
    }

    managePartialDegradation = (enable, callback) => {
        let partialDegradationRequest = new PartialDegradationRequest();
        partialDegradationRequest.setEnable(enable);

        this.adminServiceClient.managePartialDegradation(partialDegradationRequest, { }, 
            function(err, response) {
                callback(response);
            }
        );
    }
}