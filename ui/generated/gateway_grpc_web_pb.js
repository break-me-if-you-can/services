/**
 * @fileoverview gRPC-Web generated client stub for gateway
 * @enhanceable
 * @public
 */

// GENERATED CODE -- DO NOT EDIT!



const grpc = {};
grpc.web = require('grpc-web');

const proto = {};
proto.gateway = require('./gateway_pb.js');

/**
 * @param {string} hostname
 * @param {?Object} credentials
 * @param {?Object} options
 * @constructor
 * @struct
 * @final
 */
proto.gateway.FixtureServiceClient =
    function(hostname, credentials, options) {
  if (!options) options = {};
  options['format'] = 'text';

  /**
   * @private @const {!grpc.web.GrpcWebClientBase} The client
   */
  this.client_ = new grpc.web.GrpcWebClientBase(options);

  /**
   * @private @const {string} The hostname
   */
  this.hostname_ = hostname;

  /**
   * @private @const {?Object} The credentials to be used to connect
   *    to the server
   */
  this.credentials_ = credentials;

  /**
   * @private @const {?Object} Options for the client
   */
  this.options_ = options;
};


/**
 * @param {string} hostname
 * @param {?Object} credentials
 * @param {?Object} options
 * @constructor
 * @struct
 * @final
 */
proto.gateway.FixtureServicePromiseClient =
    function(hostname, credentials, options) {
  if (!options) options = {};
  options['format'] = 'text';

  /**
   * @private @const {!proto.gateway.FixtureServiceClient} The delegate callback based client
   */
  this.delegateClient_ = new proto.gateway.FixtureServiceClient(
      hostname, credentials, options);

};


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.gateway.GetFixtureRequest,
 *   !proto.gateway.FixtureResponse>}
 */
const methodInfo_GetFixture = new grpc.web.AbstractClientBase.MethodInfo(
  proto.gateway.FixtureResponse,
  /** @param {!proto.gateway.GetFixtureRequest} request */
  function(request) {
    return request.serializeBinary();
  },
  proto.gateway.FixtureResponse.deserializeBinary
);


/**
 * @param {!proto.gateway.GetFixtureRequest} request The
 *     request proto
 * @param {!Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.gateway.FixtureResponse)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.gateway.FixtureResponse>|undefined}
 *     The XHR Node Readable Stream
 */
proto.gateway.FixtureServiceClient.prototype.getFixture =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/gateway.FixtureService/GetFixture',
      request,
      metadata,
      methodInfo_GetFixture,
      callback);
};


/**
 * @param {!proto.gateway.GetFixtureRequest} request The
 *     request proto
 * @param {!Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.gateway.FixtureResponse>}
 *     The XHR Node Readable Stream
 */
proto.gateway.FixtureServicePromiseClient.prototype.getFixture =
    function(request, metadata) {
  return new Promise((resolve, reject) => {
    this.delegateClient_.getFixture(
      request, metadata, (error, response) => {
        error ? reject(error) : resolve(response);
      });
  });
};


/**
 * @param {string} hostname
 * @param {?Object} credentials
 * @param {?Object} options
 * @constructor
 * @struct
 * @final
 */
proto.gateway.LeaderboardServiceClient =
    function(hostname, credentials, options) {
  if (!options) options = {};
  options['format'] = 'text';

  /**
   * @private @const {!grpc.web.GrpcWebClientBase} The client
   */
  this.client_ = new grpc.web.GrpcWebClientBase(options);

  /**
   * @private @const {string} The hostname
   */
  this.hostname_ = hostname;

  /**
   * @private @const {?Object} The credentials to be used to connect
   *    to the server
   */
  this.credentials_ = credentials;

  /**
   * @private @const {?Object} Options for the client
   */
  this.options_ = options;
};


/**
 * @param {string} hostname
 * @param {?Object} credentials
 * @param {?Object} options
 * @constructor
 * @struct
 * @final
 */
proto.gateway.LeaderboardServicePromiseClient =
    function(hostname, credentials, options) {
  if (!options) options = {};
  options['format'] = 'text';

  /**
   * @private @const {!proto.gateway.LeaderboardServiceClient} The delegate callback based client
   */
  this.delegateClient_ = new proto.gateway.LeaderboardServiceClient(
      hostname, credentials, options);

};


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.gateway.TopScoresRequest,
 *   !proto.gateway.TopScoresResponse>}
 */
const methodInfo_GetTopScores = new grpc.web.AbstractClientBase.MethodInfo(
  proto.gateway.TopScoresResponse,
  /** @param {!proto.gateway.TopScoresRequest} request */
  function(request) {
    return request.serializeBinary();
  },
  proto.gateway.TopScoresResponse.deserializeBinary
);


/**
 * @param {!proto.gateway.TopScoresRequest} request The
 *     request proto
 * @param {!Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.gateway.TopScoresResponse)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.gateway.TopScoresResponse>|undefined}
 *     The XHR Node Readable Stream
 */
