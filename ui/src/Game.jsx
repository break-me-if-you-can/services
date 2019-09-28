import { h, render, Component } from 'preact';
import * as PIXI from 'pixi.js';

import { Service } from './Service';

import { Goose } from './gameobjects/Goose';
import { Cloud } from './gameobjects/Cloud';
import { Aircraft } from './gameobjects/Aircraft';
import { Explosion } from './gameobjects/Explosion';
import { ParallaxTexture } from './gameobjects/ParallaxTexture';

import { CONSTANTS } from './Constants';
import { IMAGES } from './Assets';

import { Portrait, Spinner, GameOver } from './Messages';
import * as Helper from './helper';

export class Game extends Component {
    constructor(props) {
        super(props);
        const width = CONSTANTS.FIELD_WIDTH;
        const height = CONSTANTS.FIELD_HEIGHT;
        const transparent = false;

        this.loader = new PIXI.loaders.Loader();
        this.app = new PIXI.Application({ width, height, transparent });

        this.service = new Service(props.deadline);

        const playerId = '';
        const score = 0;
        const topScores = Helper.createArray(CONSTANTS.TOP_SCORES_COUNT, { playerId, score });
        const enginesStatus = Helper.createArray(CONSTANTS.ENGINES_COUNT, CONSTANTS.ENGINE_ALIVE_CLASSNAME);
        const portrait = window.innerHeight > window.innerWidth;
        const gameOver = false;

        this.state = { playerId, score, topScores, enginesStatus, portrait, gameOver };

        this.mainDiv = null;
        this.counter = 0;
        this.orientationIsChanging = false;
        this.score = 0;
        this.collisionsCounter = 0;

        this.statisticsInterval = null;
        this.scoreInterval = null;
        this.statisticsUpdatePlayerScoreInterval = null;
        this.statisticsTopPlayerScoreInterval = null;
        this.fixtureInterval = null;
        this.leaderboardComboPressed = false;

        const goose = [];
        const explosion = [];

        this.frames = { goose, explosion };

        const firefox = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;

        this.aircraftFactor = (firefox ? CONSTANTS.FIREFOX_FACTOR : CONSTANTS.OTHER_BROWSERS);
    }

    init = () => {
        this.collisionsCounter = 0;
        this.score = 0;

        this.setState({
            score: this.score,
            enginesStatus: new Array(CONSTANTS.ENGINES_COUNT).fill(CONSTANTS.ENGINE_ALIVE_CLASSNAME),
            gameOver: false
        });

        this.focusDiv();
    }

    getStage = () => this.app.stage;

    focusDiv = () => this.mainDiv.focus()

    getHeight = () => {
        const height = Math.min(window.innerHeight, window.innerWidth);

        return height < CONSTANTS.FIELD_HEIGHT ? height : CONSTANTS.FIELD_HEIGHT;
    }

    setAircraftVerticalPosition = () => this.getHeight() - CONSTANTS.AIRCRAFT_OFFSET - this.aircraftFactor * CONSTANTS.AIRCRAFT_HEIGHT;

    getVerticalCutOff = () => this.getHeight() + CONSTANTS.CUT_OFF_OFFSET;

    handleError = (message = 'Error occured: ', error) => console.log(message, error);

    handleGetPalyerIdResult = (result) => {
        const playerId = result.getPlayerId();

        this.setState({ playerId });

        this.loadAssets((loader, resources) => this.runGame(resources));
    }

    loadAssets = (onAssetsLoaded) => {
        this.loader
            .add('gooseSpriteSheet', IMAGES.GOOSE_SPRITESHEET)
            .add('explosionSpriteSheet', IMAGES.EXPLOSION_SPRITESHEET)
            .add('aircraftTurnLeftSpriteSheet', IMAGES.AIRCRAFT_LEFT_TURN_SPRITESHEET)
            .add('aircraftTurnRightSpriteSheet', IMAGES.AIRCRAFT_RIGHT_TURN_SPRITESHEET)
            .add('cloudTexture', IMAGES.CLOUD)
            .add('waterTexture', IMAGES.WATER)
            .add('banksTexture', IMAGES.BANKS)
            .load(onAssetsLoaded);
    }

