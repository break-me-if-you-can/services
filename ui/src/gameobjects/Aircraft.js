import * as PIXI from 'pixi.js';
export class Aircraft extends PIXI.extras.AnimatedSprite {

    constructor(props) {
        super(props.frames);
        this.anchor.set(0.5);
        this.x = props.x;
        this.y = props.y;
        this.animationSpeed = props.animationSpeed || 0.15;
        this.loop = false;
    }

    setPosition(props) {
        this.x = props.x;
        this.y = props.y;
    }

    addToStage(stage) {
        stage.addChild(this);
    }

    removeFromStage(stage) {
        stage.removeChild(this);
    }

}