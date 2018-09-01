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
          env:
            - name: foobar
              value: "$(date +%s)"
            - name: ZIPKIN_SERVICE_HOST
              value: "zipkin"
            - name: ZIPKIN_SERVICE_PORT
              value: "9411"
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
YAML