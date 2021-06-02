package com.keptn.neotys.testexecutor.EventSender;


import com.keptn.neotys.testexecutor.KeptnEvents.CloudTestEvent;
import com.keptn.neotys.testexecutor.KeptnEvents.KeptnEventFinished;
import com.keptn.neotys.testexecutor.cloudevent.KeptnExtensions;
import com.keptn.neotys.testexecutor.log.KeptnLogger;
import io.cloudevents.CloudEvent;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.core.Vertx;

import io.vertx.reactivex.core.http.HttpClientRequest;

import java.net.URI;

import static com.keptn.neotys.testexecutor.KeptnEvents.EventType.*;
import static com.keptn.neotys.testexecutor.conf.NeoLoadConfiguration.*;


public class NeoLoadEvent {

    String eventid;
    KeptnLogger logger;
    Vertx vertx;
    String keptnApiToken;

    private final static String CONTENTYPE="application/json";
    private final static String CONTENTYPE_CLOUD=" application/cloudevents+json";
    public NeoLoadEvent(KeptnLogger log, String enventid, Vertx rxvertx) {
        this.eventid=enventid;
        logger=log;
        vertx=rxvertx;
        keptnApiToken=System.getenv(SECRET_KEPTN_API_TOKEN);
    }

    //{
    //  "type": "sh.keptn.events.deployment-finished",
    //  "specversion": "0.2",
    //  "source": "https://github.com/keptn/keptn/helm-service",
    //  "id": "f2b878d3-03c0-4e8f-bc3f-454bc1b3d79d",
    //  "time": "2019-06-07T07:02:15.64489Z",
    //  "contenttype": "application/json",
    //  "shkeptncontext": "08735340-6f9e-4b32-97ff-3b6c292bc509",
    //  "data": {
    //    "project": "sockshop",
    //    "stage": "staging",
    //    "service": "carts",
    //    "testStrategy": "performance",
    //    "deploymentStrategy": "direct",
    //    "tag": "0.9.1",
    //    "image": "docker.io/keptnexamples/carts"
    //  }
    //}


    public void endevent(KeptnEventFinished data, KeptnExtensions extensions, CloudEvent<Object> receivedEvent, String keptn_namespace)
    {
        try {
            logger.debug("endevent : Start sending event");
            final HttpClientRequest request = vertx.createHttpClient().post(KEPTN_PORT_EVENT, KEPTN_EVENT_HOST+keptn_namespace+KEPTN_END_URL, "/"+KEPTN_EVENT_URL);

            logger.debug("endevent : Defining cloud envet with data:" + data.toJsonEndObject().toString());

            logger.debug("endevnet specversion : "+receivedEvent.getSpecVersion()+" : source : "+URI.create(NEOLOAD_SOURCE).toString()+ " id :"+this.eventid);
            String id;
            if(receivedEvent.getId()==null)
                id=extensions.getShkeptncontext();
            else
                id=receivedEvent.getId();

            WebClient client=WebClient.create(vertx);

            HttpRequest<Buffer> httpRequest=client.post(CONFIGURAITON_PORT, KEPTN_EVENT_HOST+keptn_namespace+KEPTN_END_URL, "/"+CONFIGURATION_VERSION+"/"+KEPTN_EVENT_URL);

            httpRequest.putHeader(CONTENT_TYPE,CONTENTYPE_CLOUD);
            httpRequest.putHeader(HEADER_KEPTN_TOKEN,keptnApiToken);


            CloudTestEvent cloudTestEvent =new CloudTestEvent(KEPTN_TEST_FINISHED,CONTENTYPE,extensions.getShkeptncontext(),receivedEvent.getSpecVersion(),NEOLOAD_SOURCE,id,extensions.getTriggeredid(),extensions.getShkeptnspecversion(),data.toJsonEndObject());
            httpRequest.sendJson(cloudTestEvent.toJson(), httpResponseAsyncResult -> {
                if(httpResponseAsyncResult.succeeded())
                {
                    logger.info("endevent : received response code "+String.valueOf(httpResponseAsyncResult.result().statusCode())+ " message "+ httpResponseAsyncResult.result().statusMessage());
                }
                else
                {
                    logger.error("ERROR endevent : received response code "+String.valueOf(httpResponseAsyncResult.result().statusCode())+ " message "+ httpResponseAsyncResult.result().statusMessage());
                }
            });
            logger.info("Request sent " + cloudTestEvent.toJson().toString() );



        }
        catch(Exception e)
        {
            logger.error("end event generate exception",e);
            if(extensions.getShkeptncontext()==null)
                logger.debug("keptn context null");

            if(receivedEvent.getSpecVersion()==null)
                logger.debug("Specversion null");

            if(data.toStartJsonObject() ==null)
                logger.debug("data null");

        }
    }
    public void changeevent(KeptnEventFinished data, KeptnExtensions extensions, CloudEvent<Object> receivedEvent, String keptn_namespace)
    {
        try {
            logger.debug("changeevent : Start sending event");
            final HttpClientRequest request = vertx.createHttpClient().post(KEPTN_PORT_EVENT, KEPTN_EVENT_HOST+keptn_namespace+KEPTN_END_URL, "/"+KEPTN_EVENT_URL);

            logger.debug("changeevent : Defining cloud envet with data:" + data.toStartJsonObject().toString());

            logger.debug("changeevent specversion : "+receivedEvent.getSpecVersion()+" : source : "+URI.create(NEOLOAD_SOURCE).toString()+ " id :"+this.eventid);
            String id;
            if(receivedEvent.getId()==null)
                id=extensions.getShkeptncontext();
            else
                id=receivedEvent.getId();

            WebClient client=WebClient.create(vertx);

            HttpRequest<Buffer> httpRequest=client.post(CONFIGURAITON_PORT, KEPTN_EVENT_HOST+keptn_namespace+KEPTN_END_URL, "/"+CONFIGURATION_VERSION+"/"+KEPTN_EVENT_URL);

            httpRequest.putHeader(CONTENT_TYPE,CONTENTYPE_CLOUD);
            httpRequest.putHeader(HEADER_KEPTN_TOKEN,keptnApiToken);


            CloudTestEvent cloudTestEvent =new CloudTestEvent(KEPTN_TEST_STARTING,CONTENTYPE,extensions.getShkeptncontext(),receivedEvent.getSpecVersion(),NEOLOAD_SOURCE,id,extensions.getTriggeredid(),extensions.getShkeptnspecversion(),data.toJsonEndObject());
            httpRequest.sendJson(cloudTestEvent.toJson(), httpResponseAsyncResult -> {
                if(httpResponseAsyncResult.succeeded())
                {
                    logger.info("changeevent : received response code "+String.valueOf(httpResponseAsyncResult.result().statusCode())+ " message "+ httpResponseAsyncResult.result().statusMessage());
                }
                else
                {
                    logger.error("ERROR changeevent : received response code "+String.valueOf(httpResponseAsyncResult.result().statusCode())+ " message "+ httpResponseAsyncResult.result().statusMessage());
                }
            });
            logger.info("Request sent " + cloudTestEvent.toJson().toString() );



        }
        catch(Exception e)
        {
            logger.error("changeevent  generate exception",e);
            if(extensions.getShkeptncontext()==null)
                logger.debug("keptn context null");

            if(receivedEvent.getSpecVersion()==null)
                logger.debug("Specversion null");

            if(data.toStartJsonObject() ==null)
                logger.debug("data null");

        }
    }

