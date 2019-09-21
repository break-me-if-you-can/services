import React, { h, render } from 'preact';
import { App } from './App';

console.log(`Demo is in ${process.env.NODE_ENV} mode.`);

render(<App />, document.getElementById('root'));
