#!/bin/bash
cat <<YAML
apiVersion: apps/v1beta1
kind: Deployment
metadata:
  name: geese
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: geese
    spec:
      containers:
        - name: geese
          image: gcr.io/$GCP_PROJECT/geese:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8090
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
  name: geese
spec:
  type: LoadBalancer
  selector:
    app: geese
  ports:
   - port: 8090
     targetPort: 8090
     protocol: TCP
     name: grpc
YAML