    gameRefCallback = (element) => {
        this.mainDiv = element;
        this.mainDiv.append(this.app.view);
        this.counter = 0;

        this.service.getPlayerId()
            .then(
                (result) => this.handleGetPalyerIdResult(result),
                (error) => this.handleError(error)
            )
            .catch(
                (error) => this.handleError(error)
            );

        this.focusDiv();
    }

    runGame = (resources) => {
        this.width = Math.max(window.innerWidth, window.innerHeight);
        const availableWidth = this.width;

        this.ratio = (availableWidth < CONSTANTS.FIELD_WIDTH ? availableWidth / CONSTANTS.FIELD_WIDTH : 1);

        const image = resources.waterTexture;
        const width = CONSTANTS.WATER_WIDTH;
        const height = CONSTANTS.WATER_HEIGHT;
        const horizontalOffset = CONSTANTS.WATER_HORIZONTAL_OFFSET;
        const ratio = 1;

        const water = new ParallaxTexture({ image, width, height, horizontalOffset, ratio });

        const banks = new ParallaxTexture({
            image: resources.banksTexture,
            width: CONSTANTS.FIELD_WIDTH,
            height: CONSTANTS.FIELD_HEIGHT,
            ratio: 1
        });

        water.addToStage(this.getStage());
        banks.addToStage(this.getStage());

        this.geese = [];
        this.clouds = [];

        this.gooseFrames = [];
        for (let i = 0; i < CONSTANTS.GOOSE_FRAMES_COUNT; i++) {
            this.gooseFrames.push(
                new PIXI.Texture(
                    resources.gooseSpriteSheet.texture,
                    new PIXI.Rectangle(0 + i * (CONSTANTS.GOOSE_WIDTH + 1), 0, CONSTANTS.GOOSE_WIDTH, CONSTANTS.GOOSE_HEIGHT)
                )
            );
        }

        this.explosionFrames = [];
        for (let i = 0; i < CONSTANTS.EXPLOSION_FRAMES_COUNT; i++) {
            this.explosionFrames.push(
                new PIXI.Texture(
                    resources.explosionSpriteSheet.texture,
                    new PIXI.Rectangle(0 + i * (CONSTANTS.EXPLOSION_WIDTH + 1), 0, CONSTANTS.EXPLOSION_WIDTH, CONSTANTS.EXPLOSION_HEIGHT)
                )
            );
        }

        this.aircraftLeftFrames = [];
        for (let i = 0; i < CONSTANTS.AIRCRAFT_LEFT_TURN_FRAMES_COUNT; i++) {
            this.aircraftLeftFrames.push(
                new PIXI.Texture(
                    resources.aircraftTurnLeftSpriteSheet.texture,
                    new PIXI.Rectangle(0 + i * CONSTANTS.AIRCRAFT_WIDTH, 0, CONSTANTS.AIRCRAFT_WIDTH, CONSTANTS.AIRCRAFT_HEIGHT)
                )
            );
        }

        this.aircraftRightFrames = [];
        for (let i = 0; i < CONSTANTS.AIRCRAFT_RIGHT_TURN_FRAMES_COUNT; i++) {
            this.aircraftRightFrames.push(
                new PIXI.Texture(
                    resources.aircraftTurnRightSpriteSheet.texture,
                    new PIXI.Rectangle(0 + i * CONSTANTS.AIRCRAFT_WIDTH, 0, CONSTANTS.AIRCRAFT_WIDTH, CONSTANTS.AIRCRAFT_HEIGHT)
                )
            );
        }

        const aircraftFramesObject = {
            left: this.aircraftLeftFrames,
            straight: [this.aircraftLeftFrames[0]],
            right: this.aircraftRightFrames
        };

        this.cloudTexture = resources.cloudTexture;

        this.height = this.getHeight();

        this.aircraft = new Aircraft({
            framesObject: aircraftFramesObject,
            x: -2 * CONSTANTS.AIRCRAFT_WIDTH,
            y: -2 * CONSTANTS.AIRCRAFT_HEIGHT,
            ratio: this.ratio
        });

        setTimeout(() => {
            const position = {
                x: CONSTANTS.FIELD_WIDTH / 2,
                y: this.setAircraftVerticalPosition()
            };

            this.aircraft.setPosition(position);
            this.aircraft.showStraight(this.getStage());
        }, 100);

        this.runIntervals();

        this.app.ticker.add(() => {
            water.tilePosition.y += CONSTANTS.WATER_VELOCITY;
            banks.tilePosition.y += CONSTANTS.BANKS_VELOCITY;

            this.score += CONSTANTS.POINTS_PER_TICK;
            const position = this.aircraft.getPosition();

            const tempGeese = this.geese.reduce((newGeese, goose) => {
                goose.y += CONSTANTS.GOOSE_VELOCITY;

                if (goose.y > this.getVerticalCutOff()) {
                    goose.removeFromStage(this.getStage());
                }

                if (Math.abs(goose.y - position.y) < CONSTANTS.AIRCRAFT_HEIGHT / 2 && Math.abs(goose.x - position.x) < CONSTANTS.AIRCRAFT_WIDTH / 2) {
                    const explosion = new Explosion({
                        frames: this.explosionFrames,
                        x: goose.x,
                        y: goose.y,
                        ratio: this.ratio
                    });

                    goose.removeFromStage(this.getStage());
                    explosion.playOnce(this.getStage());
                    this.updateEnginesStatus();
                } else {
                    newGeese.push(goose);
                }

                return newGeese;
            }, []);

            this.geese = tempGeese;

            const tempClouds = this.clouds.reduce((newClouds, cloud) => {
                cloud.y += CONSTANTS.CLOUD_VELOCITY;

                if (cloud.y > this.getVerticalCutOff()) {
                    cloud.removeFromStage(this.getStage());
                } else {
                    newClouds.push(cloud);
                }

                return newClouds;
            }, []);

            this.clouds = tempClouds;
        });
    }

