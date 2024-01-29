Link: https://www.elastic.co/guide/en/elasticsearch/reference/7.17/configuring-tls-docker.html
## Run the example

1. Generate the certificates (only needed once):
    - `docker-compose -f create-certs.yml run --rm create_certs`
2. Start two Elasticsearch nodes configured for SSL/TLS:
    - `docker-compose up -d`
3. Access the Elasticsearch API over SSL/TLS using the bootstrapped password:
    - `docker run --rm -v es_certs:/certs --network=es_default docker.elastic.co/elasticsearch/elasticsearch:7.17.17 curl --cacert /certs/ca/ca.crt -u elastic:PleaseChangeMe https://es01:9200`
4. The elasticsearch-setup-passwords tool can also be used to generate random passwords for all users:
    - `docker exec es01 /bin/bash -c "bin/elasticsearch-setup-passwords \
      auto --batch \
      --url https://localhost:9200"`

## Tear everything down
- To remove all the Docker resources created by the example, issue:
  - `docker-compose down -v`