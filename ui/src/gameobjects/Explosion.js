import * as PIXI from 'pixi.js';

export class Explosion extends PIXI.extras.AnimatedSprite {
    constructor(props) {
        super(props.frames);
        this.anchor.set(0.5);
        this.x = props.x;
        this.y = props.y;
        this.scale.set(props.ratio);
        this.animationSpeed = props.animationSpeed || 0.15;
        this.loop = false;
    }

    playOnce(stage) {
        this.onComplete = () => this.removeFromStage(stage);
        this.addToStage(stage);
    }

    addToStage(stage) {
        this.play();
        stage.addChild(this);
    }

    removeFromStage(stage) {
        stage.removeChild(this);
    }
}
