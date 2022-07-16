# REST-API

```
        def response = ["curl", "-u", aiLogin, "-F", "topic=fruchtfolgeflaechen", "-F",
                        "lv95_file=@" + zipFilePath, "-F", "publish=true", serverUrl
                        ].execute().text
```

```
curl -i -X POST -F file=@254900.itf http://localhost:8080/rest/upload
```