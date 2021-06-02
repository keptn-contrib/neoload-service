package com.keptn.neotys.testexecutor.cloudevent;

import com.keptn.neotys.testexecutor.KeptnEvents.KeptnEventFinished;
import com.keptn.neotys.testexecutor.log.KeptnLogger;
import com.keptn.neotys.testexecutor.messageHandler.NeoLoadHandler;
import io.cloudevents.http.reactivex.vertx.VertxCloudEvents;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.healthchecks.HealthCheckHandler;
import io.vertx.reactivex.ext.web.Router;


import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.keptn.neotys.testexecutor.KeptnEvents.EventType.KEPTN_TEST_STARTING;
import static com.keptn.neotys.testexecutor.conf.NeoLoadConfiguration.*;
import static java.lang.System.getenv;

public class CloudEventNeoload extends AbstractVerticle {

	KeptnLogger loger;
	private  Vertx rxvertx;
    private List<String> lisofKeptnContext=new ArrayList<>();
	public void start() {
		rxvertx= Vertx.newInstance(this.getVertx());
		loger=new KeptnLogger(this.getClass().getName());
		if(rxvertx ==null)
			System.out.println("Issues during init");

		rxvertx.createHttpServer()
				.requestHandler(req ->
				{
					if(req.path().equalsIgnoreCase(HEALTH_PATH)) {
						req.response().end("Status:OK");
						return;
					}
					System.out.print(req.toString());
					VertxCloudEvents.create().rxReadFromRequest(req,new Class[]{KeptnExtensions.class})
							.subscribe((receivedEvent, throwable) -> {
								if(throwable!=null)
								{
									throwable.printStackTrace();
									req.response().setStatusCode(400).end(throwable.getMessage());
									return;
								}
								if (receivedEvent != null) {
									// I got a CloudEvent object:
									System.out.println("The event type: " + receivedEvent.getType());
									System.out.println(req.headers().entries().stream().map(stringStringEntry -> stringStringEntry.getKey()).collect(Collectors.joining(",")));

									if(receivedEvent.getType().equalsIgnoreCase(KEPTN_TEST_STARTING))
									{
										System.out.println("The event received: " + receivedEvent.toString());
										if(receivedEvent.getData().isPresent())
										{
											Object obj=receivedEvent.getData().get();
											try {
												JsonObject data = new JsonObject(obj.toString());
												if (data instanceof JsonObject)
												{
													KeptnEventFinished eventFinished = new KeptnEventFinished(data);
													KeptnExtensions keptnExtensions = null;
													if (receivedEvent.getExtensions().isPresent() && receivedEvent.getExtensions().get().size() > 0) {
														System.out.println("Extracting extensions from event");
														keptnExtensions = (KeptnExtensions) receivedEvent.getExtensions().get().get(0);

													}
													else
													{
														System.out.println("Collecting keptn extension from headers");
														Optional<String> kepncontext = Optional.ofNullable(req.getHeader(HEADER_KEPTNCONTEXT));
														Optional<String> datacontent = Optional.ofNullable(req.getHeader(HEADER_datacontentype));
														Optional<String> shkeptnspecversion=Optional.ofNullable(req.getHeader(HEADER_shkeptnspecversion));
														Optional<String> triggeredid=Optional.ofNullable(req.getHeader(HEADER_triggeredid));
														if(kepncontext.isPresent()&& datacontent.isPresent() && shkeptnspecversion.isPresent() && triggeredid.isPresent())
															keptnExtensions=new KeptnExtensions(kepncontext.get(),datacontent,triggeredid,shkeptnspecversion);
													}

													if(keptnExtensions!=null)
													{
														String keptncontext;
														keptncontext= keptnExtensions.getShkeptncontext();
														if(keptncontext ==null)
														{

															keptncontext = Optional.ofNullable(req.getHeader(HEADER_KEPTNCONTEXT)).get();
															System.out.println("Ketptn contecxt : "+keptncontext);
															keptncontext=keptncontext.replaceAll("\"","");
															Optional<String> datacontent = Optional.ofNullable(req.getHeader(HEADER_datacontentype));
															Optional<String> shkeptnspecversion=Optional.ofNullable(req.getHeader(HEADER_shkeptnspecversion));
															Optional<String> triggeredid=Optional.ofNullable(req.getHeader(HEADER_triggeredid));
															keptnExtensions=new KeptnExtensions(keptncontext,datacontent,triggeredid,shkeptnspecversion);

														}
														loger.setKepncontext(keptncontext);
														loger.debug("Received data " + eventFinished.toString());
														if(lisofKeptnContext.contains(keptncontext))
														{
															///-----already received this event-------------
															req.response().setStatusCode(210).putHeader("content-type", "text/plain").end("event already received- currently processed");
															//----------------------------------------------
														}
														else
														{
															//----new event received----
															lisofKeptnContext.add(keptncontext);
															KeptnExtensions finalKeptnExtensions = keptnExtensions;
															req.response().setStatusCode(200).putHeader("content-type", "text/plain").end("event received");

															String finalKeptncontext = keptncontext;
															vertx.<String>executeBlocking(
																	future -> {
																		String result;
																		try {
																			NeoLoadHandler neoLoadHandler = new NeoLoadHandler(eventFinished, finalKeptnExtensions, receivedEvent.getId());


																			neoLoadHandler.runNeoLoadTest(rxvertx, receivedEvent);
																			result = "test has finished";
																			lisofKeptnContext.remove(finalKeptncontext);
																			future.complete(result);
																		} catch (Exception e) {
																			result = "Exception :" + e.getMessage();
																			future.fail(result);
																		}
																	}, res ->
																	{
																		if (res.succeeded()) {

																			//req.response().setStatusCode(200).putHeader("content-type", "text/plain").end(res.result());

																		} else {
																			res.cause().printStackTrace();
																		}
																	}

															);
														}


													}
													else
													{
														System.out.println("Found no extension in the even");
														req.response().setStatusCode(401).end("Unable to find Extensions in CLoud evnet");
														return;
													}
												}
												else
												{
													System.out.println("The Data is not Json Object");
												}
											}
											catch (Exception e)
											{
												e.printStackTrace();
												req.response().setStatusCode(410).end("Exception :"+e.getMessage());
											}
										}
										else
										{
											System.out.println("No data found in the event");
										}


									}
									else{
										req.response().setStatusCode(203).end("Not Supported event type");
									}

								}
								else
								{
									req.response().setStatusCode(400).end("UNsupported cloud event format");
								}

							});
				})
				.rxListen(KEPTN_PORT)
				.subscribe(server -> {
					System.out.println("Server running!");});

	}


}