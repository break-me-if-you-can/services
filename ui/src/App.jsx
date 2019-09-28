import { h, render, Component } from 'preact';
import { Game } from './Game';
import { Portrait } from './Messages';
import { CONSTANTS } from './Constants';
import './App.scss';

export class App extends Component {
    constructor(props) {
        super(props);

        const portrait = window.innerHeight > window.innerWidth;
        const deadline = props.matches.deadline === 'true';

        if (portrait) {
            window.addEventListener('orientationchange', this.onOrientationChangedHandler, false);
        }

        this.state = { portrait, deadline };
    }

    onOrientationChangedHandler = () => {
        setTimeout(() => {
            const portrait = window.innerHeight > window.innerWidth;

            this.setState({ portrait });

            window.removeEventListener('orientationchange', this.onOrientationChangedHandler, false);
        }, CONSTANTS.CHECK_ORIENTATION_TIMEOUT);
    }

    render() {
        return (
            <div className="app">
                { this.state.portrait && <Portrait /> }
                { !this.state.portrait && <Game deadline={this.state.deadline}/> }
            </div>
        );
    }
}
