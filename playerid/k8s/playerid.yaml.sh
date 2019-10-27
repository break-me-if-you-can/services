#!/bin/bash
cat <<YAML
apiVersion: apps/v1beta1
kind: Deployment
metadata:
  name: playerid
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: playerid
    spec:
      containers:
        - name: playerid
          image: gcr.io/$GCP_PROJECT/playerid:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8110
          env:
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
  name: playerid
spec:
  type: LoadBalancer
  selector:
    app: playerid
  ports:
   - port: 8110
     targetPort: 8110
     protocol: TCP
     name: grpc
YAML
