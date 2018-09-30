var path = require('path');
var express = require('express');
var webpack = require('webpack');
var config = require('./webpack.config.dev');

var HOST = process.env.HOST || '0.0.0.0';
var PORT = process.env.PORT || 3001;
console.log('Dev serv:', process);

var app = express();
var compiler = webpack(config);

app.use(require('webpack-dev-middleware')(compiler, {
  noInfo: true,
  publicPath: config.output.publicPath
}));

app.use(require('webpack-hot-middleware')(compiler));

app.get('*', function(req, res) {
  res.sendFile(path.join(__dirname, 'index.html'));
});

app.listen(Number(PORT), HOST, function(err) {
  if (err) {
    console.log(err);
    return;
  }

  console.log('Listening at http://' + HOST + ':' + PORT);
});
