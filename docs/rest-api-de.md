# REST-API

```
curl -i -X POST -F file=@254900.itf http://localhost:8080/rest/jobs

curl -i -X POST -F file=@2408.xtf -F allObjectsAccessible=false -F configFile=off http://localhost:8080/rest/jobs

curl -i -X POST -F file=@2408.xtf http://localhost:8080/rest/jobs
```
