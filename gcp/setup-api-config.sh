#!/bin/sh

set -ex

protoc -Ivr/src/main/protobuf --include_imports \
    --include_source_info vr/src/main/protobuf/vrch/services.proto\
    --descriptor_set_out gcp/out.pb

gcloud service-management deploy gcp/out.pb gcp/api_config.yaml
