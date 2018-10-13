import React, {Component} from 'react';
import * as PIXI from 'pixi.js';
import { Service } from './Service';

import { Goose } from './gameobjects/Goose';
import { Cloud } from './gameobjects/Cloud';
import { Explosion } from './gameobjects/Explosion';

import aircraftStraightImg from '../assets/textures/aircraft_straight.png';
import aircraftLeftTurnSpriteSheetImg from '../assets/spritesheets/aircraft_turn_left.png';
import aircraftRightTurnSpriteSheetImg from '../assets/spritesheets/aircraft_turn_right.png';
import explosionSpriteSheetImg from '../assets/spritesheets/explosion.png';
import gooseSpriteSheetImg from '../assets/spritesheets/goose.png';
import cloudImg from '../assets/textures/cloud.png';
import waterImg from '../assets/textures/water.png';
import banksImg from '../assets/textures/banks.png';

const FIELD_WIDTH = 767;
const FIELD_HEIGHT = 1152;

const GOOSE_WIDTH = 62;
const GOOSE_HEIGHT = 32;
const GOOSE_FRAMES_COUNT = 7;

const CLOUD_WIDTH = 62;

const EXPLOSION_WIDTH = 82;
const EXPLOSION_HEIGHT = 78;
const EXPLOSION_FRAMES_COUNT = 6;

const AIRCRAFT_WIDTH = 90;
const AIRCRAFT_HEIGHT = 101;

const ENGINES_COUNT = 4;
const ENGINE_ALIVE_CLASSNAME = 'alive';
const ENGINE_DEAD_CLASSNAME = 'dead';

const TOP_SCORES_COUNT = 5;

export class Game extends Component {
  
  constructor(props) {
    super(props);
    this.service = new Service();
    this.loader = new PIXI.loaders.Loader();
    this.app = new PIXI.Application(
      {
        width: FIELD_WIDTH,
        height: FIELD_HEIGHT,
        transparent:false,
      }
    );

    this.field = {
      width: window.innerWidth * 0.75,
      height: window.innerHeight,
    }

    let pattern = new RegExp('Android|BlackBerry|iPhone|iPad|iPod|Opera Mini|', 'i');
    this.isMobile = false || navigator.userAgent.match(pattern);

    this.score = 0;
    this.state = {
      playerId: '',
      score: this.score,
      topScores: new Array(TOP_SCORES_COUNT).fill({
        playerId: '',
        score: 0,
      }),
      enginesStatus: new Array(ENGINES_COUNT).fill(ENGINE_ALIVE_CLASSNAME),
      portrait: false,
      gameOver: false,
    }
    
    this.collisionsCounter = 0;
    this.statisticsInterval = null;
    this.scoreInterval = null;
    this.fixtureTimeout = null;

    this.frames = {
      'goose': [],
      'explosion': [],
    };
  }

  getStage = () => this.app.stage;

  gameRefCallback = (element) => {
    element.append(this.app.view);

    this.service.getPlayerId((result) => {
      this.setState({
        playerId: result.getPlayerId(),
      });

      this.loadAssets((loader, resources) => { this.runGame(resources); });
    });
  }

