package com.mays.mst.automation.op.module;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jbehave.core.annotations.Named;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.ServiceOptions;
import com.google.cloud.Timestamp;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner.TransactionCallable;
import com.google.protobuf.ByteString;
import org.threeten.bp.Duration;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.macys.mst.automation.gcloud.SpannerDB;
import com.macys.mst.automation.rest.RESTCoreObject;
import io.restassured.response.Response;

public class InvoiceObject extends RESTCoreObject {

	SpannerDB spannerdb = new SpannerDB();
	String instanceId = "orderplatform";
	String databaseId = "order_db_qa";
	private static Logger logger = Logger.getLogger(InvoiceObject.class);
	private static final String PROJECT_ID = ServiceOptions.getDefaultProjectId();
	private static HashMap<String, Publisher> publisherMap = new HashMap<String, Publisher>();
	String getorderdetailsURL = "https://oms-qa.devops.fds.com/orderdetails/order/";

	public InvoiceObject(String name, String testId) {
		super(name, testId);
	}

	// createinvoice RequestBody to spanner validation
	public void createInvoiceRequest_SpannerValidation(@Named("test_id") String testid) throws JSONException {
		String requestjson = this.getRestRequest().getRequestContent();
		JSONObject restRequest = new JSONObject(requestjson);
		// String cartonId = this.getTestData().get("cartonId");
		String orderId = this.getTestData().get("orderId");
		String query_findInvoice = "SELECT * FROM Invoice WHERE orderId ='" + orderId + "' and invoiceId=(select MAX(invoiceId) from Invoice where orderId='" + orderId + "')";
		logger.info("Query to validate Insrtion:" + query_findInvoice);
		String query_findStatus = "SELECT * FROM InvoiceLine where invoicePk =''";
		try {
			ResultSet resultSet = spannerdb.connectDBspannerGetResultSet(instanceId, databaseId, query_findInvoice);
			if (resultSet != null) {
				while (resultSet.next()) {
					// invoice Table validation
					System.out.println("From resultSet: " + resultSet.getString("orderId"));
					String invoicePk = resultSet.getString("invoicePk");
					resultSet.getString("orderId").equalsIgnoreCase(restRequest.getString("orderId"));
					assertEquals(String.valueOf(resultSet.getLong("shipmentId")), restRequest.getString("shipmentId"));
					logger.info("Spanner validation completed for the fields of Invoice table");
					// invoiceLine Table validation
					query_findStatus = "SELECT * FROM InvoiceLine where invoicePk ='" + invoicePk + "' ORDER BY lineId";
					ResultSet resultSet2 = spannerdb.connectDBspannerGetResultSet(instanceId, databaseId, query_findStatus);
					int lines = 0;
					if (resultSet2 != null) {
						while (resultSet2.next()) {
							Long lineId = resultSet2.getLong("lineId");
							System.out.println("orderLineId: " + lineId);
							assertEquals(String.valueOf(resultSet2.getLong("lineId")), String.valueOf(restRequest.getJSONArray("invoiceLines").getJSONObject(lines).getLong("lineId")));
							assertEquals(String.valueOf(resultSet2.getLong("quantityShipped")),	String.valueOf(restRequest.getJSONArray("invoiceLines").getJSONObject(lines).getLong("quantityShipped")));
							lines++;
							logger.info(" Spanner validation completed for the op InvoiceLine table");
						}
					}
					// invoiceLineCharge Table validation
					query_findStatus = "SELECT * FROM InvoiceLineCharge where invoicePk ='" + invoicePk + "'";
					ResultSet resultSet3 = spannerdb.connectDBspannerGetResultSet(instanceId, databaseId, query_findStatus);
					if (resultSet3 != null) {
						while (resultSet3.next()) {
							String associateId = resultSet3.getString("associateId");
							System.out.println("orderLineId: " + associateId);
							assertEquals(resultSet3.getString("associateId"), restRequest.getJSONArray("invoiceLines").getJSONObject(0).getJSONArray("invoiceLineCharges").getJSONObject(0).getString("associateId"));
							assertEquals(resultSet3.getString("promoId"), restRequest.getJSONArray("invoiceLines").getJSONObject(0).getJSONArray("invoiceLineCharges").getJSONObject(0).getString("promoId"));
							assertEquals(resultSet3.getString("chargeAmount"), restRequest.getJSONArray("invoiceLines").getJSONObject(0).getJSONArray("invoiceLineCharges").getJSONObject(0).getString("chargeAmount"));
							logger.info(" Spanner validation completed for the op InvoiceLineCharge table");
						}
					}
					// invoiceLineTax Table validation
					query_findStatus = "SELECT * FROM InvoiceLineTax where invoicePk ='" + invoicePk + "'";
					ResultSet resultSet4 = spannerdb.connectDBspannerGetResultSet(instanceId, databaseId, query_findStatus);
					if (resultSet4 != null) {
						while (resultSet4.next()) {
							assertEquals(resultSet4.getString("chargeCategory"), restRequest.getJSONArray("invoiceLines").getJSONObject(0).getJSONArray("invoiceLineCharges").getJSONObject(0).getString("chargeCategory"));
							assertEquals(resultSet4.getString("chargeName"), restRequest.getJSONArray("invoiceLines").getJSONObject(0).getJSONArray("invoiceLineCharges").getJSONObject(0).getString("chargeName"));
							logger.info(" Spanner validation completed for the op InvoiceLineTax table");
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info("No Record found in DB scanner for invoice query : " + query_findInvoice);
		}
	}

	// this method is with retry for publishing the request to pubsub
	public void publish_retry_releaseorder(String topicId, String message, int retryAttempts, String orderID) throws Exception {
		ProjectTopicName topicName = ProjectTopicName.of(PROJECT_ID, topicId);
		logger.info("project id is : " + PROJECT_ID);
		Publisher publisher = publisherMap.get(topicId);
		try {
			if (publisher == null) {
				synchronized (InvoiceObject.class) {
					publisher = publisherMap.get(topicId);
					if (publisher == null) {
						Duration retryDelay = Duration.ofSeconds(retryAttempts); // default:1ms
						double retryDelayMultiplier = 1.0; // back off for
															// repeated failures
						Duration maxRetryDelay = Duration.ofSeconds(retryAttempts * 2); // default:10seconds
						Duration totalTimeout = Duration.ofSeconds(retryAttempts * 15); // default:0
						Duration initialRpcTimeout = Duration.ofSeconds(retryAttempts * 4); // default:0
						Duration maxRpcTimeout = Duration.ofSeconds(retryAttempts * 15); // default:
																							// 0
						RetrySettings retrySettings = RetrySettings.newBuilder().setMaxAttempts(retryAttempts)
								.setInitialRetryDelay(retryDelay).setRetryDelayMultiplier(retryDelayMultiplier)
								.setMaxRetryDelay(maxRetryDelay).setTotalTimeout(totalTimeout)
								.setInitialRpcTimeout(initialRpcTimeout).setMaxRpcTimeout(maxRpcTimeout).build();

						// Create a publisher instance with default settings
						// bound to the topic
						publisher = Publisher.newBuilder(topicName).setRetrySettings(retrySettings).build();
						publisherMap.put(topicId, publisher);
					}
				}
			}
			// convert message to bytes
			ByteString data = ByteString.copyFromUtf8(message);

			PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data)
					.putAttributes("sourceApplication", "orderconfirm").putAttributes("sourceChannel", "ZOLA")
					.putAttributes("clientId", "123")
					.putAttributes("correlationId", "1210")
					.putAttributes("orderId", orderID)
					.build();
			// System.out.println("pubsubMessage:" + pubsubMessage);
			ApiFuture<String> future = publisher.publish(pubsubMessage);
			List<ApiFuture<String>> futures = new ArrayList<>();
			futures.add(future);
			List<String> messageIds = ApiFutures.allAsList(futures).get();

			for (String messageId : messageIds) {
				System.out.println("message id for the request is :" + messageId);
			}

		} finally {
			if (publisher != null) {
				// When finished with the publisher, shutdown to free up
				// resources.
				// publisher.shutdown();
			}
			// Schedule a message to be published. Messages are automatical
			// batched.

		}
	}

	// validate shipemntconfimation_on_success msg against spanner DB for invoice
	// tables insertion
	public void invoiceListner_SpannerDbValidation(@Named("test_id") String testid, String orderId, String cartonId,
			String jsonObject) throws InterruptedException, JSONException {
		JSONObject pubSubjsonObject = new JSONObject(jsonObject);
		logger.info("shipment confimration on success msg from method:" + pubSubjsonObject);
		JSONObject jsonObjRes = getOrderDeatilsForOrderId(testid, orderId);
		String query_findInvoice = "SELECT * FROM Invoice WHERE orderId ='" + orderId + "' and invoiceId=(select MAX(invoiceId) from Invoice where orderId='" + orderId + "')";
		logger.info("Query to validate Invoice Insertion:" + query_findInvoice);
		try {
			ResultSet resultSet = spannerdb.connectDBspannerGetResultSet(instanceId, databaseId, query_findInvoice);
			if (resultSet != null) {
				while (resultSet.next()) {
					// invoice
					assertEquals(resultSet.getString("orderId"), pubSubjsonObject.getString("orderId"));
					assertEquals(String.valueOf(resultSet.getLong("shipmentId")), pubSubjsonObject.getJSONArray("shipments").getJSONObject(0).getString("shipmentId"));
					if (resultSet.getStringList("cartonDetails") != null) {
						List<String> cartons = resultSet.getStringList("cartonDetails");
						for (int j = 0; j < cartons.size(); j++) {
							String cartonIds = cartons.get(j);
							assertEquals(cartonIds, pubSubjsonObject.getJSONArray("shipments").getJSONObject(0).getJSONArray("shipmentCartons").getJSONObject(j).getString("cartonId"));
							logger.info("cartonId from DB:" + cartonIds + " cartonId from onsuccess msg:" + pubSubjsonObject.getJSONArray("shipments").getJSONObject(0).getJSONArray("shipmentCartons").getJSONObject(j).getString("cartonId"));
						}
					} else {
						logger.info("cartonDetails column is null");
					}
					assertTrue(resultSet.getString("salesCreditDivision").equalsIgnoreCase("71")
							|| resultSet.getString("salesCreditDivision").equalsIgnoreCase("128"));

					// display validation
					logger.info("orderId from DB:" + resultSet.getString("orderId") + " orderId from onsucces msg:"	+ pubSubjsonObject.getString("orderId"));
					logger.info("shipmentId from DB:" + resultSet.getLong("shipmentId") + " shipmentId from onsucces msg:" + pubSubjsonObject.getJSONArray("shipments").getJSONObject(0).getString("shipmentId"));
					logger.info("salesCreditDivision from DB:" + resultSet.getString("salesCreditDivision")	+ "values to compare:" + "71/128");
					logger.info("Spanner validation completed for the fields of Invoice table");

					// invoiceLine
					String invoicePk = resultSet.getString("invoicePk");
					logger.info("invoicePk from invoice:" + invoicePk);
					String query_findInvoiceLine = "SELECT * FROM InvoiceLine where invoicePk ='" + invoicePk + "' order by lineId";
					logger.info("Query to validate InvoiceLine Insertion:" + query_findInvoiceLine);
					ResultSet resultSet2 = spannerdb.connectDBspannerGetResultSet(instanceId, databaseId, query_findInvoiceLine);
					logger.info("Line Items from shipment:" + pubSubjsonObject.getJSONArray("shipments").getJSONObject(0).getJSONArray("shipmentLines").length());
					int linecount = 0;
					if (resultSet2 != null) {
						while (resultSet2.next()) {
							logger.info("line item to validate" + resultSet2.getString("invoiceLineId"));
							assertEquals(resultSet2.getString("invoicePk"), invoicePk);
							assertEquals(String.valueOf(resultSet2.getLong("lineId")), pubSubjsonObject.getJSONArray("shipments").getJSONObject(0).getJSONArray("shipmentLines").getJSONObject(linecount).getString("orderLineId"));
							assertEquals(String.valueOf(resultSet2.getLong("quantityShipped")),	pubSubjsonObject.getJSONArray("shipments").getJSONObject(0).getJSONArray("shipmentLines").getJSONObject(linecount).getString("statusQty"));
							// display validation
							logger.info("lineId from DB:" + resultSet2.getLong("lineId") + " lineId from onsucces msg:" + pubSubjsonObject.getJSONArray("shipments").getJSONObject(0).getJSONArray("shipmentLines").getJSONObject(linecount).getString("orderLineId"));
							logger.info("quantity shipped from DB:" + resultSet2.getLong("quantityShipped")	+ " quantity shipped from onsucces msg:" + pubSubjsonObject.getJSONArray("shipments").getJSONObject(0).getJSONArray("shipmentLines").getJSONObject(linecount).getString("statusQty"));
							// invoiceLineCharge
							String query_findInvoiceLineCharge = "SELECT * FROM InvoiceLineCharge where invoicePk ='" + invoicePk + "'";
							logger.info("Query to validate InvoiceLine Insertion:" + query_findInvoiceLine);
							ResultSet resultSet3 = spannerdb.connectDBspannerGetResultSet(instanceId, databaseId, query_findInvoiceLineCharge);
							logger.info("Line Items from shipment:" + pubSubjsonObject.getJSONArray("shipments").getJSONObject(0).getJSONArray("shipmentLines").length());
							if (resultSet3 != null) {
								while (resultSet3.next()) {
									logger.info("linecharge item to validate"
											+ resultSet3.getString("invoiceLineChargeId"));
									assertEquals(resultSet3.getString("associateId"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("associateId"));
									assertEquals(resultSet3.getString("chargeCategory"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("chargeCategory"));
									assertEquals(resultSet3.getString("chargeName"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("chargeName"));
									assertEquals(resultSet3.getString("chargePerLine"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("chargePerLine"));
									assertEquals(resultSet3.getString("chargePerUnit"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("chargePerUnit"));
									assertEquals(resultSet3.getString("chargeType"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("chargeType"));
									assertEquals(resultSet3.getString("originalChargePerLine"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("originalChargePerLine"));
									assertEquals(resultSet3.getString("originalChargePerUnit"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("originalChargePerUnit"));
									assertEquals(resultSet3.getString("promoDescription"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("promoDescription"));
									assertEquals(resultSet3.getString("promoId"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("promoId"));
									assertEquals(resultSet3.getString("promoReasonCode"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("promoReasonCode"));
									assertEquals(resultSet3.getString("promoReasonText"), jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("promoReasonText"));
									// to display validation
									logger.info("associateId from DB:" + resultSet3.getString("associateId") + "associateId from url:" + jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("associateId"));
									logger.info("chargeCategory from DB:" + resultSet3.getString("chargeCategory") + "chargeCategory from url:"	+ jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("chargeCategory"));
									logger.info("chargeName from DB:" + resultSet3.getString("chargeName") + "chargeName from url:"	+ jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("chargeName"));
									logger.info("chargePerLine from DB:" + resultSet3.getString("chargePerLine") + "chargePerLine from url:" + jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("chargePerLine"));
									logger.info("chargePerUnit from DB:" + resultSet3.getString("chargePerUnit") + "chargePerUnit from url:" + jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("chargePerUnit"));
									logger.info("chargeType from DB:" + resultSet3.getString("chargeType")
											+ "chargeType from url:" + jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("chargeType"));
									logger.info("originalChargePerLine from DB:" + resultSet3.getString("originalChargePerLine") + "originalChargePerLine from url:" + jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("originalChargePerLine"));
									logger.info("originalChargePerUnit from DB:" + resultSet3.getString("originalChargePerUnit") + "originalChargePerUnit from url:" + jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("originalChargePerUnit"));
									logger.info("promoDescription from DB:" + resultSet3.getString("promoDescription")
											+ "promoDescription from url:" + jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("promoDescription"));
									logger.info("promoId from DB:" + resultSet3.getString("promoId") + "promoId from url:" + jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("promoId"));
									logger.info("promoReasonCode from DB:" + resultSet3.getString("promoReasonCode") + "promoReasonCode from url:" + jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("promoReasonCode"));
									logger.info("promoReasonText from DB:" + resultSet3.getString("promoReasonText") + "promoReasonText from url:" + jsonObjRes.getJSONObject("order").getJSONArray("orderLines").getJSONObject(linecount).getJSONArray("orderLineCharges").getJSONObject(0).getString("promoReasonText"));
									logger.info("Spanner validation completed for the fields of Invoice Line charge table");
								}
							}
							linecount++;
							logger.info("Spanner validation completed for the fields of Invoice Line table");
						}
					}

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("No Record found in DB scanner from invoice tables");
		}

	}

	// validate update_invoice json with spanner DB.
	public void updateInvoiceRequest_SpannerValidation(@Named("test_id") String testid, String requestJson,
			String orderId) throws JSONException {
		JSONObject restRequest = new JSONObject(requestJson);
		int invoice_count = restRequest.getJSONArray("invoices").length();
		String invoices = restRequest.getJSONArray("invoices").getJSONObject(0).getString("invoiceId");
		for (int i = 1; i < invoice_count; i++) {
			invoices = invoices + "," + restRequest.getJSONArray("invoices").getJSONObject(i).getString("invoiceId");
		}
		String query_findInvoice = "SELECT * FROM Invoice WHERE orderId ='" + orderId + "' and invoiceId in(" + invoices + ") order by invoiceId";
		logger.info("Query to Update invoice:" + query_findInvoice);
		try {
			ResultSet resultSet = spannerdb.connectDBspannerGetResultSet(instanceId, databaseId, query_findInvoice);
			int invoice = 0;
			if (resultSet != null) {
				while (resultSet.next()) {
					// invoice
					assertEquals(resultSet.getString("orderId"), restRequest.getString("orderId"));
					assertEquals(String.valueOf(resultSet.getLong("invoiceId")), restRequest.getJSONArray("invoices").getJSONObject(invoice).getString("invoiceId"));
					/*
					 * for invoice: hard coding the StatusCode code to "5000" and StatusDesc to "Invoiced: and lastUpdatedBy to "invoice_updateInvoice" and lastUpdatedTs to "TimeStamp value greater then CreatedTS" instead of reading from input json, because both attribute updated by service internally */
					assertEquals(resultSet.getString("statusCode"), "5000");
					assertEquals(resultSet.getString("statusDesc"), "INVOICED");
					assertEquals(resultSet.getString("lastUpdatedBy"), "invoice_updateInvoice");
					Timestamp lstupdts = resultSet.getTimestamp("lastUpdatedTs");
					Timestamp crtupdts = resultSet.getTimestamp("createdTs");
					if (lstupdts.compareTo(crtupdts) == 1) {
						logger.info("lastUpdatedTs:" + lstupdts + " is greater than createdTs:" + crtupdts);
					}
					/* for invoice: updating the sttDetails from input json */
					List<String> sttDetails = resultSet.getStringList("sttDetails");
					logger.info("sttDetails from DB:" + sttDetails);
					for (int i = 0; i < sttDetails.size(); i++) {
						JSONObject obj = new JSONObject(sttDetails.get(i));
						assertEquals(obj.getString("division"), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("division"));
						assertEquals(obj.getString("date"), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("date"));
						assertEquals(obj.getString("store"), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("store"));
						assertEquals(obj.getString("registerNo"), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("registerNo"));
						assertEquals(obj.getString("transactionId"), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("transactionId"));
					}
					// display validations
					logger.info("orderId from DB:" + resultSet.getString("orderId") + " orderId from pubsub:" + restRequest.getString("orderId"));
					logger.info("invoiceId from DB:" + String.valueOf(resultSet.getLong("invoiceId")) + " invoiceId from pubsub:" + restRequest.getJSONArray("invoices").getJSONObject(invoice).getString("invoiceId"));
					logger.info("statusCode from DB:" + resultSet.getString("statusCode") + " statusCode updated:" + "5000");
					logger.info("statusDesc from DB:" + resultSet.getString("statusDesc") + " statusDesc updated:" + "INVOICED");
					logger.info("lastUpdatedBy from DB:" + resultSet.getString("lastUpdatedBy") + " lastUpdatedBy updated:" + "invoice_updateInvoice");
					logger.info("lastUpdatedTs from DB:" + resultSet.getTimestamp("lastUpdatedTs") + " createdTs from DB:" + resultSet.getTimestamp("createdTs"));
					for (int i = 0; i < sttDetails.size(); i++) {
						JSONObject obj = new JSONObject(sttDetails.get(i));
						logger.info("sttDetails: division from DB:" + obj.getString("division")	+ " division from pubsub:" + restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("division"));
						logger.info("sttDetails: date from DB:" + obj.getString("date") + " date from pubsub:" + restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("date"));
						logger.info("sttDetails: store from DB:" + obj.getString("store") + " store from pubsub:" + restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("store"));
						logger.info("sttDetails: registerNo from DB:" + obj.getString("registerNo") + " registerNo from pubsub:" + restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("registerNo"));
						logger.info("sttDetails: transactionId from DB:" + obj.getString("transactionId") + " transactionId from pubsub:" + restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("transactionId"));
					}
					logger.info("validation completed for Invoice table");
					
					// invoiceLine
					String invoicePk = resultSet.getString("invoicePk");
					String query_findInvoiceLine = "SELECT * FROM InvoiceLine where invoicePk ='" + invoicePk + "' order by lineId limit 1";
					logger.info("Query to Update invoiceLine:" + query_findInvoiceLine);
					ResultSet resultSet2 = spannerdb.connectDBspannerGetResultSet(instanceId, databaseId, query_findInvoiceLine);
					// validating lineId and other fields for 1st line item from each invoice
					if (resultSet2 != null) {
						while (resultSet2.next()) {
							assertEquals(String.valueOf(resultSet2.getLong("lineId")), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("invoiceLines").getJSONObject(0).getString("lineId"));
							assertEquals(resultSet2.getString("statusCode"), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("invoiceLines").getJSONObject(0).getString("statusCode"));
							assertEquals(resultSet2.getString("statusDesc"), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("invoiceLines").getJSONObject(0).getString("statusDesc"));
							// display validations
							logger.info("lineId from DB:" + String.valueOf(resultSet2.getLong("lineId")) + " lineId from pubsub:" + restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("invoiceLines").getJSONObject(0).getString("lineId"));
							logger.info("statusCode from DB:" + resultSet2.getString("statusCode") + " statusCode from pubsub:" + restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("invoiceLines").getJSONObject(0).getString("statusCode"));
							logger.info("statusDesc from DB:" + resultSet2.getString("statusDesc") + " statusDesc from pubsub:" + restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("invoiceLines").getJSONObject(0).getString("statusDesc"));
							logger.info("validation completed for InvoiceLine table");
						}
					}
					invoice++;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("No Record found in DB scanner for invoice query : " + query_findInvoice);
		}
	}

	public String toSqlInBlock(List<String> values) {
		List<String> newValues = new ArrayList<String>();
		for (String value : values) {
			newValues.add(String.format("'%s'", value));
		}
		return newValues.toString().replace("[", "(").replace("]", ")");
	}

	public void deleteUsingDml(String query) {
		SpannerOptions options = SpannerOptions.newBuilder().build();
		Spanner spanner = options.getService();
		DatabaseClient dbClient = spanner.getDatabaseClient(DatabaseId.of(PROJECT_ID, instanceId, databaseId));
		dbClient.readWriteTransaction().run(new TransactionCallable<Void>() {
			@Override
			public Void run(TransactionContext transaction) throws Exception {
				long rowCount = transaction.executeUpdate(Statement.of(query));
				System.out.printf("%d record deleted.\n", rowCount);
				return null;
			}
		});
	}

	private JSONObject getOrderDeatilsForOrderId(@Named("test_id") String testid, String orderId) throws JSONException {
		InvoiceObject invoiceObj = new InvoiceObject("Invoice", testid);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("correlationId", "1234");
		invoiceObj.getRestRequest().setHeaders(headers);
		invoiceObj.setEndPoint(getorderdetailsURL + orderId);
		invoiceObj.sendRequest("GET");
		logger.info("got response form getorderdeatils for orderId");
		Response response = invoiceObj.getRestRequest().getResponse();
		JSONObject jsonObjRes = new JSONObject(response.getBody().asString());
		return jsonObjRes;
	}

}
