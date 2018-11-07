import React from 'react';

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

export function Portrait(props) {
  return (<div className="message portrait"></div>);
}