proto.gateway.LeaderboardServiceClient.prototype.getTopScores =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/gateway.LeaderboardService/GetTopScores',
      request,
      metadata,
      methodInfo_GetTopScores,
      callback);
};


/**
 * @param {!proto.gateway.TopScoresRequest} request The
 *     request proto
 * @param {!Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.gateway.TopScoresResponse>}
 *     The XHR Node Readable Stream
 */
proto.gateway.LeaderboardServicePromiseClient.prototype.getTopScores =
    function(request, metadata) {
  return new Promise((resolve, reject) => {
    this.delegateClient_.getTopScores(
      request, metadata, (error, response) => {
        error ? reject(error) : resolve(response);
      });
  });
};


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.gateway.UpdateScoreRequest,
 *   !proto.gateway.UpdateScoreResponse>}
 */
const methodInfo_UpdateScore = new grpc.web.AbstractClientBase.MethodInfo(
  proto.gateway.UpdateScoreResponse,
  /** @param {!proto.gateway.UpdateScoreRequest} request */
  function(request) {
    return request.serializeBinary();
  },
  proto.gateway.UpdateScoreResponse.deserializeBinary
);


/**
 * @param {!proto.gateway.UpdateScoreRequest} request The
 *     request proto
 * @param {!Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.gateway.UpdateScoreResponse)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.gateway.UpdateScoreResponse>|undefined}
 *     The XHR Node Readable Stream
 */
proto.gateway.LeaderboardServiceClient.prototype.updateScore =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/gateway.LeaderboardService/UpdateScore',
      request,
      metadata,
      methodInfo_UpdateScore,
      callback);
};


/**
 * @param {!proto.gateway.UpdateScoreRequest} request The
 *     request proto
 * @param {!Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.gateway.UpdateScoreResponse>}
 *     The XHR Node Readable Stream
 */
proto.gateway.LeaderboardServicePromiseClient.prototype.updateScore =
    function(request, metadata) {
  return new Promise((resolve, reject) => {
    this.delegateClient_.updateScore(
      request, metadata, (error, response) => {
        error ? reject(error) : resolve(response);
      });
  });
};


/**
 * @param {string} hostname
 * @param {?Object} credentials
 * @param {?Object} options
 * @constructor
 * @struct
 * @final
 */
proto.gateway.PlayerIdServiceClient =
    function(hostname, credentials, options) {
  if (!options) options = {};
  options['format'] = 'text';

  /**
   * @private @const {!grpc.web.GrpcWebClientBase} The client
   */
  this.client_ = new grpc.web.GrpcWebClientBase(options);

  /**
   * @private @const {string} The hostname
   */
  this.hostname_ = hostname;

  /**
   * @private @const {?Object} The credentials to be used to connect
   *    to the server
   */
  this.credentials_ = credentials;

  /**
   * @private @const {?Object} Options for the client
   */
  this.options_ = options;
};


/**
 * @param {string} hostname
 * @param {?Object} credentials
 * @param {?Object} options
 * @constructor
 * @struct
 * @final
 */
proto.gateway.PlayerIdServicePromiseClient =
    function(hostname, credentials, options) {
  if (!options) options = {};
  options['format'] = 'text';

  /**
   * @private @const {!proto.gateway.PlayerIdServiceClient} The delegate callback based client
   */
  this.delegateClient_ = new proto.gateway.PlayerIdServiceClient(
      hostname, credentials, options);

};


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.gateway.GeneratePlayerIdRequest,
 *   !proto.gateway.GeneratePlayerIdResponse>}
 */
const methodInfo_GeneratePlayerId = new grpc.web.AbstractClientBase.MethodInfo(
  proto.gateway.GeneratePlayerIdResponse,
  /** @param {!proto.gateway.GeneratePlayerIdRequest} request */
  function(request) {
    return request.serializeBinary();
  },
  proto.gateway.GeneratePlayerIdResponse.deserializeBinary
);


/**
 * @param {!proto.gateway.GeneratePlayerIdRequest} request The
 *     request proto
 * @param {!Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.gateway.GeneratePlayerIdResponse)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.gateway.GeneratePlayerIdResponse>|undefined}
 *     The XHR Node Readable Stream
 */
proto.gateway.PlayerIdServiceClient.prototype.generatePlayerId =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/gateway.PlayerIdService/GeneratePlayerId',
      request,
      metadata,
      methodInfo_GeneratePlayerId,
      callback);
};


/**
 * @param {!proto.gateway.GeneratePlayerIdRequest} request The
 *     request proto
 * @param {!Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.gateway.GeneratePlayerIdResponse>}
 *     The XHR Node Readable Stream
 */
proto.gateway.PlayerIdServicePromiseClient.prototype.generatePlayerId =
    function(request, metadata) {
  return new Promise((resolve, reject) => {
    this.delegateClient_.generatePlayerId(
      request, metadata, (error, response) => {
        error ? reject(error) : resolve(response);
      });
  });
};


module.exports = proto.gateway;

