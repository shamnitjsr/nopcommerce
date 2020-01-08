package com.macys.mst.automation.op.steps;

import org.jbehave.core.annotations.*;
import static org.junit.Assert.assertEquals;

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
import com.macys.mst.automation.op.module.MessageStoreObject;

import io.restassured.response.Response;




public class MessageStoreSteps {

	
	SpannerDB spannerdb = new SpannerDB();
	final String ENDPOINT_NAME = "messageStore.url";
	String baseUrl = FileConfig.getInstance().getStringConfigValue(ENDPOINT_NAME);
	Map<String, String> headers = new HashMap<String, String>();
	public static String SubscriptionId;
	public JSONObject messagePostResponse;
	static String query_delete_returns = "DELETE FROM ReturnOrder WHERE returnOrderId in %s";
	private long threadId = Thread.currentThread().getId();
	private List<String> returnOrderIdsToCleanup = new ArrayList<>();
	private Logger logger = Logger.getLogger(MessageStoreSteps.class);
	private MessageStoreObject messageStoreObject;
	
	@BeforeStory
	public void beforeStories() {
		System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");
		ConcurrentHashMap<String, String> obj = TestNGListener.EnvMap.get(threadId);
		TestNGListener.EnvMap.put(Thread.currentThread().getId(), obj);
	}

	
	
	@Given("the message to be posted is formed")
	public void givenTheMessageToBePostedIsFormed(@Named("test_id") String testid) throws Exception {
		 
		
		// forming the request
		
		messageStoreObject = new MessageStoreObject("MessageStore", testid);
		messageStoreObject.createMessageStoreRequest(testid);
		
	
		
	}
	@Then(" the message is stored in the messagestore table")
	public void thenTheMessageIsStoredInTheMessagestoreTable(){
		 //TODO 
	}
	@When("all the mandatory headers are present and unique")
	public void whenAllTheMandatoryHeadersArePresentAndUnique(@Named("test_id") String testid) throws JSONException{
		messageStoreObject.validateResponseMessage( testid );
	}
}