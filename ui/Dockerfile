FROM node
WORKDIR /
ADD ./dist dist
ADD ./package.json .

RUN npm i yarn && yarn global add serve;

EXPOSE 3001
CMD yarn serve:build
