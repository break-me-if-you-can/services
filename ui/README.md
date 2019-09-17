This project was bootstrapped with [Create React App](https://github.com/facebookincubator/create-react-app).

## Table of Contents

- [Proto Generation](#protogeneration)
    - [bin/protogen](#bin-protogen)
- [Available Scripts](#available-scripts)
    - [yarn clean](#yarn-clean)
    - [yarn start](#yarn-start)
    - [yarn build:prod](#yarn-build-prod)
    - [yarn start:prod](#yarn-start-prod)
- [Deployment](#deployment)
    - [cd bin](#cd-bin)
    - [./protogen](#protogen)
    - [yarn build:prod](#yarn-build-prod)
    - [./gradlew pushdocker](#gradlew-pushdocker)
    - [./gradlew deploy](#gradlew-deploy)

## Available Scripts

In the project directory, you can run:

### `yarn clean`

Removes dist folder

### `yarn start`

Runs the app in the development mode.<br>
Open [http://0.0.0.0:3001](http://0.0.0.0:3001) to view it in the browser.

The page will reload if you make edits.<br>

### `yarn build:prod`

Builds the app for production to the `dist` folder.<br>
It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.<br>
Your app is ready to be deployed!

### `yarn start:prod`

Runs the app in the production mode.<br>
Open [http://0.0.0.0:3001](http://0.0.0.0:3001) to view it in the browser.

## Deployment
    Steps for an application deployment.
    As a prerequisites you need to have gcp configured, got 'export GCP_PROJECT' variable, got protoc and yarn installed.

### `cd bin`
### `./protogen`
### `yarn build:prod`
### `./gradlew pushdocker`
### `./gradlew deploy`

