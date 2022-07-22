# REST-API

```
curl -i -X POST -F file=@254900.itf http://localhost:8080/rest/jobs

curl -i -X POST -F file=@2408.xtf -F allObjectsAccessible=false -F configFile=off http://localhost:8080/rest/jobs

curl -i -X POST -F file=@2408.xtf http://localhost:8080/rest/jobs
```

- optionen (allobjects, configfile)

- swagger / open-api url
- https://dunnhq.com/posts/2021/long-running-rest-requests/
- https://github.com/microsoft/api-guidelines/blob/vNext/Guidelines.md#1327-the-typical-flow-polling