import * as PIXI from 'pixi.js';

export class Cloud extends PIXI.Sprite {
    constructor(props) {
        super(props.texture);
        this.anchor.set(0.5);
        this.x = props.x;
        this.y = props.y;
        this.scale.set(props.ratio);
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
