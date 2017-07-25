#!/bin/sh

set -ex

# prerequisite: sbt vrchgrpc/assembly slackbridge/assembly

version="v0"

cd docker

docker build -f vrchgrpc -t nokamotohub/vrchgrpc .
docker build -f slackbridge -t nokamotohub/slackbridge .

docker push nokamotohub/vrchgrpc
docker tag nokamotohub/vrchgrpc nokamotohub/vrchgrpc:$version
docker push nokamotohub/vrchgrpc:$version

docker push nokamotohub/slackbridge
docker tag nokamotohub/slackbridge nokamotohub/slackbridge:$version
docker push nokamotohub/slackbridge:$version
