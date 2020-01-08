package com.macys.mst.automation.op.steps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.json.JSONException;
import org.json.JSONObject;
import com.mays.mst.automation.op.module.InvoiceObject;
import io.restassured.response.Response;

public class InvoiceSteps {

	public InvoiceObject invoiceObj;
	private Logger logger = Logger.getLogger(InvoiceSteps.class);
	String topicId = "invoicesettlement_onsuccess_QA";
	String invoiceIdResponse = "";
	JSONObject jsonObjRes = null;
	private List<String> InvoicesToCleanup = new ArrayList<>();
	private static final String QUERY_DELETE_INVOICES = "DELETE FROM Invoice WHERE orderId in %s";

	@BeforeStory
	public void beforeStories(@Named("test_id") String testid) {		
		invoiceObj = new InvoiceObject("Invoice", testid);
		invoiceObj.loadData();
		String orderId = invoiceObj.getTestData().get("orderId");
		InvoicesToCleanup.add(orderId);
		String orderIdToDelete = invoiceObj.toSqlInBlock(InvoicesToCleanup);
		String queryDeleteInvoice = String.format(QUERY_DELETE_INVOICES, orderIdToDelete);
		invoiceObj.deleteUsingDml(queryDeleteInvoice);
	}

	@Given("create invoice")
	public void givenCreateInvoice(@Named("test_id") String testid) throws JSONException {
		invoiceObj = new InvoiceObject("Invoice", testid);
		invoiceObj.loadData();
		String template = invoiceObj.getTestData().get("template");
		invoiceObj.createRequest(template);
		invoiceObj.updateRequestTemplateFromExcel();
		int updateInvoiceCount = Integer.parseInt(invoiceObj.getTestData().get("updateInvoiceCount"));
		String updateInvoiceDesc = invoiceObj.getTestData().get("updateInvoiceDesc");
		logger.info("updateInvoiceCount:" + updateInvoiceCount);
		logger.info("updateInvoiceDesc:" + updateInvoiceDesc);
		for (int i = 0; i < updateInvoiceCount; i++) {
			String requestjson = invoiceObj.getRestRequest().getRequestContent();
			System.out.println(requestjson);
			String orderId = invoiceObj.getTestData().get("orderId");
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("clientId", "1234");
			headers.put("correlationId", "1234");
			headers.put("orderId", orderId);
			invoiceObj.getRestRequest().setHeaders(headers);
			String createInvoiceUrl = "https://oms-qa.devops.fds.com/invoice/createinvoice";
			invoiceObj.setEndPoint(createInvoiceUrl);
			invoiceObj.sendRequest("POST");
			logger.info("got response form createinvoice");
			Response response = invoiceObj.getRestRequest().getResponse();
			jsonObjRes = new JSONObject(response.getBody().asString());
			if (i == 0) {
				invoiceIdResponse = jsonObjRes.getJSONArray("invoices").getJSONObject(0).getString("invoiceId");
			} else {
				invoiceIdResponse = invoiceIdResponse + "," + jsonObjRes.getJSONArray("invoices").getJSONObject(0).getString("invoiceId");
			}
		}
		logger.info("invoices created to update:" + invoiceIdResponse);
	}

	@Then("validate using invoice message")
	public void thenValidateUsingInvoiceMessage(@Named("test_id") String testid)
			throws JSONException, InterruptedException {
		invoiceObj.createInvoiceRequest_SpannerValidation(testid);
	}

	@Then("publish the invoice update msg")
	public void thenPublishTheInvoiceUpdateMsg(@Named("test_id") String testid) throws Exception {
		invoiceObj = new InvoiceObject("Invoice", testid);
		invoiceObj.loadData();
		String template = invoiceObj.getTestData().get("template1");
		invoiceObj.createRequest(template);
		if (invoiceIdResponse.contains(",")) {
			String[] invoiceIdResponseList = invoiceIdResponse.split(",");
			for (int i = 1; i <= invoiceIdResponseList.length; i++) {
				String columnKey = "invoiceId" + i;
				String columnValue = invoiceIdResponseList[i - 1];
				invoiceObj.getTestData().put(columnKey, columnValue);
			}
		} else {
			invoiceObj.getTestData().put("invoiceId1", invoiceIdResponse);
		}
		invoiceObj.updateRequestTemplateFromExcel();
		String requestjson = invoiceObj.getRestRequest().getRequestContent();
		invoiceObj.publish_retry_releaseorder(topicId, requestjson, 2, invoiceObj.getTestData().get("orderId"));
		logger.info("successfully published to topic:" + requestjson);
	}

	@Then("validate the invoice update msg with spannerDB")
	public void thenValidateTheInvoiceUpdateMsgWithSpannerDB(@Named("test_id") String testid) throws JSONException {
		String requestjson = invoiceObj.getRestRequest().getRequestContent();
		String orderId = invoiceObj.getTestData().get("orderId");
		invoiceObj.updateInvoiceRequest_SpannerValidation(testid, requestjson, orderId);
	}

}