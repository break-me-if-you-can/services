var path = require('path');
var config = require('./webpack.config');

var output = Object.assign({}, config.output);
output.publicPath = '/dist';

var newExtraParams = Object.assign({}, config.newExtraParams);

module.exports = Object.assign({}, config, {
  mode: 'production',
  entry: [
    path.join(__dirname, '../src/index')
  ],
  output: output,
  plugins: newExtraParams.productionPlugins,
  devServer: {
    publicPath: '',
    compress: true,
    contentBase: path.join(__dirname, '../public'),
    headers: { 'Access-Control-Allow-Origin': '*' },
    historyApiFallback: true,
    hot: false
  }
});
