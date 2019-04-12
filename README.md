# SimpleDownloader

#Bootstrap Steps
mkdir /tmp/downloaded/
SPRING_APPLICATION_JSON='{"java":{"io":{"tmpdir":"/${HOME}/workspace/IDM/logs"}}}'  mvn package && java -jar target/IDM-0.0.1-SNAPSHOT.jar
