import * as PIXI from 'pixi.js';

export class ParallaxTexture extends PIXI.extras.TilingSprite {
    constructor(props) {
        super(props.image.texture, props.width, props.height);
        this.anchor.set(0, 0);
        this.position.x = props.horizontalOffset || 0;
        this.position.y = props.verticalOffset || 0;
        this.tilePosition.x = 0;
        this.tilePosition.y = 0;
        this.scale.set(props.ratio);
    }

    addToStage(stage) {
        stage.addChild(this);
    }

    removeFromStage(stage) {
        stage.removeChild(this);
    }
}
