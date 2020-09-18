#!/usr/bin/env bash
[  -z "$1" ] && NAMESPACE="keptn" || NAMESPACE=$1

kubectl delete secret neoload -n "$NAMESPACE" --ignore-not-found

# Create dynatrace-service
NL_SERVICE_RELEASE="0.7.0"

echo "Delete neoload-service $NL_SERVICE_RELEASE"
wget https://raw.githubusercontent.com/keptn-contrib/neoload-service/$NL_SERVICE_RELEASE/config/neoloadexecutor/service.yaml -O service.yaml
wget https://raw.githubusercontent.com/keptn-contrib/neoload-service/$NL_SERVICE_RELEASE/config/neoloadexecutor/distributor.yaml -O distributor.yaml
#replace the namespace in the deployment


sed -i "s/NAMESPACE_TO_REPLACE/$NAMESPACE/" service.yaml
sed -i "s/NAMESPACE_TO_REPLACE/$NAMESPACE/" distributor.yaml

# to update the link
kubectl delete -f service.yaml --ignore-not-found
kubectl delete -f distributor.yaml --ignore-not-found