  loadAssets = (onAssetsLoaded) => {
    this.loader
      .add('gooseSpriteSheet', gooseSpriteSheetImg)
        .add('explosionSpriteSheet', explosionSpriteSheetImg)
        .add('aircraftTurnLeftSpriteSheetImg', aircraftLeftTurnSpriteSheetImg)
        .add('aircraftTurnRightSpriteSheetImg', aircraftRightTurnSpriteSheetImg)
        .add('aircraftStraightTexture', aircraftStraightImg)
        .add('cloudTexture', cloudImg)
        .add('waterTexture', waterImg)
        .add('banksTexture', banksImg)
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

    let water = new PIXI.extras.TilingSprite(resources.waterTexture.texture, 634, 1152);
    water.anchor.set(0, 0);
    water.position.x = 65;
    water.position.y = 0;
    water.tilePosition.x = 0;
    water.tilePosition.y = 0;
    this.app.stage.addChild(water);

    let banks = new PIXI.extras.TilingSprite(resources.banksTexture.texture, 767, 1152);
    banks.anchor.set(0, 0);
    banks.position.x = 0;
    banks.position.y = 0;
    banks.tilePosition.x = 0;
    banks.tilePosition.y = 0;
    this.app.stage.addChild(banks);

    this.geese = [];
    this.clouds = [];

    this.gooseFrames = [];
    for (let i = 0; i < GOOSE_FRAMES_COUNT; i++) {
      this.gooseFrames.push(
        new PIXI.Texture(
          resources.gooseSpriteSheet.texture,
          new PIXI.Rectangle(0 + i * (GOOSE_WIDTH + 1), 0, GOOSE_WIDTH, GOOSE_HEIGHT)
        )
      )
    }

    this.explosionFrames = [];
    for (let i = 0; i < EXPLOSION_FRAMES_COUNT; i++) {
      this.explosionFrames.push(
        new PIXI.Texture(
          resources.explosionSpriteSheet.texture,
          new PIXI.Rectangle(0 + i * (EXPLOSION_WIDTH + 1), 0, EXPLOSION_WIDTH, EXPLOSION_HEIGHT)
        )
      )
    }

    this.cloudTexture = resources.cloudTexture;

    this.height = window.innerHeight < 1152? window.innerHeight: 1152;

    this.aircraft = new PIXI.Sprite(resources.aircraftStraightTexture.texture);
    this.aircraft.anchor.set(0.5);
    this.aircraft.width = AIRCRAFT_WIDTH;
    this.aircraft.height = AIRCRAFT_HEIGHT;
    this.aircraft.x = 767 / 2 ;
    this.aircraft.y = this.height - 70;
    this.app.stage.addChild(this.aircraft);

    let fixtureCallback = (result) => {
      let resultList = result.getLinesList();

      if (resultList && resultList.length) {
          resultList.forEach(line => {
            let geesePos = line.getGoosePositionsList();
              geesePos.forEach(position => {
                let goose = new Goose( {
                  'frames': this.gooseFrames,
                  'x': position,
                  'y': -50
                });
                goose.addToStage(this.getStage());
                this.geese.push(goose);
              });

              let cloudsPos = line.getCloudPositionsList();
                cloudsPos.forEach(position => {
                  let cloud = new Cloud( {
                    'texture': this.cloudTexture.texture,
                    'x': position,
                    'y': -50
                  });
                  cloud.addToStage(this.getStage());
                  this.clouds.push(cloud);
              });
          });

          this.fixtureTimeout = setTimeout(() => {
            this.service.getFixture(fixtureCallback);
          }, 1000);
      } else {
        this.fixtureTimeout = setTimeout(() => {
          this.service.getFixture(fixtureCallback);
        }, 400);
      }
    }

    this.scoreInterval = setInterval(() => {
      this.setState({
        score: this.score,
      });
    }, 1000);

    this.statisticsInterval = setInterval(() => {

      this.service.updatePlayerScore(
        {
          playerId: this.state.playerId,
          score: this.score,
        },
        (result) => { }
      );

      this.service.getTopPlayerScore((result) => {
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
  
    }, 5000);

    this.service.getFixture(fixtureCallback);
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

  updateEnginesStatus = () => {
    this.collisionsCounter++;
    
    let enginesStatus = (new Array(ENGINES_COUNT).fill(ENGINE_ALIVE_CLASSNAME)).map(
      (obj, i) => (i < this.collisionsCounter? ENGINE_DEAD_CLASSNAME: ENGINE_ALIVE_CLASSNAME)
    );

    this.setState(
      {
        enginesStatus: enginesStatus,
      }
    );
    
    this.checkGameOver();
  }

  checkGameOver = () => {
    if (this.collisionsCounter == ENGINES_COUNT) {

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

      if (this.fixtureTimeout) {
        clearTimeout(this.fixtureTimeout);
      }
    }
  }

  onDeviceOrientationHandler = (event) => {
    //alert(event.alpha, event.beta, event.gamma);
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
      //if (this.field.width * 0.1 < this.aircraft.x && this.aircraft.x < this.field.width * 0.9) {
      let turn = Math.sign(event.gamma) * event.beta / 180 * 30;

      let turnLeft = turn > 0;
      if (turnLeft) {
        if (this.aircraft.x > AIRCRAFT_WIDTH / 2 + turn) {
          this.aircraft.x -= turn;
        }
      } else {
        if (this.aircraft.x < FIELD_WIDTH - (AIRCRAFT_WIDTH / 2 + turn)) {
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
    if (e.keyCode == 37) {
      if (this.aircraft.x > AIRCRAFT_WIDTH / 2) {
        this.aircraft.x -= 5;
      }
    } else if (e.keyCode == 39) {
      if (this.aircraft.x < FIELD_WIDTH - AIRCRAFT_WIDTH / 2) {
        this.aircraft.x += 5;
      }
    }
    //  else if (e.keyCode == 80 && e.ctrlKey && e.altKey) { 
    //   console.log("CTRL + ALT + P: Disable Partial Degradation");
    //   this.service.managePartialDegradation(false, (result) => { 
    //     console.log("Disable Partial Degradation Result: ", result);
    //   });
    // } else if (e.keyCode == 51 && e.ctrlKey && e.shiftKey) { 
    //   console.log("CTRL + SHIFT + 3: Enable Partial Degradation");
    //   this.service.managePartialDegradation(true, (result) => { 
    //     console.log("Enable Partial Degradation Result: ", result);
    //   });
    // }
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
    let topScoresList = this.state.topScores.map(player => <p><span className="black" key={ player.id }>{ player.id }...</span>{player.score} </p> );
    let enginesStatusList = this.state.enginesStatus.map(engineStatus => <div className={ 'engine ' + engineStatus }></div>)

    let message = '';
    if (this.state.gameOver) {
      message = (<div className="message game_over">
                    <p>game over!</p>
                    <a>play again</a>
                  </div>);
    }
    if (this.state.portrait) {
      message = (<div className="message portrait">
                    <p>turn ur phone by 90&deg;!</p>
                  </div>)
    }

    return (
      <div className="container">
        { message}
        <div className="game">
          <div className="left stats">
            <div className="leaderboard">
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