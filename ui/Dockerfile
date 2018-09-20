FROM node
WORKDIR /
ADD ./assets assets
ADD ./node_modules node_modules
ADD ./public public
ADD ./src src
ADD .babelrc .
ADD ./devServer.js .
ADD ./index.html .
ADD ./package.json .
ADD ./webpack.config.dev.js .
ADD ./webpack.config.prod.js .
ADD ./yarn.lock .

#RUN apt-get update; apt-get install -y nodejs; apt-get install -y npm
EXPOSE 3001
CMD npm start
