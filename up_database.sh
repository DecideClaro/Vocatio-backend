# sudo chmod +x up_database.sh
# ./up_database.sh

sudo docker build -t vocatio .
sudo docker run --rm -p 8080:8080 \
  --add-host=host.docker.internal:host-gateway \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5434/vocatio_db?sslmode=disable" \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e JWT_SECRET="dev-secret" \
  vocatio
  -e PORT=8080 \

# Trouble shooting

# host.docker.internal remplaza por la ip del host (si no tienes docker desktop)