import * as PIXI from 'pixi.js';

export class Goose extends PIXI.extras.AnimatedSprite {

    constructor(props) {
        super(props.frames);
        this.anchor.set(0.5);
        this.x = props.x; // 200 + 350 * Math.random();
        this.y = props.y; // 50
        this.animationSpeed = props.animationSpeed || 0.25;
    }

    addToStage(stage) {
        this.play();
        stage.addChild(this);
    }

    removeFromStage(stage) {
        stage.removeChild(this);
    }

}