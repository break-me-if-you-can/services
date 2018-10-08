import * as PIXI from 'pixi.js';

export class Cloud extends PIXI.Sprite {
    
    constructor(props) {
        super(props.texture);
        this.anchor.set(0.5);
        this.x = props.x; // 200 + 350 * Math.random();
        this.y = props.y; // -50;
    }

    addToStage(stage) {
        stage.addChild(this);
    }

    removeFromStage(stage) {
        stage.removeChild(this);
    }

}