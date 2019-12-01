FROM node
WORKDIR /
ADD ./bin bin
ADD ./dist dist
ADD ./public public
ADD ./package.json .

RUN npm install express;

EXPOSE 3001
CMD npm run start:prod
