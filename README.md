# Keptn NeoLoad Service

This service is designed to use NeoLoad for executing various Load testing tasks. 

To trigger a NeoLoad test, the service has subscriptions to event channels. In more details, the current implementation of the service listens to CloudEvents from type:
* `sh.keptn.events.deployment-finished`: When receiving this event, the service executes a test for a deployed application. This event would be replace by the start test event

## Secret for credentials
During the setup of NeoLaod, a secret is created that contains key-value pairs for the NeoLoad  URL, NeoLoad apiKey:
   * NL_WEB_HOST 
   * NL_API_HOST 
   * NL_UPLOAD_HOST
   * NL_API_TOKEN
   * NL_WEB_ZONEID 




## Install service <a id="install"></a>

1. To install the service, you need to run :
 * installer/defineNeoLoadWebCredentials.sh to configure the required parameters :
    1. NL_WEB_HOST : host of the web ui of NeoLoad web
    1. NL_API_HOST : host of the api of NeoLoad web
    1. NL_UPLOAD_HOST : host of upload api of NeoLoad Web
    1. NL_API_TOKEN: api token of your NeoLoad account
    1. NL_WEB_ZONEID : NeoLoad Web Zone id that would be used by Keptn


## The NeoLoad Service requires to store the keptn.neoload.engine.yaml file in the ressources of keptn

1. Create your keptn.neoload.engine.yaml file describing the test and the infrastructure

```yaml
steps:
- step:
    repository: https://yourreposistory/project.git
    project:
    - path: /tests/neoload/load_template/load_template.nlp
    - path: /tests/neoload/catalogue_neoload.yaml
    description: BasicCheck
    scenario: BasicCheck
    constant_variables:
    - name: server_host
      value: catalog-service.orders-project-dev.svc
    - name: server_port
      value: 8080
    infrastructure:
      local_LG:
      - name: lg1
      populations:
      - name: BasicCheck
        lgs:
        - name: lg1
 ```
   Here is a template of a [keptn.neoload.engine.yaml](/template/keptn.neoload.engine.yaml) file
   The ```repository``` needs to have the url of your source control repo containing your NeoLoad tests.
   The property ```project``` will have the list of the relative path of your neoload project files.
   A NeoLoad project can be combined of a NeoLoad gui project ( nlp) , yaml files, or both.
   
   ```constant_variables``` is the object allowing you to replace the value of constant variable defined in your project.
   
   ```infrastructure``` is the object describing the Load testing infrastructure required for this test.
  
1. Once your keptn.neoload.engine.yaml file created , you will need to store in keptn by sending the following command :

   ```keptn add-resource --project=your-project --service=my-service --stage=your stage --resource=keptn.neoload.engine.yaml```
   
    [here](https://keptn.sh/docs/0.6.0/installation/setup-keptn/) is the log to install the keptn_cli 