    createGoose(position) {
        const goose = new Goose({
            frames: this.gooseFrames,
            x: position,
            y: CONSTANTS.START_Y_POSITION,
            ratio: this.ratio
        });

        goose.addToStage(this.getStage());

        return goose;
    }

    createCloud(position) {
        const cloud = new Cloud({
            texture: this.cloudTexture.texture,
            x: position,
            y: CONSTANTS.START_Y_POSITION,
            ratio: this.ratio
        });

        cloud.addToStage(this.getStage());

        return cloud;
    }

    removeGeese = () => {
        this.geese.forEach(item => item.removeFromStage(this.getStage()));
        this.geese = [];
    }

    removeClouds = () => {
        this.clouds.forEach(item => item.removeFromStage(this.getStage()));
        this.clouds = [];
    }

    updateScoreHandler = () => {

    }

    updatePlayerScoreCall = () => {
        const playerId = this.state.playerId;
        const score = this.score;

        this.service.updatePlayerScore({ playerId, score })
            .then(
                () => { },
                (error) => this.handleError(error)
            )
            .catch(
                (error) => this.handleError(error)
            );
    }

    getTopPlayerScoreCall = () => {
        if (this.leaderboardOk) {
            this.leaderboardOk = false;
            this.setState({ leaderboardDown: false });
        } else {
            this.setState({ leaderboardDown: true });
        }

        this.service.getTopPlayerScore()
            .then(
                (result) => {
                    this.leaderboardOk = true;
                    const topScores = result.getTopScoresList()
                        .map(playerScore => {
                            return {
                                id: playerScore.getPlayerId(),
                                score: playerScore.getScore()
                            };
                        });

                    this.setState({
                        topScores: topScores
                    });
                },
                (error) => this.handleError(error)
            )
            .catch(
                (error) => this.handleError(error)
            );
    }

    renderLine = (line, index) => {
        setTimeout(() => {
            const geesePos = line.getGoosePositionsList();

            geesePos.forEach(position => this.geese.push(this.createGoose(position)));

            const cloudsPos = line.getCloudPositionsList();

            cloudsPos.forEach(position => this.clouds.push(this.createCloud(position)));
        }, index * CONSTANTS.INTERVAL_BETWEEN_LINES);
    }

