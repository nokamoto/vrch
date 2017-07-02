#!/bin/sh

set -ex

# prerequisite: sbt vrchgrpc/assembly slackbridge/assembly

docker build -f docker/vrchgrpc -t nokamotohub/vrchgrpc .
docker build -f docker/slackbridge -t nokamotohub/slackbridge .

docker push nokamotohub/vrchgrpc
docker push nokamotohub/slackbridge
