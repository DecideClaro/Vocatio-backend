JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH="$JAVA_HOME/bin:$PATH" mvn package -Dskiptests

docker compose build server
docker tag vocatio:latest r0sewt/vocatio:latest
docker push r0sewt/vocatio:latest