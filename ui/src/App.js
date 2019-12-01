import React, { Component } from 'react';
import { Game } from './Game';
import { Portrait } from './Messages';
import { CONSTANTS } from './Constants';
import './App.css';

export class App extends Component {
  
    constructor(props) {
        super(props);

        let isPortrait = window.innerHeight > window.innerWidth;
        if (isPortrait) {
            window.addEventListener("orientationchange", this.onOrientationChangedHandler, false);
        }
        this.state = {
            portrait: isPortrait,
        }
    }

    onOrientationChangedHandler = (e) => {
        setTimeout(() => {
            let isPortrait = window.innerHeight > window.innerWidth;
            this.setState({
                portrait: isPortrait,
            });

            window.removeEventListener('orientationchange', this.onOrientationChangedHandler, false);
        }, CONSTANTS.CHECK_ORIENTATION_TIMEOUT);
    }
    
    render() {
        let view = (this.state.portrait? (<Portrait />): (<Game />));

        return (
            <div className="app">
                { view }
            </div>
        )
    }
}