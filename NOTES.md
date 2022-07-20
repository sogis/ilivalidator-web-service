```
docker run --rm -p8080:8080 -e JDBC_URL=jdbc:sqlite:./jobrunr_db.sqlite -e DOC_BASE=/tmp/ -e WORK_DIRECTORY=/tmp/ edigonzales/ilivalidator-web-service
```

```
docker run --rm -p8080:8080 -v /Users/stefan/tmp/foo:/tmp/ -e JDBC_URL=jdbc:sqlite:./jobrunr_db.sqlite -e DOC_BASE=/tmp/ -e WORK_DIRECTORY=/tmp/ edigonzales/ilivalidator-web-service
```

- /tmp/ hat noch anderen Karsumpel drin (e.g. sqlite lib und tomcat-Gedöns)
- Eigene reinkopierte ili und toml bleiben erhalten. Die von der Anwendung werden überschrieben (-> Schalter setzen?)


```
docker run --rm -p8080:8080 -v /Users/stefan/tmp/foo:/tmp/ -e JDBC_URL=jdbc:sqlite:/tmp/jobrunr_db.sqlite -e DOC_BASE=/tmp/ -e WORK_DIRECTORY=/tmp/ edigonzales/ilivalidator-web-service
```

- o.j.storage.sql.common.DatabaseCreator: wird nicht ausgeführt. Scheint i.O. zu sein mit der DB.

Test mit /docbase, /tmp, /work...