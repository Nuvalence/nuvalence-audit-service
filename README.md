# Nuvalence Audit Service

## Overview 

The Nuvalence Audit Service is responsible for receiving, storing, and retrieving auditable events for the 
various business applications in a domain agnostic way. Specifically the Audit service provides:

1. A REST-ful endpoint for other services to POST auditable events (for specific business objects) 
   to be stored. These records must be stored in an immutable fashion. Clients are not free to 
   delete or modify records.
2. A REST-ful endpoint for other services to retrieve the auditable events related to a specific 
   business object (for use in UIs and internal workflows). As it is anticipated that any given 
   business object will only have a small set of audit records, no filtering is to be provided.
   
The Audit Service may experience significant spikes in load, especially if the dependent services 
encounter spikes in demand, or we introduce bulk operations within those business applications. 
Additionally, the client applications should not have to wait on the auditing process to complete, 
from the client perspective it should be a fire-and-forget process. With this in mind, the API 
should act simply as a facade over a buffer/queue and should respond with a 200 as soon as the 
queue push is successful.

The immutability of these records is vital to the integrity of the overall system. At a minimum 
the REST endpoints should not allow any deletion of records, and the process for storing new 
records should anticipate ID collisions with existing records - there should not be a uniqueness 
constraint based on any identifier provided by a client service. Ideally, the database user 
principal should be authorized with just enough permissions to insert and retrieve records but 
not to edit or remove them.

The Audit Service is a platform component. It should be very opinionated about a small set of 
required data fields (the header) and unopinionated about the majority of the record (the body). 
The implication of this is that the service should be able to manage

### Further Documentation

 - [architecture diagrams](./docs/architecture/README.md)
 - [tools and frameworks](./docs/tools.md)

## Contributing

#### Prerequisites
Make sure you have done the following before you can deploy
1. install: docker, java 11+

#### Checkstyle
1. Install the Checkstyle plugin for IntelliJ
2. Set Checkstyle version to 8.25 in IntelliJ Preferences under Tools/Checkstyle
3. Import Checkstyle config file from `config/checkstyle/checkstyle.xml`

#### Build
```shell
./gradlew build
```

##### Run Locally
1. start the db: `docker-compose -f docker/docker-compose.yml up -d`
2. start the service: `./gradlew bootRun`
3. [view docs](http://localhost:8080/swagger-ui.html)

## Deploy to GCP

#### Deploy a isolated developer sandbox

For validating work you have in progress, you can deploy a functioning service to a gcp project. 

##### Prerequisites
Make sure you have done the following before you can deploy
1. [install gcloud](https://cloud.google.com/sdk/docs/install)
2. sign in to gcp
   ```shell
   gcloud auth application-default login
   ```

##### Deploy
Update the gcp related fields in `./service/src/main/resources/application.yml`, `./cloudbuild.yaml`, and the files in the `./infrastructure` folder.  

Publish your locally built image to gcr & deploy.
```shell
./local-deploy.sh
```

##### Get Auth Token
```shell
gcloud auth print-identity-token --project <project-name>
```

##### Get URL
```shell
gcloud run services describe audit-service --region us-east4 --platform managed --format 'value(status.url)' --project <project-name>
```
