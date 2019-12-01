import { h, render } from 'preact';
import { App } from './App';
import Router from 'preact-router';

render(
    <Router>
        <App path="/:param"/>
        <App default/>
    </Router>,
    document.getElementById('root'));
