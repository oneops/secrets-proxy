#!/usr/bin/env bash

# This script is used for uploading secrets of
# the Keywhiz Proxy environment itself.

set -e

if [ -z ${SECRETS_TOKEN} ]; then
        echo "SECRETS_TOKEN not set!"
        exit 1;
fi

# Secrets files
secrets_dir=/keywhiz-proxy/secrets/
secrets=(
    'application.yaml::Keywhiz Proxy Application Config Yaml.'
    'keystores/keywhiz_keystore.p12::Keywhiz Automation mTLS Client Certificate.'
    'keystores/keywhiz_proxy_keystore.p12::TLS Server Certificate For Keywhiz-Proxy Application.'
    'keystores/keywhiz_truststore.p12::Keywhiz Server Trust-Store.'
    'keystores/ldap_truststore.p12::LDAP/AD Server Trust-Store.'
)

cd ${secrets_dir}
for index in "${secrets[@]}" ; do
    secret="${index%%::*}"
    desc="${index##*::}"
    name=${secret##*/}
    echo -e "\n\xF0\x9F\x8D\xBB Uploading secret \033[36m$secret\033[0m with name: \033[36m$name\033[0m , desc: $desc"
    base64_content=$(base64 -in ${secret})
    payload="{\"description\":\"${desc}\",\"content\":\"${base64_content}\"}"
    curl -k -X POST -H "Content-Type: application/json" -H "X-Authorization: Bearer ${SECRETS_TOKEN}" https://localhost:8443/v1/apps/oneops_keywhiz-proxy_prod/secrets/${name} -d"$payload"
    echo -e "\n\xE2\x9C\x94 - Uploaded \033[36m$secret\033[0m."
done



