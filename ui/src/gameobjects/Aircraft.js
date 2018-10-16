import * as PIXI from 'pixi.js';
export class Aircraft extends PIXI.Sprite {

    constructor(props) {
        super(props.image.texture);
        this.anchor.set(0.5);
        this.width = props.width;
        this.height = props.height;
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