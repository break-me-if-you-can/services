import React, {Component} from 'react';
import * as PIXI from 'pixi.js';

// import gooseSpritesheetImg from '../assets/spritesheets/goose.png';
// import aircraftSpritesheetImg from '../assets/spritesheets/aircraft.png';
// import cloudSpritesheetImg from '../assets/spritesheets/cloud.png';
import gooseImg from '../assets/textures/goose.png';
import aircraftImg from '../assets/textures/aircraft.png';
import cloudImg from '../assets/textures/cloud.png';
import waterImg from '../assets/textures/water.png';

export class Game extends Component {
  
  constructor(props) {
    super(props); 
    this.app = new PIXI.Application(
      {
        width: 800,
        height: 756,
        transparent:false
      }
    );
  }
  
  gameRefCallback = (element) => {
    element.append(this.app.view);
    this.runGame();
  }

  runGame = () => {
    let loader = new PIXI.loaders.Loader();
    loader
      .add('gooseTexture', gooseImg)
      .add('aircraftTexture', aircraftImg)
      .add('cloudTexture', cloudImg)
      .add('waterTexture', waterImg)
      //.add('gooseSpriteSheet', gooseSpritesheetImg)
      //.add('aircraftSpriteSheet', aircraftSpritesheetImg)
      //.add('cloudSpriteSheet', cloudSpritesheetImg)
      .load(this.onAssetsLoaded);

    loader.onProgress.add((e) => { console.log('Progress', e); }); // called once per loaded/errored file
    loader.onError.add((e) => { console.log('Error', e); }); // called once per errored file
    loader.onLoad.add((e) => { console.log('On Load', e); }); // called once per loaded file
    loader.onComplete.add((e) => { console.log('Complete', e); });
  }

  onAssetsLoaded = (loader, resources) => {
    console.log('Assets loading complete: ', loader, resources);

    let water = new PIXI.extras.TilingSprite(resources.waterTexture.texture, 800, 756);
    water.anchor.set(0.5);
    water.scale.set(2.0);
    water.position.x = 0;
    water.position.y = 0;
    water.tilePosition.x = 0;
    water.tilePosition.y = 0;
    this.app.stage.addChild(water);

    let goose = new PIXI.Sprite(resources.gooseTexture.texture);
    goose.anchor.set(0.5);
    goose.x = 0;
    goose.y = 0;
    //this.app.stage.addChild(goose);

    let cloud = new PIXI.Sprite(resources.cloudTexture.texture);
    cloud.anchor.set(0.5);
    cloud.x = 100;
    cloud.y = 50;
    //this.app.stage.addChild(cloud);

    this.aircraft = new PIXI.Sprite(resources.aircraftTexture.texture);
    this.aircraft.anchor.set(0.5);
    this.aircraft.scale.set(0.35);
    this.aircraft.x = 400;
    this.aircraft.y = 378 - 30;
    this.app.stage.addChild(this.aircraft);

    this.app.ticker.add((delta) => {
      water.tilePosition.y += 0.25;
    });
  }

  onDeviceOrientationHandler = (event) => {
    this.aircraft.x -= event.beta / 180 * 25;
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
    return (
      <div ref={this.gameRefCallback}
          onKeyDown={(e) => this.onKeyDownHandler(e) }
          tabIndex="0">
      </div>
    );
  }
}