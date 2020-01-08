package com.macys.mst.automation.op.steps;

import org.jbehave.core.annotations.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.json.JSONException;
import org.json.JSONObject;
import com.macys.mst.artemis.config.FileConfig;
import com.macys.mst.artemis.testNg.TestNGListener;
import com.macys.mst.automation.gcloud.SpannerDB;
import com.macys.mst.automation.op.module.OrderEnrichmentObject;
import com.macys.mst.automation.rest.RESTRequest;

import io.restassured.RestAssured;
import io.restassured.response.Response;




public class OrderEnrichmentSteps {

	
	SpannerDB spannerdb = new SpannerDB();
	final String ENDPOINT_NAME = "orderEnrichment.url";
	String baseUrl = FileConfig.getInstance().getStringConfigValue(ENDPOINT_NAME);
	Map<String, String> headers = new HashMap<String, String>();
	public static String SubscriptionId;
	public JSONObject orderEnrichmentPostResponse;
	static String query_delete_returns = "DELETE FROM ReturnOrder WHERE returnOrderId in %s";
	private long threadId = Thread.currentThread().getId();
	//private List<String> returnOrderIdsToCleanup = new ArrayList<>();
	private Logger logger = Logger.getLogger(OrderEnrichmentSteps.class);
	private OrderEnrichmentObject orderEnrichmentObject;
	JSONObject restRequest;
	String orderId;
	String requestjson;
	//protected RESTRequest restRequest; 
	
	@BeforeStory
	public void beforeStories() {
		System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");
		ConcurrentHashMap<String, String> obj = TestNGListener.EnvMap.get(threadId);
		TestNGListener.EnvMap.put(Thread.currentThread().getId(), obj);
	}

	
	
	@Given("order enrichment request is created")
	public void givenOrderEnrichmentRequestIsCreated(@Named("test_id") String testid) throws Exception {
		orderEnrichmentObject = new OrderEnrichmentObject("OrderEnrichment", testid);
		orderEnrichmentObject.loadData();
		String template = orderEnrichmentObject.getTestData().get("OrderEnrichmentTemplate");
		orderEnrichmentObject.createRequest(template);
		orderEnrichmentObject.updateRequestTemplateFromExcel();
		
		 orderId  = orderEnrichmentObject.getTestData().get("OrderId");
		String requestjson = orderEnrichmentObject.getRestRequest().getRequestContent();
		 restRequest = new JSONObject(requestjson);
		
		// forming the request
		
		}
	@When("Rest call is made to order enrichment")
	public void whenRestcallismadetoorderenrichment(){
		
		//String template = orderEnrichmentObject.getStringValueForKey("OrderEnrichmentTemplate");
		String ContentType = orderEnrichmentObject.getStringValueForKey("Content-Type");
		String CORRELATIONID = orderEnrichmentObject.getStringValueForKey("CORRELATIONID");
		String ORDERID = orderEnrichmentObject.getStringValueForKey("ORDERID");
		String sourceService = orderEnrichmentObject.getStringValueForKey("sourceService");
		String lineId = orderEnrichmentObject.getStringValueForKey("lineId");
		
		requestjson = orderEnrichmentObject.getRestRequest().getRequestContent();
		logger.info("OrderEnrichment json request" + requestjson);
		String baseUrl="https://oms-dev.devops.fds.com/orderenrichment/order/";
		orderEnrichmentObject.setEndPoint(baseUrl);
		headers.put("Content-Type", ContentType);
		headers.put("CORRELATIONID", CORRELATIONID);
		headers.put("ORDERID", ORDERID);
		headers.put("sourceService", sourceService);
		headers.put("lineId", lineId);

		orderEnrichmentObject.getRestRequest().setHeaders(headers);
		//RestAssured.useRelaxedHTTPSValidation();
		orderEnrichmentObject.sendRequest("POST");

		Response response = orderEnrichmentObject.getRestRequest().getResponse();
		String sampleresonse = response.getBody().asString();
		System.out.println(sampleresonse);
	}
	
	/*
	@Then("validate the json response with status code $statusCode")
	public void thenValidateResponseStatusCode(@Named("test_id") String testid,
			@Named("statusCode") String statusCode) {
		orderEnrichmentObject.validateResponseStatusCode(statusCode);
	}
	*/
	
	@Then("orderline is updated with product additional attribute details")
	public void thenOrderlineIsUpdatedWithProductAdditionalAttributeDetails(@Named("test_id") String testid) throws JSONException, SQLException, InterruptedException, IOException{
		//orderEnrichmentObject.validateResponseMessage( testid );
		
		orderEnrichmentObject.validateOrderEnrichmentAttribute();
		orderEnrichmentObject.validateOrderEnrichmentResponse_statusmessage();
		
	}
	
	
}