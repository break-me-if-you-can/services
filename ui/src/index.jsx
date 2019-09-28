import { h, render } from 'preact';
import { App } from './App';
import Router from 'preact-router';

render(
    <Router>
        <App path="/"/>
    </Router>,
    document.getElementById('root'));
