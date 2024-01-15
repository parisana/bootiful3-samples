### Tracing with OpenZipkin Brave and reporting to Zipkin


Zipkin UI: 
    - http://localhost:9411/zipkin/
testController endpoint: 
    - http://localhost:8080/fact

### Resync git repo with new .gitignore file
1. rm all files
   - git rm -r --cached .
2. add all files as per new .gitignore
   - git add .
3. now, commit for new .gitignore to apply
   - git commit -m ".gitignore is now working"