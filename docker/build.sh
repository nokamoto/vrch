#!/bin/sh

set -ex

# prerequisite: sbt vrchgrpc/assembly slackbridge/assembly

cd docker

docker build -f vrchgrpc -t nokamotohub/vrchgrpc .
docker build -f slackbridge -t nokamotohub/slackbridge .

docker push nokamotohub/vrchgrpc
docker push nokamotohub/slackbridge