    getFixtureCall = () => {
        this.service.getFixture()
            .then(
                (result) => {
                    const lines = result.getLinesList();

                    lines.forEach(this.renderLine);
                },
                (error) => this.handleError(error)
            )
            .catch(
                (error) => this.handleError(error)
            );
    }

    runIntervals = () => {
        this.clearIntervals();

        this.scoreInterval = setInterval(() => this.setState({ score: this.score }), CONSTANTS.SCORE_INTERVAL);

        this.statisticsUpdatePlayerScoreInterval = setInterval(this.updatePlayerScoreCall.bind(this), CONSTANTS.SCORE_INTERVAL);

        this.statisticsTopPlayerScoreInterval = setInterval(this.getTopPlayerScoreCall.bind(this), CONSTANTS.TOP_PLAYER_SCORE_INTERVAL);

        this.fixtureInterval = setInterval(this.getFixtureCall.bind(this), CONSTANTS.FIXTURE_INTERVAL);
    }

    updateEnginesStatus = () => {
        this.collisionsCounter++;

        const enginesStatus = (new Array(CONSTANTS.ENGINES_COUNT).fill(CONSTANTS.ENGINE_ALIVE_CLASSNAME)).map(
            (obj, i) => (i < this.collisionsCounter ? CONSTANTS.ENGINE_DEAD_CLASSNAME : CONSTANTS.ENGINE_ALIVE_CLASSNAME)
        );

        this.setState({ enginesStatus });
        this.checkGameOver();
    }

    clearIntervals = () => {
        if (this.scoreInterval) {
            clearInterval(this.scoreInterval);
        }

        if (this.statisticsInterval) {
            clearInterval(this.statisticsInterval);
        }

        if (this.fixtureInterval) {
            clearInterval(this.fixtureInterval);
        }

        if (this.statisticsUpdatePlayerScoreInterval) {
            clearInterval(this.statisticsUpdatePlayerScoreInterval);
        }

        if (this.statisticsTopPlayerScoreInterval) {
            clearInterval(this.statisticsTopPlayerScoreInterval);
        }
    }

    checkGameOver = () => {
        if (this.collisionsCounter >= CONSTANTS.ENGINES_COUNT) {
            this.app.ticker.stop();

            this.setState({
                gameOver: true
            });

            this.clearIntervals();
        }
    }

    updateDimensions = () => {
        if (this.aircraft) {
            const isLandscape = window.innerHeight < window.innerWidth;

            if (isLandscape) {
                const position = this.aircraft.getPosition();

                position.y = this.setAircraftVerticalPosition();
                this.aircraft.setPosition(position);
            }
        }
        setTimeout(() => this.checkOrientation(), CONSTANTS.CHECK_ORIENTATION_TIMEOUT);
    }

    onBlurHandler = () => {
        this.clearIntervals();
        this.app.ticker.stop();
    }

    onFocusHandler = () => {
        this.runIntervals();
        this.focusDiv();
        this.app.ticker.start();
    }

