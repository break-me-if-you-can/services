import React, {Component} from 'react';
import * as PIXI from 'pixi.js';
import { Service } from './Service';

import { Goose } from './gameobjects/Goose';
import { Cloud } from './gameobjects/Cloud';

import aircraftStraightImg from '../assets/textures/aircraft_straight.png';
import aircraftLeftTurnSpriteSheetImg from '../assets/spritesheets/aircraft_turn_left.png';
import aircraftRightTurnSpriteSheetImg from '../assets/spritesheets/aircraft_turn_right.png';
import explosionSpriteSheetImg from '../assets/spritesheets/explosion.png';
import gooseSpriteSheetImg from '../assets/spritesheets/goose.png';
import cloudImg from '../assets/textures/cloud.png';
import waterImg from '../assets/textures/water.png';
import banksImg from '../assets/textures/banks.png';


export class Game extends Component {
  
  constructor(props) {
    super(props);
    this.service = new Service();
    this.loader = new PIXI.loaders.Loader();
    this.app = new PIXI.Application(
      {
        width: 767,
        height: 1152,
        transparent:false,
      }
    );

    this.score = 100000;
    this.state = {
      playerId: '',
      score: this.score,
      topScores: [],
    }
    
    this.frames = {
      'goose': [],
      'explosion': [],
    };
  }
  
  getStage() {
    return this.app.stage;
  }

  gameRefCallback = (element) => {
    element.append(this.app.view);

    this.service.getPlayerId((result) => {
      this.setState({
        playerId: result.getPlayerId()
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
    for (let i = 0; i < 7; i++) {
      this.gooseFrames.push(new PIXI.Texture(resources.gooseSpriteSheet.texture, new PIXI.Rectangle(0 + i*63, 0, 62, 32)));
    }
    this.cloudTexture = resources.cloudTexture;

    this.height = window.innerHeight < 1152? window.innerHeight: 1152;

    this.aircraft = new PIXI.Sprite(resources.aircraftStraightTexture.texture);
    this.aircraft.anchor.set(0.5);
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
                  'x': 191 + 384 * position / 25,
                  'y': -50
                });
                goose.addToStage(this.getStage());
                this.geese.push(goose);
              });

              let cloudsPos = line.getCloudPositionsList();
                cloudsPos.forEach(position => {
                  let cloud = new Cloud( {
                    'texture': this.cloudTexture.texture,
                    'x': 191 + 384 * position / 25,
                    'y': -50
                  });
                  cloud.addToStage(this.getStage());
                  this.clouds.push(cloud);
              });
          });

          setTimeout(() => {
            this.service.getFixture(fixtureCallback);
          }, 300);
      } else {
        setTimeout(() => {
          this.service.getFixture(fixtureCallback);
        }, 400);
      }
    }

    setInterval(() => {
      this.setState({
        score: this.score
      });
    }, 1000);

    setInterval(() => {
      let data = {
        playerId: this.state.playerId,
        score: this.score
      }

      this.service.updatePlayerScore(data, (result) => {
        //console.log("Update Player Score: ", result);
      });

      this.service.getTopPlayerScore((result) => {
        let topScores = result.getTopScoresList()
        .map(playerScore => {
          return {
            id: playerScore.getPlayerId(), 
            score: playerScore.getScore(),
          }
        });

        this.setState({
          topScores: topScores
        });

        console.log("Top Player Result: ", result.getTopScoresList());
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
          this.app.stage.removeChild(goose);
        } else {
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

  onDeviceOrientationHandler = (event) => {
    //alert(event.alpha, event.beta, event.gamma);
    this.aircraft.x -= Math.sign(event.gamma) * event.beta / 180 * 25;
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
    switch(e.keyCode) {
      case 37: 
              this.aircraft.x -= 5;
              break;
      case 39:
              this.aircraft.x += 5;
              break;
    }
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

    return (
      <div className="container">
        <div className="left stats">
          <div className="leaderboard">
            <div className="black">TOP 5</div>
            <div>{ topScoresList }</div>
          </div>
          <div className="status">
            <h3>ENGINES</h3>
            <div>
              <div className="engine dead"></div>
              <div className="engine dead"></div>
              <div className="engine alive"></div>
              <div className="engine alive"></div>
            </div>
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
    );
  }
}