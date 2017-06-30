#!/bin/sh

set -ex

docker build -f docker/chgrpc -t vrch/chgrpc .
docker build -f docker/vrgrpc -t vrch/vrgrpc .
docker build -f docker/vrchgrpc -t vrch/vrchgrpc .
