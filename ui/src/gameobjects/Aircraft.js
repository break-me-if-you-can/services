import * as PIXI from 'pixi.js';

class AircraftSprite extends PIXI.extras.AnimatedSprite {
    constructor(props) {
        super(props.frames);
        this.anchor.set(0.5, 0.5);
        this.x = props.x;
        this.y = props.y;
        this.scale.set(props.ratio);
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
export class Aircraft {

    constructor(props) {
        this.x = props.x,
        this.y = props.y,
        
        this.left = new AircraftSprite({
            frames: props.framesObject.left,
            x: this.x,
            y: this.y,
            ratio: props.ratio,
        });

        this.straight = new AircraftSprite({
            frames: props.framesObject.straight,
            x: this.x,
            y: this.y,
            ratio: props.ratio,
        });

        this.right = new AircraftSprite({
            frames: props.framesObject.right,
            x: this.x,
            y: this.y,
            ratio: props.ratio,
        });
    }

    setPosition(props) {
        this.x = props.x;
        this.y = props.y;
        this.right.setPosition(props);
        this.straight.setPosition(props);
        this.left.setPosition(props);
    }

    getPosition = () => ({
        x: this.x,
        y: this.y,
    })

    removeFromStage(stage) {
        stage.removeChild(this.left);
        stage.removeChild(this.straight);
        stage.removeChild(this.right);
    }

    showStraight(stage) {
        this.removeFromStage(stage);
        stage.addChild(this.straight);
    }

    showLeft(stage) {
        this.removeFromStage(stage);
        this.left.addToStage(stage);
        this.left.play();
    }

    showRight(stage) {
        this.removeFromStage(stage);
        this.right.addToStage(stage);
        this.right.play();
    }
}