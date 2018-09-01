#!/bin/bash
cat <<YAML
apiVersion: apps/v1beta1
kind: Deployment
metadata:
  name: zipkin
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: zipkin
    spec:
      containers:
        - name: zipkin
          image: gcr.io/stackdriver-trace-docker/zipkin-collector
          imagePullPolicy: Always
          ports:
            - containerPort: 9411
---
apiVersion: v1
kind: Service
metadata:
  name: zipkin
spec:
  type: NodePort
  selector:
    app: zipkin
  ports:
   - port: 9411
     targetPort: 9411
     protocol: TCP
---
YAML
