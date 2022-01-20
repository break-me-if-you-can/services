#!/bin/bash
cat <<YAML
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ui
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ui
  template:
    metadata:
      labels:
        app: ui
    spec:
      containers:
        - name: ui
          image: eu.gcr.io/$GCP_PROJECT/ui:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8100
          env:
            - name: foobar
              value: "$(date +%s)"
            - name: GATEWAY_SERVICE_HOST
              value: "envoy"
            - name: GATEWAY_SERVICE_PORT
              value: "80"
---
apiVersion: v1
kind: Service
metadata:
  name: ui
spec:
  type: LoadBalancer
  selector:
    app: ui
  ports:
   - port: 80
     targetPort: 3001
     protocol: TCP
     name: http
YAML
