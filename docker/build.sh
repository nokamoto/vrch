#!/bin/sh

set -ex

# prerequisite: sbt vrchgrpc/assembly

docker build -f docker/vrchgrpc -t nokamotohub/vrchgrpc .

docker push nokamotohub/vrchgrpc
