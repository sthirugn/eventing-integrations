#!/usr/bin/env

# See step 7 of this link: https://clouddot.pages.redhat.com/docs/dev/getting-started/ephemeral/onboarding.html
echo "Checking if you are already logged in"
if ! oc whoami --server="https://api.c-rh-c-eph.8p0c.p1.openshiftapps.com:6443"; then
    echo "Please login using this link: https://oauth-openshift.apps.c-rh-c-eph.8p0c.p1.openshiftapps.com/oauth/token/display"
    exit 1;
fi

echo "Checking for reserved namespace"
export NAMESPACE
NAMESPACE=$(bonfire namespace list --mine | grep "ephemeral" | awk '{print $1"|"$2}' | grep '|true' | awk -F'|' '{print $1}' | head -n1) # we use the first one available

if [[ "$NAMESPACE" == *"ephemeral-"* ]]; then
    echo "Namespace ${NAMESPACE} reserved, extending for 8 hours"    
    bonfire namespace extend "$NAMESPACE" -d '8h0m' 
else
    echo "Reserving namespace for 8 hours"
    export NAMESPACE
    NAMESPACE=$(bonfire namespace reserve -d '8h0m')
    echo "Namespace ${NAMESPACE} reserved"
fi

echo "Using ${NAMESPACE} as default oc project"
oc project "$NAMESPACE"

echo "Deploying apps to ${NAMESPACE}"
bonfire deploy advisor gateway insights-ephemeral host-inventory notifications -n "$NAMESPACE" -t 1200 --source=appsre
