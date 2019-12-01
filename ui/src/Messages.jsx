import { h, render } from 'preact';

export function GameOver(props) {
    return (<div className="game_over">
        <div className="game_over content">
            <div className="wrapper">
                <div>
                    <p>game over!</p>
                </div>
                <div>
                    <div className="goose_gameover"></div>
                    <div className="goose_gameover"></div>
                    <div className="goose_gameover"></div>
                    <div className="goose_gameover"></div>
                </div>
                <div className="play_again" onClick={ props.onClick }>
                    <p><a href="#">play again</a></p>
                </div>
            </div>
        </div>
    </div>);
}

export function Notification(props) {
    return (
        <div className="notification_container">
            <div className="notification_content">{props.message}</div>
        </div>
    );
}

export function Portrait() {
    return (<div className="message portrait"></div>);
}

export function Spinner() {
    return (<div className="spinner"></div>);
}
