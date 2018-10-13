import React, { Component } from 'react';
import { Game } from './Game';
import './App.css';

export class App extends Component {
  
  constructor(props) {
    super(props);
}

render() {
    return (
        <div className="app">
            <Game />
        </div>
    )
  }
}