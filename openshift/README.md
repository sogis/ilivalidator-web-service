# ilivalidator-web-service

## First install in an Openshift Environment

All necessary components of the application are configured in the template ilivalidator-web-service.yaml
Set environment and desired version of the image
```
oc process -p env=test -p version=latest -f ilivalidator-web-service.yaml  | oc apply -f-
```

## Update of app configuration in Openshift Environment

Make changes to the configuration in the template ilivalidator-web-service.yaml and run
Set environment and desirde version of the image
```
oc process -p env=test -p version=latest -f ilivalidator-web-service.yaml  | oc apply -f-
```
