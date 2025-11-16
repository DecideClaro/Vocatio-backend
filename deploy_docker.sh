JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH="$JAVA_HOME/bin:$PATH" mvn package -Dskiptests

docker build -t r0sewt/vocatio:latest .
docker push rody/vocatio:latest