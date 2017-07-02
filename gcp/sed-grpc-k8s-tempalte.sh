#!/bin/sh

set -ex

sed -e "s/{SERVICE_NAME}/$1/g" -e "s/{SERVICE_CONFIG_ID}/$2/g" -e "s/{API_KEY}/$3/g" gcp/grpc-k8s-template.yaml > gcp/grpc-k8s.yaml
