#!/bin/bash
cat <<YAML
apiVersion: apps/v1beta1
kind: Deployment
metadata:
  name: gateway
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: gateway
    spec:
      containers:
        - name: gateway
          image: gcr.io/$GCP_PROJECT/gateway:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8080
            - containerPort: 8082
            - containerPort: 9080
          env:
            - name: grpc_geese_host
              value: "geese"
            - name: grpc_geese_port
              value: "8090"
            - name: grpc_clouds_host
              value: "clouds"
            - name: grpc_clouds_port
              value: "8100"
            - name: rest_leaderboard_host
              value: "leaderboard"
            - name: rest_leaderboard_port
              value: "8080"
            - name: playerid_host
              value: "playerid"
            - name: playerid_port
              value: "8110"
            - name: foobar
              value: "$(date +%s)"
            - name: ZIPKIN_SERVICE_HOST
              value: "zipkin"
            - name: ZIPKIN_SERVICE_PORT
              value: "9411"
            - name: GCP_PROJECTID
              value: $GCP_PROJECT
---
apiVersion: v1
kind: Service
metadata:
  name: gateway
spec:
  type: LoadBalancer
  selector:
    app: gateway
  ports:
   - port: 8080
     targetPort: 8080
     protocol: TCP
     name: grpc
   - port: 8082
     targetPort: 8082
     protocol: TCP
     name: http
   - port: 9080
     targetPort: 9080
     protocol: TCP
     name: zpages
YAML
