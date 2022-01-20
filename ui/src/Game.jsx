import React, {h, render, Component} from 'preact';
import * as PIXI from 'pixi.js';

import { Service } from './Service';

import { Goose } from './gameobjects/Goose';
import { Cloud } from './gameobjects/Cloud';
import { Aircraft } from './gameobjects/Aircraft';
import { Explosion } from './gameobjects/Explosion';
import { ParallaxTexture } from './gameobjects/ParallaxTexture';

import { CONSTANTS } from './Constants';
import { IMAGES } from './Assets';

import * as Messages from './Messages';

export class Game extends Component {

  constructor(props) {
    super(props);
    this.service = new Service();
     // Use the native window resolution as the default resolution
     // will support high-density displays when rendering
     PIXI.settings.RESOLUTION = window.devicePixelRatio;

     // Disable interpolation when scaling, will make texture be pixelated
     PIXI.settings.SCALE_MODE = PIXI.SCALE_MODES.NEAREST;
    this.loader = new PIXI.loaders.Loader();

    this.app = new PIXI.Application(
      {
        width:  CONSTANTS.FIELD_WIDTH,
        height: CONSTANTS.FIELD_HEIGHT,
        transparent:false,
      }
    );

//     let height = Math.min(window.innerHeight, window.innerWidth);
//     height = height < CONSTANTS.FIELD_HEIGHT? height: CONSTANTS.FIELD_HEIGHT;
//
//     let width = Math.max(window.innerHeight, window.innerWidth);
//     width = width < CONSTANTS.FIELD_WIDTH? width: CONSTANTS.FIELD_WIDTH;

    this.app.stage.scale.set(393/850)//CONSTANTS.FIELD_WIDTH/CONSTANTS.FIELD_HEIGHT)

    this.mainDiv = null;
    this.counter = 0;

    this.orientationIsChanging = false;
    this.score = 0;
    this.state = {
      playerId: '',
      score: this.score,
      topScores: new Array(CONSTANTS.TOP_SCORES_COUNT).fill({
        playerId: '',
        score: 0,
      }),
      enginesStatus: new Array(CONSTANTS.ENGINES_COUNT).fill(CONSTANTS.ENGINE_ALIVE_CLASSNAME),
      portrait: window.innerHeight > window.innerWidth,
      gameOver: false,
    }

    this.collisionsCounter = 0;
    this.statisticsInterval = null;
    this.scoreInterval = null;
    this.statisticsUpdatePlayerScoreInterval = null;
    this.statisticsTopPlayerScoreInterval = null;
    this.fixtureInterval = null;
    this.playerIdInterval = null;
    this.leaderboardComboPressed = false;

    var isFirefox = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
    this.aircraftFactor = isFirefox? CONSTANTS.FIREFOX_FACTOR: CONSTANTS.OTHER_BROWSERS;

    this.frames = {
      'goose': [],
      'explosion': [],
    };
  }

