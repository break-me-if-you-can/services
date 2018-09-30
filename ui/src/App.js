import React, { Component } from 'react';
import { Game } from './Game';

export class App extends Component {
  
  constructor(props) {
    super(props);
}

render() {
    return (
        <div>
            <Game />
        </div>
    )
  }
}