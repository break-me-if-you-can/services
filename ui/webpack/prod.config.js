var path = require('path');
var config = require('./webpack.config');
var publicPath = config.output.publicPath;
var newExtraParams = Object.assign({}, config.newExtraParams);

module.exports = Object.assign({}, config, {
  mode: 'production',
  devtool: 'source-map',
  entry: [
    path.join(__dirname, '../src/index')
  ],
  plugins: newExtraParams.productionPlugins,
  devServer: {
    compress: true,
    host: '0.0.0.0',
    headers: { 'Access-Control-Allow-Origin': '*' },
    historyApiFallback: true,
    hot: false
  }
});
