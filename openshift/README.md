# ilivalidator-web-service

## First install in an Openshift Environment

All necassary components of the application are configured in the template ilivalidator-web-service.yaml
oc process -f ilivalidator-web-service.yaml | oc create -f-

# Update of app configuration in Openshift Environment

Make changes to the configuration in the template ilivalidator-web-service.yaml and run
oc process -f ilivalidator-web-service.yaml | oc apply -f-