  mobileAndTabletcheck = () => {
    var check = false;
    (function(a){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino|android|ipad|playbook|silk/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))) check = true;})(navigator.userAgent||navigator.vendor||window.opera);
    return check;
  };

  init = () => {
    this.collisionsCounter = 0;
    this.score = 0;
    this.setState({
      score: this.score,
      enginesStatus: new Array(CONSTANTS.ENGINES_COUNT).fill(CONSTANTS.ENGINE_ALIVE_CLASSNAME),
      gameOver: false,
    });
    this.focusDiv();
  }

  getStage = () => this.app.stage;

  focusDiv = () => { this.mainDiv.focus() }

  getHeight = () => {
    let height = Math.min(window.innerHeight, window.innerWidth);
    return height < CONSTANTS.FIELD_HEIGHT? height: CONSTANTS.FIELD_HEIGHT;
  }

  getWidth = () => {
    let width = Math.max(window.innerHeight, window.innerWidth);
    return width < CONSTANTS.FIELD_WIDTH? width: CONSTANTS.FIELD_WIDTH;
  }

  setAircraftVerticalPosition = () => this.getHeight() - CONSTANTS.AIRCRAFT_OFFSET - 2 * CONSTANTS.AIRCRAFT_HEIGHT; //this.aircraftFactor * CONSTANTS.AIRCRAFT_HEIGHT;

  getVerticalCutOff = () => this.getHeight() + CONSTANTS.CUT_OFF_OFFSET;

  gameRefCallback = (element) => {
    this.mainDiv = element;
    this.mainDiv.append(this.app.view);
    this.counter = 0;

    this.playerIdInterval = setInterval(() => {
      this.service.getPlayerId()
        .then((result) => {
          clearInterval(this.playerIdInterval);
          this.setState({
            playerId: result.getPlayerId(),
          });

          this.loadAssets((loader, resources) => { this.runGame(resources); });
        },
        (error) => console.log('then error player id', error)
        )
        .catch((error) => console.log('then error player id', error));
    }, CONSTANTS.PLAYER_ID_INTERVAL);

    this.focusDiv();
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

    this.loader.onProgress.add((e) => {
      //console.log('Assets Loading Progress', e);
    });
    this.loader.onError.add((e) => {
      //console.log('Assets Loading Error: ', e);
    });
    this.loader.onLoad.add((e) => {
      //console.log('Asset Loaded: ', e);
    });
    this.loader.onComplete.add((e) => {
      //console.log('Assets Loading Completed', e);
    });
  }

  runGame = (resources) => {
    this.width = Math.max(window.innerWidth, window.innerHeight);
    let availableWidth = this.width;
    this.ratio = availableWidth < CONSTANTS.FIELD_WIDTH? availableWidth / CONSTANTS.FIELD_WIDTH : 1;

    let water = new ParallaxTexture({
      image: resources.waterTexture,
      width: CONSTANTS.WATER_WIDTH,
      height: CONSTANTS.WATER_HEIGHT,
      horizontalOffset: CONSTANTS.WATER_HORIZONTAL_OFFSET,
      ratio: this.ratio,
    });

    let banks = new ParallaxTexture({
      image: resources.banksTexture,
      width: CONSTANTS.FIELD_WIDTH,
      height: CONSTANTS.FIELD_HEIGHT,
      ratio: this.ratio,
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
      )
    }

    this.explosionFrames = [];
    for (let i = 0; i < CONSTANTS.EXPLOSION_FRAMES_COUNT; i++) {
      this.explosionFrames.push(
        new PIXI.Texture(
          resources.explosionSpriteSheet.texture,
          new PIXI.Rectangle(0 + i * (CONSTANTS.EXPLOSION_WIDTH + 1), 0, CONSTANTS.EXPLOSION_WIDTH, CONSTANTS.EXPLOSION_HEIGHT)
        )
      )
    }

    this.aircraftLeftFrames = [];
    for (let i = 0; i < CONSTANTS.AIRCRAFT_LEFT_TURN_FRAMES_COUNT; i++) {
      this.aircraftLeftFrames.push(
        new PIXI.Texture(
          resources.aircraftTurnLeftSpriteSheet.texture,
          new PIXI.Rectangle(0 + i * CONSTANTS.AIRCRAFT_WIDTH, 0, CONSTANTS.AIRCRAFT_WIDTH, CONSTANTS.AIRCRAFT_HEIGHT)
        )
      )
    }

    this.aircraftRightFrames = [];
    for (let i = 0; i < CONSTANTS.AIRCRAFT_RIGHT_TURN_FRAMES_COUNT; i++) {
      this.aircraftRightFrames.push(
        new PIXI.Texture(
          resources.aircraftTurnRightSpriteSheet.texture,
          new PIXI.Rectangle(0 + i * CONSTANTS.AIRCRAFT_WIDTH, 0, CONSTANTS.AIRCRAFT_WIDTH, CONSTANTS.AIRCRAFT_HEIGHT)
        )
      )
    }

    let aircraftFramesObject = {
      left: this.aircraftLeftFrames,
      straight: [ this.aircraftLeftFrames[0] ],
      right: this.aircraftRightFrames,
    }

    this.cloudTexture = resources.cloudTexture;

    this.height = this.getHeight();

    this.aircraft = new Aircraft({
      framesObject: aircraftFramesObject,
      x: -2 * CONSTANTS.AIRCRAFT_WIDTH,
      y: -2 * CONSTANTS.AIRCRAFT_HEIGHT,
      ratio: this.ratio,
    });
    setTimeout( () => {
      let position = {
        x: CONSTANTS.FIELD_WIDTH / 2,
        y: this.setAircraftVerticalPosition(),
      };
      this.aircraft.setPosition(position);
      this.aircraft.showStraight(this.getStage());

      console.log('Height: ', this.getHeight(), window.innerWidth, window.innerHeight )
    }, 200);

    this.runIntervals();

    this.app.ticker.add((delta) => {
      water.tilePosition.y += CONSTANTS.WATER_VELOCITY;
      banks.tilePosition.y += CONSTANTS.BANKS_VELOCITY;

      this.score += CONSTANTS.POINTS_PER_TICK;
      let position = this.aircraft.getPosition();
      let tempGeese = this.geese.reduce((newGeese, goose ) => {
         goose.y += CONSTANTS.GOOSE_VELOCITY;

        if (goose.y > this.getVerticalCutOff()) {
          goose.removeFromStage(this.getStage());
        } if (Math.abs(goose.y - position.y) < CONSTANTS.AIRCRAFT_HEIGHT / 2 && Math.abs(goose.x - position.x) < CONSTANTS.AIRCRAFT_WIDTH / 2) {
          let explosion = new Explosion({
            frames: this.explosionFrames,
            x: goose.x,
            y: goose.y,
            ratio: this.ratio
          });
          goose.removeFromStage(this.getStage());
          explosion.playOnce(this.getStage());
          this.updateEnginesStatus();
        }
         else {
          newGeese.push(goose);
        }
        return newGeese;
      }, []);

      this.geese = tempGeese;

      let tempClouds = this.clouds.reduce((newClouds, cloud ) => {
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
    let goose = new Goose( {
      frames: this.gooseFrames,
      x: position,
      y: CONSTANTS.START_Y_POSITION,
      ratio: this.ratio,
    });
    goose.addToStage(this.getStage());

    return goose;
  }

  createCloud(position) {
    let cloud = new Cloud( {
      texture: this.cloudTexture.texture,
      x: position,
      y: CONSTANTS.START_Y_POSITION,
      ratio: this.ratio,
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

  runIntervals = () => {
    this.clearIntervals();

    this.scoreInterval = setInterval( () => {
      this.setState({
        score: this.score,
      });
    }, CONSTANTS.SCORE_INTERVAL);


    this.statisticsUpdatePlayerScoreInterval = setInterval(() => {

        let playerId = this.state.playerId;
        let score = this.score;

      this.service.updatePlayerScore({ playerId, score })
        .then(
          (result) => { },
          (error) => console.log('then player score', error))
        .catch(
          (error) => console.log('then player score', error)
        );
    }, CONSTANTS.SCORE_INTERVAL);

    this.statisticsTopPlayerScoreInterval = setInterval(() => {
      if (this.leaderboardOk) {
        this.leaderboardOk = false;
        this.setState({
          leaderboardDown: false,
        });
      } else {
        this.setState({
          leaderboardDown: true,
        });
      }
      this.service.getTopPlayerScore()
        .then((result) => {
            this.leaderboardOk = true;
            let topScores = result.getTopScoresList()
            .map(playerScore => {
              return {
                id: playerScore.getPlayerId(),
                score: playerScore.getScore(),
              }
            });

            this.setState({
              topScores: topScores,
            });
          },
        (error) => console.log('then error getTopPlayer', error))
        .catch((error) => console.log('catch error getTopPlayer', error));

    }, CONSTANTS.TOP_PLAYER_SCORE_INTERVAL);

    this.fixtureInterval = setInterval(
        () => {this.service.getFixture()
                  .then((result) => {
                    let resultList = result.getLinesList();

                    if (resultList && resultList.length) {
                      resultList.forEach((line, index) => {
                          setTimeout(() => {
                            let geesePos = line.getGoosePositionsList();
                            geesePos.forEach(position => { this.geese.push(this.createGoose(position)); });

                            let cloudsPos = line.getCloudPositionsList();
                            cloudsPos.forEach(position => { this.clouds.push(this.createCloud(position)); });
                          }, index * CONSTANTS.INTERVAL_BETWEEN_LINES);
                      });
                    }
                  }, (error) => console.log('then getFixture error', error))
                  .catch((error) => console.log('catch getFixture error', error))
                  }, CONSTANTS.FIXTURE_INTERVAL);
  }

  updateEnginesStatus = () => {
    this.collisionsCounter++;

    let enginesStatus = (new Array(CONSTANTS.ENGINES_COUNT).fill(CONSTANTS.ENGINE_ALIVE_CLASSNAME)).map(
      (obj, i) => (i < this.collisionsCounter? CONSTANTS.ENGINE_DEAD_CLASSNAME: CONSTANTS.ENGINE_ALIVE_CLASSNAME)
    );

    this.setState({
      enginesStatus: enginesStatus,
    });

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
        gameOver: true,
      });

      this.clearIntervals();
    }
  }

  updateDimensions = (event) => {
    if (this.aircraft) {
      let isLandscape = window.innerHeight < window.innerWidth;
      if (isLandscape) {
        let position = this.aircraft.getPosition();
        position.y = this.setAircraftVerticalPosition();
        this.aircraft.setPosition(position);
      }
    }
    setTimeout(() => { this.checkOrientation() }, CONSTANTS.CHECK_ORIENTATION_TIMEOUT);
  }

  onBlurHandler = (event) => {
    this.clearIntervals();
    this.app.ticker.stop();
  }

  onFocusHandler = (event) => {
    if (!this.state.gameOver) {
        this.runIntervals();
        this.focusDiv();
        this.app.ticker.start();
    }
  }

  onDeviceOrientationHandler = (event) => {
    if (this.aircraft && window.innerHeight < window.innerWidth) {
      let turn = 0;
      let betaNorm = event.beta / CONSTANTS.BETA_MAX_ABS;
      let gammaNorm = event.gamma / CONSTANTS.GAMMA_MAX_ABS;
      if (Math.abs(betaNorm) < CONSTANTS.ANGLE_NORM_CUT_OFF && Math.abs(gammaNorm) < CONSTANTS.ANGLE_NORM_CUT_OFF) {
        turn = Math.sign(gammaNorm) * CONSTANTS.AIRCRAFT_HORIZONTAL_STEP_MAX * betaNorm;
      }

      let position = this.aircraft.getPosition();
      this.aircraft.removeFromStage(this.getStage());

      if (Math.abs(betaNorm) < CONSTANTS.EPSILON) {
        this.aircraft.showStraight(this.getStage());
      }
      else if (turn > 0) {
        let delta = turn;
        if (position.x > Math.floor(CONSTANTS.AIRCRAFT_WIDTH / 2 + turn)) {
          position.x -= turn;
        } else {
          delta = Math.abs(Math.floor(position.x - CONSTANTS.AIRCRAFT_WIDTH / 2));
          position.x = Math.floor(CONSTANTS.AIRCRAFT_WIDTH / 2);
        }
        this.aircraft.setPosition(position);
        if (delta != 0) {
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
        if (delta != 0) {
          this.aircraft.showRight(this.getStage());
        } else {
          this.aircraft.showStraight(this.getStage());
        }
      } else {
        this.aircraft.showStraight(this.getStage());
      }
    }
  }

  onDeviceMotionHandler = (event) => {
    //console.log('onDeviceMotionHandler');
  }

  onMozOrientationHandler = (e) => {
    //console.log('onDeviceMotionHandler');
  }

  onOrientationChangedHandler = (e) => {
    this.checkOrientation();
  }

  checkOrientation() {
    let isLandscape = window.innerHeight < window.innerWidth;
    this.setState({
        portrait: !isLandscape,
    });
  }

  moveLeft = () => {
    let position = this.aircraft.getPosition();
    if (position.x > CONSTANTS.AIRCRAFT_WIDTH / 2 ) {
      this.aircraft.removeFromStage(this.getStage());
      position.x -= CONSTANTS.AIRCRAFT_KEYPRESS_HORIZONTAL_STEP_MAX;
      this.aircraft.setPosition(position);
      this.aircraft.showLeft(this.getStage());
    }
  }

  moveRight = () => {
    let position = this.aircraft.getPosition();
    if (position.x < CONSTANTS.FIELD_WIDTH - CONSTANTS.AIRCRAFT_WIDTH / 2) {
      this.aircraft.removeFromStage(this.getStage());
      position.x += CONSTANTS.AIRCRAFT_KEYPRESS_HORIZONTAL_STEP_MAX;
      this.aircraft.setPosition(position);
      this.aircraft.showRight(this.getStage());
    }
  }

  onKeyDownHandler = (event) => {
    if (event.keyCode == CONSTANTS.LEFT_ARROW_KEYCODE) {
      this.moveLeft();
    }
    else if (event.keyCode == CONSTANTS.RIGHT_ARROW_KEYCODE) {
      this.moveRight();
    }
    else if (event.keyCode == CONSTANTS.U_KEYCODE && event.ctrlKey) { // u + CTRL: LB off
      this.leaderboardComboPressed = true;
    }
    else if (event.keyCode == CONSTANTS.Y_KEYCODE && event.ctrlKey) { // y + CTRL: LB on
      this.leaderboardComboPressed = false;
    }
  }

  onLeftArrowTouchStart = (event) => {
    this.leftArrowIntervalId = setInterval(() => { this.moveLeft(); }, CONSTANTS.TOUCH_INTERVAL);
  }

  onLeftArrowTouchEnd = (event) => {
    clearInterval(this.leftArrowIntervalId);
    this.aircraft.removeFromStage(this.getStage());
    this.aircraft.showStraight(this.getStage());
  }

  onRightArrowTouchStart = (event) => {
    this.rightArrowIntervalId = setInterval(() => { this.moveRight(); }, CONSTANTS.TOUCH_INTERVAL);
  }

  onRightArrowTouchEnd = (event) => {
    clearInterval(this.rightArrowIntervalId);
    this.aircraft.removeFromStage(this.getStage());
    this.aircraft.showStraight(this.getStage());
  }

  onKeyUpHandler = (e) => {
    this.aircraft.removeFromStage(this.getStage());
    this.aircraft.showStraight(this.getStage());
  }

  startAgain = (e) => {
    this.removeGeese();
    this.removeClouds();
    this.init();
    this.runIntervals();
    this.app.ticker.start();
  }

  preventDefault = (e) => e.preventDefault()

  componentDidMount() {
    window.addEventListener("blur", this.onBlurHandler);
    window.addEventListener("focus", this.onFocusHandler);
    window.addEventListener("resize", this.updateDimensions);
    window.addEventListener("orientationchange", this.onOrientationChangedHandler, false);
    window.addEventListener("touchstart", this.preventDefault);
    window.addEventListener("touchend", this.preventDefault);
    document.body.addEventListener("touchstart", this.preventDefault);
    document.body.addEventListener("touchcancel", this.preventDefault);
    document.body.addEventListener("touchmove", this.preventDefault);
    document.body.addEventListener("touchend", this.preventDefault);

    if (window.DeviceOrientationEvent) {
      window.addEventListener("deviceorientation", this.onDeviceOrientationHandler, true);
    } else if (window.DeviceMotionEvent) {
      window.addEventListener('devicemotion', this.onDeviceMotionHandler, true);
    } else {
      window.addEventListener("MozOrientation", this.onOrientationChangedHandler, true);
    }
  }

  componentWillUnmount() {
    window.removeEventListener("blur", this.onBlurHandler);
    window.removeEventListener("focus", this.onFocusHandler);
    window.removeEventListener("resize", this.updateDimensions);
    window.removeEventListener('orientationchange', this.onOrientationChangedHandler, false);
    window.removeEventListener("touchstart", this.preventDefault);
    window.removeEventListener("touchend", this.preventDefault);
    document.body.removeEventListener("touchstart", this.preventDefault);
    document.body.removeEventListener("touchcancel", this.preventDefault);
    document.body.removeEventListener("touchmove", this.preventDefault);
    document.body.removeEventListener("touchend", this.preventDefault);

    if (window.DeviceOrientationEvent) {
      window.removeEventListener("deviceorientation", this.onDeviceOrientationHandler, true);
    } else if (window.DeviceMotionEvent) {
      window.removeEventListener('devicemotion', this.onDeviceMotionHandler, true);
    } else {
      window.removeEventListener("MozOrientation", this.onOrientationChangedHandler, true);
    }
  }

  render() {
    let topScoresList = this.state.topScores.map((player, i) => <p key={ 'player_' + i }><span className="black">{ player.id }...</span>{player.score} </p> );
    let enginesStatusList = this.state.enginesStatus.map((engineStatus, i) => <div key={ 'engine_' + i }className={ 'engine ' + engineStatus }></div>)

    let message = '';
    if (this.state.gameOver) {
        message = (<Messages.GameOver playAgain={(e) => this.startAgain(e)} />);
    }

    let portraitClass = '';
    let leftArrow = '';
    let rightArrow = '';
    if (this.state.portrait) {
      message = (<Messages.Portrait />);
      portraitClass = 'hideField';
    } else {
      if (this.mobileAndTabletcheck()) {
        leftArrow = (<div className='arrow left'
                          onTouchStart={ (e) => { this.onLeftArrowTouchStart(e); } }
                          onTouchEnd={ (e) => { this.onLeftArrowTouchEnd(e); } }>
                      </div>);
        rightArrow = (<div className='arrow right'
                            onTouchStart={ (e) => { this.onRightArrowTouchStart(e); } }
                            onTouchEnd={ (e) => { this.onRightArrowTouchEnd(e); } }>
                      </div>);
      }
    }

    let leaderboardBlinking = '';
    if (this.leaderboardComboPressed && this.state.leaderboardDown) {
      leaderboardBlinking = 'blinking'
    }

    return (
      <div className="container">
        { message}
        <div className={ "game" + portraitClass }>
          <div className="left stats">
            <div className={"leaderboard " + leaderboardBlinking}>
              <div className="black">TOP 5</div>
              <div>{ topScoresList }</div>
            </div>
            <div className="status">
              <h3>ENGINES</h3>
                <div> { enginesStatusList } </div>
            </div>
            <div className="profile">
              <p className="black">{ this.state.playerId } <br/> score</p>
              <p>{ this.state.score }</p>
            </div>
          </div>
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
