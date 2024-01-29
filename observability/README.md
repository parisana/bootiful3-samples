#### Observability is the ability to observe the internal state of a running system from the outside. It consists of the three pillars logging, metrics and traces.



### Tracing with OpenZipkin Brave and reporting to Zipkin. 
### Metrics reporting and monitoring with Prometheus and grafana 


- Zipkin UI: 
    - http://localhost:9411/zipkin/
- testController endpoint: 
    - http://localhost:8080/fact
- apachePerformance monitoring using javamelody:
    - http://localhost:8080/monitoring

- private
  - http://localhost:8080/ffmpeg/test
  - .~/opt/node_exporter-1.7.0.linux-amd64/node_exporter

## Execution:
- $ `.~/opt/node_exporter-1.7.0.linux-amd64/node_exporter`
- cd to resources/dockerFiles
  - $ `docker compose up`
  - prometheus-endpoint: localhost:9090
  - metrics-springApp: localhost:8080
  - metrics-linux-node_exporter: localhost:9100
  - grafana: localhost:3000
  - zipkin: localhost:9411

### Resync git repo with new .gitignore file
1. rm all files
   - git rm -r --cached .
2. add all files as per new .gitignore
   - git add .
3. now, commit for new .gitignore to apply
   - git commit -m ".gitignore is now working"