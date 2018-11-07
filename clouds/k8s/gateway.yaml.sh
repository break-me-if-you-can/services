#!/bin/bash
cat <<YAML
apiVersion: apps/v1beta1
kind: Deployment
metadata:
  name: clouds
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: clouds
    spec:
      containers:
        - name: clouds
          image: gcr.io/$GCP_PROJECT/clouds:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8100
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
  name: clouds
spec:
  type: LoadBalancer
  selector:
    app: clouds
  ports:
   - port: 8100
     targetPort: 8100
     protocol: TCP
     name: grpc
YAML
