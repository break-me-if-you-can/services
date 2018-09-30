const grpc = {};
    grpc.web = require('grpc-web');

const { GetFixtureRequest, FixtureResponse} = require("../generated/gateway_pb");
const { FixtureServiceClient} = require("../generated/gateway_grpc_web_pb");
const GATEWAY_SERVICE_HOST = process.env.GATEWAY_SERVICE_HOST || '35.233.196.238';

export class Game extends Component {
  
    constructor(props) {
      super(props); 
      this.fixtureServiceClient = new FixtureServiceClient('http://' + GATEWAY_SERVICE_HOST);
    }
  
    getFixture = (callback) => {
        let getFixtureRequest = new GetFixtureRequest();
            getFixtureRequest.setLineWidth(10);
            getFixtureRequest.setLinesCount(1);

        this.fixtureServiceClient
            .getFixture(getFixtureRequest, { },
                function(err, response) { 
                    console.log("Get Fixture Lines: ", response.getLinesList()[0].getGoosePositionsList());
                }
            );
    }
}