    //{
    //  "data": {
    //    "project": "sockshop",
    //    "stage": "dev",
    //    "service": "carts",
    //    "labels": {
    //      "label-key": "label-value"
    //    },
    //    "status": "succeeded",
    //    "result": "pass",
    //    "message": "a message"
    //  },
    //  "datacontenttype": "application/json",
    //  "id": "c4d3a334-6cb9-4e8c-a372-7e0b45942f53",
    //  "shkeptncontext": "a3e5f16d-8888-4720-82c7-6995062905c1",
    //  "source": "source-service",
    //  "specversion": "1.0",
    //  "triggeredid": "3f9640b6-1d2a-4f11-95f5-23259f1d82d6",
    //  "type": "sh.keptn.event.deployment.started"
    //}
    public void sendTestStarted(KeptnEventFinished keptnEventFinished, KeptnExtensions extensions,CloudEvent<Object> receivedEvent, String keptn_namespace) {
        try {
            logger.debug("started : Start sending event");
            final HttpClientRequest request = vertx.createHttpClient().post(KEPTN_PORT_EVENT, KEPTN_EVENT_HOST+keptn_namespace+KEPTN_END_URL, "/"+KEPTN_EVENT_URL);

            logger.debug("started : Defining cloud envet with data:" + keptnEventFinished.toStartJsonObject().toString());

            logger.debug("started specversion : "+receivedEvent.getSpecVersion()+" : source : "+URI.create(NEOLOAD_SOURCE).toString()+ " id :"+this.eventid);
            String id;
            if(receivedEvent.getId()==null)
                id=extensions.getShkeptncontext();
            else
                id=receivedEvent.getId();

            WebClient client=WebClient.create(vertx);

            HttpRequest<Buffer> httpRequest=client.post(CONFIGURAITON_PORT, KEPTN_EVENT_HOST+keptn_namespace+KEPTN_END_URL, "/"+CONFIGURATION_VERSION+"/"+KEPTN_EVENT_URL);

            httpRequest.putHeader(CONTENT_TYPE,CONTENTYPE_CLOUD);
            httpRequest.putHeader(HEADER_KEPTN_TOKEN,keptnApiToken);

            String contentype;

            CloudTestEvent cloudTestEvent =new CloudTestEvent(KEPTN_TEST_STARTED,CONTENTYPE,extensions.getShkeptncontext(),receivedEvent.getSpecVersion(),NEOLOAD_SOURCE,id,extensions.getTriggeredid(),extensions.getShkeptnspecversion(),keptnEventFinished.toStartJsonObject());
            logger.debug("Sending cloud event to Keptn with payload "+ cloudTestEvent.toJson().toString());
            httpRequest.sendJson(cloudTestEvent.toJson(), httpResponseAsyncResult -> {
                if(httpResponseAsyncResult.succeeded())
                {
                    logger.info("endevent : received response code "+String.valueOf(httpResponseAsyncResult.result().statusCode())+ " message "+ httpResponseAsyncResult.result().statusMessage());
                }
                else
                {
                    logger.error("ERROR endevent : received response code "+String.valueOf(httpResponseAsyncResult.result().statusCode())+ " message "+ httpResponseAsyncResult.result().statusMessage());
                }
            });
            logger.info("Request sent " + cloudTestEvent.toJson().toString() );



        }
        catch(Exception e)
        {
            logger.error("end event generate exception",e);
            if(extensions.getShkeptncontext()==null)
                logger.debug("keptn context null");

            if(receivedEvent.getSpecVersion()==null)
                logger.debug("Specversion null");

            if(keptnEventFinished.toStartJsonObject() ==null)
                logger.debug("data null");

        }

    }



}