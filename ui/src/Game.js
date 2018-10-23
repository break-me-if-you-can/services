import React, {Component} from 'react';
import ReactDOM from 'react-dom';
import * as PIXI from 'pixi.js';

import { Service } from './Service';

import { Goose } from './gameobjects/Goose';
import { Cloud } from './gameobjects/Cloud';
import { Aircraft } from './gameobjects/Aircraft';
import { Explosion } from './gameobjects/Explosion';
import { ParallaxTexture } from './gameobjects/ParallaxTexture';

import { CONSTANTS } from './Constants';
import { IMAGES } from './Assets';

export class Game extends Component {
  
  constructor(props) {
    super(props);
    this.service = new Service();
    this.loader = new PIXI.loaders.Loader();
    this.app = new PIXI.Application(
      {
        width: CONSTANTS.FIELD_WIDTH,
        height: CONSTANTS.FIELD_HEIGHT,
        transparent:false,
      }
    );

    this.field = {
      width: window.innerWidth * 0.75,
      height: window.innerHeight,
    }

    let pattern = new RegExp('Android|BlackBerry|iPhone|iPad|iPod|Opera Mini|', 'i');
    this.isMobile = false || navigator.userAgent.match(pattern);

    this.mainDiv = null;

    this.score = 0;
    this.state = {
      playerId: '',
      score: this.score,
      topScores: new Array(CONSTANTS.TOP_SCORES_COUNT).fill({
        playerId: '',
        score: 0,
      }),
      enginesStatus: new Array(CONSTANTS.ENGINES_COUNT).fill(CONSTANTS.ENGINE_ALIVE_CLASSNAME),
      portrait: false,
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

    this.frames = {
      'goose': [],
      'explosion': [],
    };
  }

  init = () => {
    this.collisionsCounter = 0;
    this.aircraft.setPosition({
      x: CONSTANTS.FIELD_WIDTH / 2,
      y: this.height - CONSTANTS.AIRCRAFT_OFFSET,
    });

    this.score = 0;
    this.setState({
      score: this.score,
      enginesStatus: new Array(CONSTANTS.ENGINES_COUNT).fill(CONSTANTS.ENGINE_ALIVE_CLASSNAME),
      portrait: false,
      gameOver: false,
    });
    this.focusDiv();
  }

  getStage = () => this.app.stage;

  focusDiv = ()=> { this.mainDiv.focus() }

  gameRefCallback = (element) => {
    this.mainDiv = element;

    this.mainDiv.append(this.app.view);

    this.playerIdInterval = setInterval(() => {
      this.service.getPlayerId((result) => {
        clearInterval(this.playerIdInterval);
        this.setState({
          playerId: result.getPlayerId(),
        });

        this.loadAssets((loader, resources) => { this.runGame(resources); });
      });
    }, CONSTANTS.PLAYER_ID_INTERVAL);

    this.focusDiv();
  }

  loadAssets = (onAssetsLoaded) => {
    this.loader
      .add('gooseSpriteSheet', IMAGES.GOOSE_SPRITESHEET)
        .add('explosionSpriteSheet', IMAGES.EXPLOSION_SPRITESHEET)
        .add('aircraftTurnLeftSpriteSheetImg', IMAGES.AIRCRAFT_LEFT_TURN_SPRITESHEET)
        .add('aircraftTurnRightSpriteSheetImg', IMAGES.AIRCRAFT_RIGHT_TURN_SPRITESHEET)
        .add('aircraftStraightTexture', IMAGES.AIRCRAFT_STRAIGHT)
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

    let water = new ParallaxTexture({
      image: resources.waterTexture,
      width: 634,
      height: 1152,
      horizontalOffset: 65,
    });
    
    let banks = new ParallaxTexture({
      image: resources.banksTexture,
      width: CONSTANTS.FIELD_WIDTH,
      height: CONSTANTS.FIELD_HEIGHT,
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

    this.cloudTexture = resources.cloudTexture;

    this.height = window.innerHeight < CONSTANTS.FIELD_HEIGHT? window.innerHeight: CONSTANTS.FIELD_HEIGHT;

    this.aircraft = new Aircraft({
      image: resources.aircraftStraightTexture,
      width: CONSTANTS.AIRCRAFT_WIDTH,
      height: CONSTANTS.AIRCRAFT_HEIGHT,
      x: CONSTANTS.FIELD_WIDTH / 2,
      y: this.height - CONSTANTS.AIRCRAFT_OFFSET,
    });
    this.aircraft.addToStage(this.getStage());

    this.runIntervals();

    this.app.ticker.add((delta) => {
      water.tilePosition.y += 1.25;
      banks.tilePosition.y += 0.85;

      this.score += 25;

      let tempGeese = this.geese.reduce((newGeese, goose ) => {
         goose.y += 2.5;

        if (goose.y > this.height + 50) {
          goose.removeFromStage(this.getStage());
        } if (Math.abs(goose.y - this.aircraft.y) < 30 && Math.abs(goose.x - this.aircraft.x) < 50) {
          let explosion = new Explosion({
            'frames': this.explosionFrames,
            'x': goose.x,
            'y': goose.y,
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
        cloud.y += 2.7;

        if (cloud.y > this.height + 50) {
          cloud.removeFromStage(this.getStage());
        } else {
          newClouds.push(cloud);
        }
        return newClouds;
      }, []);
    
      this.clouds = tempClouds;
    });
  }

  fixtureCallback = (result) => {
    let resultList = result.getLinesList();

    if (resultList && resultList.length) {
      resultList.forEach(line => {
        console.log(line);
        setTimeout(() => {
          let geesePos = line.getGoosePositionsList();
          geesePos.forEach(position => {
            let goose = new Goose( {
              'frames': this.gooseFrames,
              'x': position,
              'y': CONSTANTS.START_Y_POSITION
            });
            goose.addToStage(this.getStage());
            this.geese.push(goose);
          });

          let cloudsPos = line.getCloudPositionsList();
            cloudsPos.forEach(position => {
              let cloud = new Cloud( {
                'texture': this.cloudTexture.texture,
                'x': position,
                'y': CONSTANTS.START_Y_POSITION
              });
              cloud.addToStage(this.getStage());
              this.clouds.push(cloud);
          });
        }, CONSTANTS.INTERVAL_BETWEEN_LINES)
      });
    }
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
    this.scoreInterval = setInterval( () => {
      this.setState({
        score: this.score,
      });
    }, CONSTANTS.SCORE_INTERVAL);

    this.statisticsUpdatePlayerScoreInterval = setInterval(() => {
      this.service.updatePlayerScore(
        {
          playerId: this.state.playerId,
          score: this.score,
        },
        (result) => { }
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
      this.service.getTopPlayerScore((result) => {
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
      });
  
    }, CONSTANTS.TOP_PLAYER_SCORE_INTERVAL);

    this.fixtureInterval = setInterval(() => { this.service.getFixture(this.fixtureCallback) }, CONSTANTS.FIXTURE_INTERVAL)
  }

  updateEnginesStatus = () => {
    this.collisionsCounter++;
    
    let enginesStatus = (new Array(CONSTANTS.ENGINES_COUNT).fill(CONSTANTS.ENGINE_ALIVE_CLASSNAME)).map(
      (obj, i) => (i < this.collisionsCounter? CONSTANTS.ENGINE_DEAD_CLASSNAME: CONSTANTS.ENGINE_ALIVE_CLASSNAME)
    );

    this.setState(
      {
        enginesStatus: enginesStatus,
      }
    );
    
    this.checkGameOver();
  }

  checkGameOver = () => {
    if (this.collisionsCounter >= CONSTANTS.ENGINES_COUNT) {

      this.app.ticker.stop();

      this.setState({
        gameOver: true,
      });

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
  }

  onDeviceOrientationHandler = (event) => {
    if (window.innerHeight > window.innerWidth) {
      if (this.app.ticker.started) {
        this.app.ticker.stop();
      }
      if (!this.state.portrait) {
        this.setState({
          portrait: true,
        })
      }
    } else {
      if (!this.app.ticker.started) {
        this.app.ticker.start();
      }
      if (this.state.portrait) {
        this.setState({
          portrait: false,
        });
      }

      let turn = Math.sign(event.gamma) * event.beta / 180 * 30;

      let turnLeft = turn > 0;
      if (turnLeft) {
        if (this.aircraft.x > CONSTANTS.AIRCRAFT_WIDTH / 2 + turn) {
          this.aircraft.x -= turn;
        }
      } else {
        if (this.aircraft.x < CONSTANTS.FIELD_WIDTH - (CONSTANTS.AIRCRAFT_WIDTH / 2 + turn)) {
          this.aircraft.x -= turn;
        }
      }
    }
  }

  onDeviceMotionHandler = (event) => {
    alert('Device Motion Event', e);
    tilt([event.acceleration.x * 2, event.acceleration.y * 2]);
  }

  onMozOrientationHandler = (event) => {
    alert('MozOrientation');
    tilt([orientation.x * 50, orientation.y * 50]);
  }

  onOrientationChangedHandler = (e) => {
    console.log('orientation changed', e);
  }

  onKeyDownHandler = (e) => {
    if (e.keyCode == 37) { // left arrow
      if (this.aircraft.x > CONSTANTS.AIRCRAFT_WIDTH / 2) {
        this.aircraft.x -= 5;
      }
    }
    else if (e.keyCode == 39) { // right arrow
      if (this.aircraft.x < CONSTANTS.FIELD_WIDTH - CONSTANTS.AIRCRAFT_WIDTH / 2) {
        this.aircraft.x += 5;
      }
    }
    else if (e.keyCode == 85 && e.ctrlKey) { // u + CTRL: LB off
      this.leaderboardComboPressed = true;
    }
    else if (e.keyCode == 89 && e.ctrlKey) { // y + CTRL: LB on
      this.leaderboardComboPressed = false;
    }

  }

  startAgain = (e) => {
    this.removeGeese();
    this.removeClouds();
    this.init();
    this.runIntervals();
    this.app.ticker.start();
  }

  componentDidMount() {
    window.addEventListener("orientationchange", this.onOrientationChangedHandler, false);
    
    if (window.DeviceOrientationEvent) {
      window.addEventListener("deviceorientation", this.onDeviceOrientationHandler, true);
    } else if (window.DeviceMotionEvent) {
      window.addEventListener('devicemotion', this.onDeviceMotionHandler, true);
    } else {
      window.addEventListener("MozOrientation", this.onOrientationChangedHandler, true);
    }
  }

  componentWillUnmount() {
    window.removeEventListener('orientationchange', this.onOrientationChangedHandler, false);
  
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
      message = (<div className="game_over">
                    <div className="game_over content">
                      <div className="wrapper">
                        <div>
                          <p>game over!</p>
                        </div>
                        <div>
                          <div className="goose_gameover"></div>
                          <div className="goose_gameover"></div>
                          <div className="goose_gameover"></div>
                          <div className="goose_gameover"></div>
                        </div>
                        <div className="play_again" onClick={ (e) => this.startAgain(e) }>
                          <p><a href="#">play again</a></p>
                        </div>
                      </div>
                    </div>
                  </div>);
    }

    if (this.state.portrait) {
      message = (
        <div className="message portrait">
          <img src= { portraitGif } alt=""></img>
        </div>
      );
    }

    let leaderboardBlinking = '';
    // console.log('Combo + LB down', this.leaderboardComboPressed && this.state.leaderboardDown);
    if (this.leaderboardComboPressed && this.state.leaderboardDown) {
      leaderboardBlinking = 'blinking'
    }

    return (
      <div className="container">
        { message}
        <div className="game">
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
              tabIndex="0">
          </div>
        </div>
      </div>
    );
  }
}