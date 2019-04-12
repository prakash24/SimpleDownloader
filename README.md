## SimpleDownloader
### Bootstrap Steps

mkdir /tmp/downloaded/ && SPRING_APPLICATION_JSON='{"java":{"io":{"tmpdir":"/${HOME}/workspace/IDM/logs"}}}'  mvn package && java -jar target/IDM-0.0.1-SNAPSHOT.jar


##### PM tests
1. Test URL: http://localhost:8080/download?url=https://speed.hetzner.de/100MB.bin&threadCount=16
   * ```tests["Status code is 200"] = responseCode.code === 200;```
   * ```tests["Body is correct"] = responseBody === "File downloaded. Find on disk!!";```
   
2. Test URL: http://localhost:8080/download?=https://speed.hetzner.de/100MB.bin&threadCount=16
   * ```tests["Status code is 400"] = responseCode.code === 400;```
   * ```tests["Body is correct"] = responseBody === "Something Went Wong!! Please check the URL or contact app admin";```




