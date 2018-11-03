var path = require('path');
var webpack = require('webpack');

var cssLoader = {
  module: true,
  importLoaders: 2,
  sourceMap: true,
  localIdentName: '[local]'
};
var jsLoader = {
  react: 'react-hot',
  babel: 'babel'
};
var loaders = {
  js: {
    test: /\.js$/,
    loaders: [jsLoader.react, jsLoader.babel],
    exclude: /node_modules/
  },
  css: {
    test: /\.css$/,
    loaders: ['style-loader', 'css-loader'],
    include: [path.join(__dirname, '../src')]
  },
  fonts: {
    test: /\.(otf|ttf)$/,
    loader: 'url-loader',
    options: {
      limit: 100000,
      name: "fonts/[name].[ext]"
    },
    include: path.join(__dirname, '../assets')
  },
  json: {
    test: /\.json$/,
    loaders:['json'],
    exclude: /node_modules/
  },
  raw: {
    test: /\.tpl$|^((?!\.ng).)*\.html$/,
    loaders: ['raw']
  },
  file: {
    test: /\.jpe?g$|\.gif$|\.png$|\.svg|\.woff|\.woff2|\.ttf|\.wav$|\.mp3$|\.eot/,
    loader: 'file?name=static/[sha512:hash:base64:7].[ext]'
  },
  dataurl: {
    test: /\.jpe?g$|\.gif$|\.png$|\.svg|\.woff|\.woff2|\.ttf|\.wav$|\.mp3$|\.eot/,
    loader: 'url'
  }
}
var productionPlugins = [
  new webpack.optimize.UglifyJsPlugin(),
  new webpack.optimize.DedupePlugin(),
  new webpack.NoErrorsPlugin(),
  new webpack.DefinePlugin({
    'process.env': {
      'GATEWAY_SERVICE_PORT': JSON.stringify('80'),
      'GATEWAY_SERVICE_HOST': JSON.stringify('35.233.196.238'),
      'NODE_ENV': JSON.stringify('production'),
      'BABEL_ENV': JSON.stringify('production')
    }
  })
]
module.exports = {
  devtool: 'eval',
  output: {
    path: path.join(__dirname, '../dist'),
    filename: 'bundle.js',
    publicPath: '../dist/'
  },
  module: {
    loaders: [
      loaders.js, loaders.css, loaders.fonts,
      loaders.raw, loaders.file, loaders.json
    ]
  },
  newExtraParams: {
    loaders: loaders,
    cssLoader: cssLoader,
    jsLoader: jsLoader,
    productionPlugins: productionPlugins
  }
};
