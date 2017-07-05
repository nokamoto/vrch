#!/bin/sh

set -ex

if [ -z $2 ]
then
    sed -e "s/{PROJECT_ID}/$1/g" gcp/api_config-template.yaml > gcp/api_config.yaml
else
    sed -e "s/{PROJECT_ID}/$1/g" -e "s/{EXTERNAL_IP}/$2/g" gcp/api_config_dns-template.yaml > gcp/api_config.yaml
fi
