// import React, {h, render, Component } from 'react';
import React, {h, render, Component } from 'preact';
import * as PIXI from 'pixi.js';

export class Goose extends PIXI.extras.AnimatedSprite {

    constructor(props) {
        super(props.frames);
        this.anchor.set(0.5);
        this.x = props.x;
        this.y = props.y;
        this.scale.set(props.ratio);
        this.animationSpeed = props.animationSpeed || 0.25;
    }

    setPosition(props) {
        this.x = props.x;
        this.y = props.y;
    }

    addToStage(stage) {
        this.play();
        stage.addChild(this);
    }

    removeFromStage(stage) {
        stage.removeChild(this);
    }

}