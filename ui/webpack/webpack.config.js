var path = require('path');
var webpack = require('webpack');

function params(json) {
  var result = [];
  for(var i in json) {
    if(typeof json[i] === 'boolean' && json[i]){
      result.push(i);
    }
    else{
      result.push(i+'='+json[i]);
    }
  }
  return result.join('&')
}

var cssLoader = {
  module: true,
  importLoaders: 2,
  sourceMap: true,
  //localIdentName: '[local]___[hash:base64:5]'
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
  scss: {
    test: /\.scss$/,
    loaders: [
      'style',
      'css?'+params(cssLoader),
      'postcss',
      'sass'
    ]
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
  nghtml: {
    test: /\.ng\.html$/,
    loaders: [
      'ngtemplate?relativeTo='+path.join(__dirname, '../src'),
      'raw'
    ]
  },
  file: {
    test: /\.jpe?g$|\.gif$|\.png$|\.svg|\.woff|\.woff2|\.ttf|\.wav$|\.mp3$|\.eot/,
    loader: 'file?name=static/[sha512:hash:base64:7].[ext]'
  },
  dataurl: {
    test: /\.jpe?g$|\.gif$|\.png$|\.svg|\.woff|\.woff2|\.ttf|\.wav$|\.mp3$|\.eot/,
    loader: 'url'
  },
  doc: {
    test: /__doc__\/index\.js$/,
    loaders: ['react-router', jsLoader.babel],
    exclude: /node_modules/
  },
}
var productionPlugins = [
  new webpack.optimize.UglifyJsPlugin(),
  new webpack.optimize.DedupePlugin(),
  new webpack.NoErrorsPlugin(),
  new webpack.DefinePlugin({
    'process.env': {
      'GATEWAY_SERVICE_PORT': JSON.stringify(process.env.GATEWAY_SERVICE_PORT || '80'),
      'GATEWAY_SERVICE_HOST': JSON.stringify(process.env.GATEWAY_SERVICE_HOST || '35.233.196.238'),
      'NODE_ENV': JSON.stringify('production'),
      'BABEL_ENV': JSON.stringify('production')
    },
    'THEME': {
      'ISNIGHT': JSON.stringify(false),
      'PORT': JSON.stringify('3001'),
    }
  })
]
module.exports = {
  devtool: 'eval',
  output: {
    path: path.join(__dirname, '../dist'),
    filename: 'bundle.js',
    publicPath: 'http://0.0.0.0:3001/dist/'
  },
  module: {
    loaders: [
      loaders.js, loaders.css, loaders.fonts,
      loaders.raw, loaders.file, loaders.doc,
      loaders.json, loaders.nghtml
    ]
  },
  newExtraParams: {
    loaders: loaders,
    cssLoader: cssLoader,
    jsLoader: jsLoader,
    params: params,
    productionPlugins: productionPlugins
  }
};
