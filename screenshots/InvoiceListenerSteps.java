package com.macys.mst.automation.op.steps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.json.JSONException;
import org.json.JSONObject;
import com.mays.mst.automation.op.module.InvoiceObject;

public class InvoiceListenerSteps {

	public InvoiceObject invoiceObj;
	private Logger logger = Logger.getLogger(InvoiceListenerSteps.class);
	String topicId = "shipment_confirmation_onsuccess_QA";
	String invoiceIdResponse = "";
	JSONObject jsonObjRes = null;
	private List<String> InvoicesToCleanup = new ArrayList<>();
	private static final String QUERY_DELETE_INVOICES = "DELETE FROM Invoice WHERE orderId in %s";

	@BeforeStory
	public void beforeStories(@Named("test_id") String testid) {		
		invoiceObj = new InvoiceObject("InvoiceListener", testid);
		invoiceObj.loadData();
		String orderId = invoiceObj.getTestData().get("orderId");
		InvoicesToCleanup.add(orderId);
		String orderIdToDelete = invoiceObj.toSqlInBlock(InvoicesToCleanup);
		String queryDeleteInvoice = String.format(QUERY_DELETE_INVOICES, orderIdToDelete);
		invoiceObj.deleteUsingDml(queryDeleteInvoice);
	}

	@Given("publish the shipment confimration onsucess msg")
	public void givenPublishTheShipmentConfimrationOnsucessMsg(@Named("test_id") String testid) throws Exception {
		invoiceObj = new InvoiceObject("InvoiceListener", testid);
		invoiceObj.loadData();
		String template = invoiceObj.getTestData().get("template");
		invoiceObj.createRequest(template);
		invoiceObj.updateRequestTemplateFromExcel();		
		HashMap<String, String> headersMap = new HashMap<>();
		headersMap.put("orderId", invoiceObj.getTestData().get("orderId"));
		headersMap.put("correlationId", "12345");
		String requestjson = invoiceObj.getRestRequest().getRequestContent();
		invoiceObj.publish_retry_releaseorder(topicId, requestjson, 2, invoiceObj.getTestData().get("orderId"));
		logger.info("successfully published to topic:" + requestjson);
	}

	@Then("validate the shipment confimration onsucess msg with spannerDB")
	public void thenValidateTheShipmentConfimrationOnsucessMsgWithSpannerDB(@Named("test_id") String testid) throws InterruptedException, JSONException {
		String requestjson = invoiceObj.getRestRequest().getRequestContent();
		String cartonId = invoiceObj.getTestData().get("cartonId");
		String orderId = invoiceObj.getTestData().get("orderId");
		invoiceObj.invoiceListner_SpannerDbValidation(testid, orderId, cartonId, requestjson);
	}

}