    onDeviceOrientationHandler = (event) => {
        if (this.aircraft && window.innerHeight < window.innerWidth) {
            let turn = 0;

            const betaNorm = event.beta / CONSTANTS.BETA_MAX_ABS;

            const gammaNorm = event.gamma / CONSTANTS.GAMMA_MAX_ABS;

            if (Math.abs(betaNorm) < CONSTANTS.ANGLE_NORM_CUT_OFF && Math.abs(gammaNorm) < CONSTANTS.ANGLE_NORM_CUT_OFF) {
                turn = Math.sign(gammaNorm) * CONSTANTS.AIRCRAFT_HORIZONTAL_STEP_MAX * betaNorm;
            }

            const position = this.aircraft.getPosition();

            this.aircraft.removeFromStage(this.getStage());

            if (Math.abs(betaNorm) < CONSTANTS.EPSILON) {
                this.aircraft.showStraight(this.getStage());
            } else if (turn > 0) {
                let delta = turn;

                if (position.x > Math.floor(CONSTANTS.AIRCRAFT_WIDTH / 2 + turn)) {
                    position.x -= turn;
                } else {
                    delta = Math.abs(Math.floor(position.x - CONSTANTS.AIRCRAFT_WIDTH / 2));
                    position.x = Math.floor(CONSTANTS.AIRCRAFT_WIDTH / 2);
                }
                this.aircraft.setPosition(position);
                if (delta !== 0) {
                    this.aircraft.showLeft(this.getStage());
                } else {
                    this.aircraft.showStraight(this.getStage());
                }
            } else if (turn < 0) {
                let delta = turn;

                if (position.x < Math.floor(CONSTANTS.FIELD_WIDTH - (CONSTANTS.AIRCRAFT_WIDTH / 2 + turn))) {
                    position.x -= turn;
                } else {
                    delta = Math.abs(Math.floor(position.x - (CONSTANTS.FIELD_WIDTH - CONSTANTS.AIRCRAFT_WIDTH / 2)));
                    position.x = Math.floor(CONSTANTS.FIELD_WIDTH - CONSTANTS.AIRCRAFT_WIDTH / 2);
                }
                this.aircraft.setPosition(position);
                if (delta !== 0) {
                    this.aircraft.showRight(this.getStage());
                } else {
                    this.aircraft.showStraight(this.getStage());
                }
            } else {
                this.aircraft.showStraight(this.getStage());
            }
        }
    }

    onOrientationChangedHandler = () => {
        this.checkOrientation();
    }

    checkOrientation() {
        const isLandscape = window.innerHeight < window.innerWidth;

        this.setState({
            portrait: !isLandscape
        });
    }

    moveLeft = () => {
        const position = this.aircraft.getPosition();

        if (position.x > CONSTANTS.AIRCRAFT_WIDTH / 2) {
            this.aircraft.removeFromStage(this.getStage());
            position.x -= CONSTANTS.AIRCRAFT_KEYPRESS_HORIZONTAL_STEP_MAX;
            this.aircraft.setPosition(position);
            this.aircraft.showLeft(this.getStage());
        }
    }

    moveRight = () => {
        const position = this.aircraft.getPosition();

        if (position.x < CONSTANTS.FIELD_WIDTH - CONSTANTS.AIRCRAFT_WIDTH / 2) {
            this.aircraft.removeFromStage(this.getStage());
            position.x += CONSTANTS.AIRCRAFT_KEYPRESS_HORIZONTAL_STEP_MAX;
            this.aircraft.setPosition(position);
            this.aircraft.showRight(this.getStage());
        }
    }

    onKeyDownHandler = (event) => {
        if (event.keyCode === CONSTANTS.LEFT_ARROW_KEYCODE) {
            this.moveLeft();
        } else if (event.keyCode === CONSTANTS.RIGHT_ARROW_KEYCODE) {
            this.moveRight();
        } else if (event.keyCode === CONSTANTS.U_KEYCODE && event.ctrlKey) { // u + CTRL: LB off
            this.leaderboardComboPressed = true;
        } else if (event.keyCode === CONSTANTS.Y_KEYCODE && event.ctrlKey) { // y + CTRL: LB on
            this.leaderboardComboPressed = false;
        }
    }

    onLeftArrowTouchStart = () => {
        this.leftArrowIntervalId = setInterval(() => this.moveLeft(), CONSTANTS.TOUCH_INTERVAL);
    }

    onLeftArrowTouchEnd = () => {
        clearInterval(this.leftArrowIntervalId);
        this.aircraft.removeFromStage(this.getStage());
        this.aircraft.showStraight(this.getStage());
    }

    onRightArrowTouchStart = () => {
        this.rightArrowIntervalId = setInterval(() => this.moveRight(), CONSTANTS.TOUCH_INTERVAL);
    }

    onRightArrowTouchEnd = () => {
        clearInterval(this.rightArrowIntervalId);
        this.aircraft.removeFromStage(this.getStage());
        this.aircraft.showStraight(this.getStage());
    }

