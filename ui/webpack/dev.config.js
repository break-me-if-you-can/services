var path = require('path');
var webpack = require('webpack');
var config = require('./webpack.config');
var publicPath = config.output.publicPath;
module.exports = Object.assign({}, config, {
  mode: 'development',
  entry: [
    'webpack-dev-server/client?http://0.0.0.0:3001',
    'webpack/hot/only-dev-server',
    path.join(__dirname, '../src/index')
  ],
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('development'),
        'BABEL_ENV': JSON.stringify('development')
      },
      'THEME': {
        'ISNIGHT': JSON.stringify(false),
        'PORT': JSON.stringify('3001'),
      }
    })
  ],
  sassLoader:{
    outputStyle:'expanded',
    sourceMap: true,
    includePaths: [
      path.join(__dirname, '../src/assets/scss/dev'),
      path.join(__dirname, '../node_modules')
    ]
  },
  devServer: {
    compress: true,
    contentBase: path.join(__dirname, '../public'),
    publicPath: publicPath,
    host: '0.0.0.0',
    headers: { 'Access-Control-Allow-Origin': '*' },
    changeOrigin: true,
    historyApiFallback: true,
    disableHostCheck: true,
    hot: true
  }
});
