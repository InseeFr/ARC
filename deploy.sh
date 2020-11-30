#!/bin/bash

DEPLOY_TAG=$1

#Build and deploy inseefr/arc
docker build -f app.Dockerfile -t inseefr/arc:$DEPLOY_TAG . 
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin && docker push inseefr/arc:$DEPLOY_TAG

#Build and deploy inseefr/arc-ws
docker build -f app-ws.Dockerfile -t inseefr/arc-ws:$DEPLOY_TAG . 
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin && docker push inseefr/arc-ws:$DEPLOY_TAG
