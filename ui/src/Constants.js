const DEFAULT_TIMEOUT = 1000;
const LINES_COUNT = 4;
const FIELD_WIDTH = 767;
const WATER_WIDTH = 634;
const CUT_OFF_OFFSET = 50;
const AIRCRAFT_HORIZONTAL_STEP_MAX = 30;
const DEFAULT_CANCEL_TIMEOUT = 900;
const GATEWAY_SERVICE_HOST = process.env.GATEWAY_SERVICE_HOST;
const GATEWAY_SERVICE_PORT = process.env.GATEWAY_SERVICE_PORT;

export const CONSTANTS = {

    GATEWAY_SERVICE_HOST: 'http://' + GATEWAY_SERVICE_HOST + ':' + GATEWAY_SERVICE_PORT,

    EPSILON: 0.03,
    FIELD_WIDTH: FIELD_WIDTH,
    FIELD_HEIGHT: 1152,
    WATER_WIDTH:  634,
    WATER_HEIGHT: 1152,
    WATER_HORIZONTAL_OFFSET: (FIELD_WIDTH - WATER_WIDTH) / 2,

    LINES_COUNT: LINES_COUNT,

    ANGLE_NORM_CUT_OFF: 0.85,
    CUT_OFF_OFFSET: CUT_OFF_OFFSET,
    START_Y_POSITION: -1 * CUT_OFF_OFFSET,

    GOOSE_WIDTH: 62,
    GOOSE_HEIGHT: 32,
    GOOSE_FRAMES_COUNT: 7,

    CLOUD_WIDTH: 62,

    BETA_MAX_ABS: 180,
    GAMMA_MAX_ABS: 90,

    AIRCRAFT_HORIZONTAL_STEP_MAX: AIRCRAFT_HORIZONTAL_STEP_MAX,
    AIRCRAFT_KEYPRESS_HORIZONTAL_STEP_MAX: AIRCRAFT_HORIZONTAL_STEP_MAX / 6,

    GOOSE_VELOCITY: 2.5,
    CLOUD_VELOCITY: 2.7,
    WATER_VELOCITY: 1.25,
    BANKS_VELOCITY: 0.85,

    POINTS_PER_TICK: 25,

    U_KEYCODE: 85,
    Y_KEYCODE: 89,
    LEFT_ARROW_KEYCODE: 37,
    RIGHT_ARROW_KEYCODE: 39,

    EXPLOSION_WIDTH: 82,
    EXPLOSION_HEIGHT: 78,
    EXPLOSION_FRAMES_COUNT: 6,

    AIRCRAFT_LEFT_TURN_FRAMES_COUNT: 4,
    AIRCRAFT_RIGHT_TURN_FRAMES_COUNT: 4,
    AIRCRAFT_STRAIGHT_POSITION: 0,

    AIRCRAFT_WIDTH: 90,
    AIRCRAFT_HEIGHT: 101,
    AIRCRAFT_OFFSET: 30,

    ENGINES_COUNT: 4,
    ENGINE_ALIVE_CLASSNAME: 'alive',
    ENGINE_DEAD_CLASSNAME: 'dead',

    TOP_SCORES_COUNT: 5,

    CHECK_ORIENTATION_TIMEOUT: 200,
    DEFAULT_TIMEOUT: DEFAULT_TIMEOUT,
    TOP_PLAYER_SCORE_INTERVAL: 2500,
    SCORE_INTERVAL: DEFAULT_TIMEOUT,
    PLAYER_ID_INTERVAL: DEFAULT_TIMEOUT,
    FIXTURE_INTERVAL: LINES_COUNT * DEFAULT_TIMEOUT,
    INTERVAL_BETWEEN_LINES: DEFAULT_TIMEOUT,

    CANCEL_TIMEOUT: DEFAULT_CANCEL_TIMEOUT,
};
