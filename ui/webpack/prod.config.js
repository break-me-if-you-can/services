var path = require('path');
var webpack = require('webpack');

module.exports = {
  devtool: 'eval',
  stats: {
    errorDetails: true
  },
  entry: [
    'webpack-hot-middleware/client',
    './src/index'
  ],
  output: {
    path: path.join(__dirname, 'dist'),
    filename: 'bundle.js',
    publicPath: '/static/'
  },
  resolve: {
    extensions: ["", ".js", ".jsx", ".css", ".json", ".otf", ".ttf", ".png", ".gif"]
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new webpack.NoErrorsPlugin()
  ],
  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        loaders: ['babel'],
        include: [path.join(__dirname, 'src'), path.join(__dirname, 'generated')]
      },
      {
        test: /\.(png|svg|gif)(\?[a-z0-9=.]+)?$/,
        loader: 'url-loader?limit=100000',
        include: path.join(__dirname, 'assets')
      },
      {
        test: /\.(otf|ttf)$/,
        loader: 'url-loader',
        options: {
          limit: 100000,
          name: "fonts/[name].[ext]"
        },
        include: path.join(__dirname, 'assets')
      },
      {
          test: /\.css$/,
          loaders: ['style-loader', 'css-loader'],
          include: [path.join(__dirname, 'src')]
      },
      {
        test: /\.js$/,
        loaders: ['transform?brfs'],
        include: /node_modules/
      },
      {
        test: /\.json$/,
        loaders: ['json']
      }
    ],
    postLoaders: [
      {
        include: path.resolve(__dirname, 'node_modules/pixi.js'),
        loader: 'transform?brfs'
      }
    ]
  }
};