    onKeyUpHandler = () => {
        this.aircraft.removeFromStage(this.getStage());
        this.aircraft.showStraight(this.getStage());
    }

    startAgain = () => {
        this.removeGeese();
        this.removeClouds();
        this.init();
        this.runIntervals();
        this.app.ticker.start();
    }

    componentDidMount() {
        window.addEventListener('blur', this.onBlurHandler);
        window.addEventListener('focus', this.onFocusHandler);
        window.addEventListener('resize', this.updateDimensions);
        window.addEventListener('orientationchange', this.onOrientationChangedHandler, false);

        if (window.DeviceOrientationEvent) {
            window.addEventListener('deviceorientation', this.onDeviceOrientationHandler, true);
        } else if (window.DeviceMotionEvent) {
            window.addEventListener('devicemotion', this.onDeviceMotionHandler, true);
        } else {
            window.addEventListener('MozOrientation', this.onOrientationChangedHandler, true);
        }
    }

    componentWillUnmount() {
        window.removeEventListener('blur', this.onBlurHandler);
        window.removeEventListener('focus', this.onFocusHandler);
        window.removeEventListener('resize', this.updateDimensions);
        window.removeEventListener('orientationchange', this.onOrientationChangedHandler, false);

        if (window.DeviceOrientationEvent) {
            window.removeEventListener('deviceorientation', this.onDeviceOrientationHandler, true);
        } else if (window.DeviceMotionEvent) {
            window.removeEventListener('devicemotion', this.onDeviceMotionHandler, true);
        } else {
            window.removeEventListener('MozOrientation', this.onOrientationChangedHandler, true);
        }
    }

    render() {
        const topScoresList = this.state.topScores.map((player, i) => <p key={ 'player_' + i }><span className="black">{ player.id }...</span>{player.score} </p>);

        const enginesStatusList = this.state.enginesStatus.map((engineStatus, i) => <div key={ 'engine_' + i }className={ 'engine ' + engineStatus }></div>);

        let message = '';

        if (this.state.gameOver) {
            message = (<GameOver onClick={ (e) => this.startAgain(e) } />);
        }

        let portraitClass = '';

        let leftArrow = '';

        let rightArrow = '';

        if (this.state.portrait) {
            message = (<Portrait />);
            portraitClass = 'hideField';
        } else if (Helper.mobileAndTabletcheck()) {
            leftArrow = (<div className="arrow left"
                onTouchStart={ (e) => this.onLeftArrowTouchStart(e) }
                onTouchEnd={ (e) => this.onLeftArrowTouchEnd(e) }>
            </div>);
            rightArrow = (<div className="arrow right"
                onTouchStart={ (e) => this.onRightArrowTouchStart(e) }
                onTouchEnd={ (e) => this.onRightArrowTouchEnd(e) }>
            </div>);
        }

        const leaderboardBlinking = (this.leaderboardComboPressed && this.state.leaderboardDown ? 'blinking' : '');

        let stats;

        if (this.state.playerId) {
            stats = (<div>
                <div className={`leaderboard ${leaderboardBlinking}`}>
                    <div className="black">TOP 5</div>
                    <div>{ topScoresList }</div>
                </div>
                <div className="status">
                    <h3>ENGINES</h3>
                    <div>{enginesStatusList}</div>
                </div>
                <div className="profile">
                    <p className="black">{ this.state.playerId } <br/> score</p>
                    <p>{ this.state.score }</p>
                </div>
            </div>);
        } else {
            stats = (
                <div className="stats-spinner">
                    <Spinner />
                </div>
            );
        }

        return (
            <div className="container">
                { message}
                <div className={`game ${portraitClass}`}>
                    <div className="left stats">{stats}</div>
                    <div className="right field" ref={this.gameRefCallback}
                        onKeyDown={(e) => this.onKeyDownHandler(e) }
                        onKeyUp={(e) => this.onKeyUpHandler(e) }
                        tabIndex="0">
                    </div>
                </div>
                { leftArrow } { rightArrow }
            </div>
        );
    }
}
