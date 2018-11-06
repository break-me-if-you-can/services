var path = require('path');
var webpack = require('webpack');
var config = require('./webpack.config');
var publicPath = config.output.publicPath;
module.exports = Object.assign({}, config, {
  mode: 'development',
  entry: [
    'webpack-hot-middleware/client?http://localhost:3001',
    //'webpack/hot/only-dev-server',
    path.join(__dirname, '../src/index')
  ],
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new webpack.DefinePlugin({
      'process.env': {
        'GATEWAY_SERVICE_PORT': JSON.stringify('80'),
        'GATEWAY_SERVICE_HOST': JSON.stringify('35.233.196.238'),
        'NODE_ENV': JSON.stringify('development'),
        'BABEL_ENV': JSON.stringify('development')
      }
    })
  ],
  devServer: {
    compress: true,
    contentBase: [path.resolve('public')],
    index: 'index.html',
    filename: 'bundle.js',
    publicPath: '/dist/',
    host: '0.0.0.0',
    headers: { 'Access-Control-Allow-Origin': '*' },
    changeOrigin: true,
    historyApiFallback: true,
    disableHostCheck: true,
    hot: true
  }
});
