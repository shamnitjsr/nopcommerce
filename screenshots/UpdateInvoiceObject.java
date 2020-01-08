package com.mays.mst.automation.op.module;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jbehave.core.annotations.Named;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Duration;

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
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.macys.mst.automation.gcloud.SpannerDB;
import com.macys.mst.automation.rest.RESTCoreObject;

public class UpdateInvoiceObject extends RESTCoreObject {

	SpannerDB spannerdb = new SpannerDB();
	String instanceId = "orderplatform";
	String databaseId = "order_db_qa";
	private static Logger logger = Logger.getLogger(UpdateInvoiceObject.class);
	private static final String PROJECT_ID = ServiceOptions.getDefaultProjectId();
	private static HashMap<String, Publisher> publisherMap = new HashMap<String, Publisher>();
	public UpdateInvoiceObject(String name, String testId) {
		super(name, testId);
	}
	
	        // this method is with retry for publishing the request to pubsub
			public void publish_retry_releaseorder(String topicId, String message, int retryAttempts, String orderID) throws Exception {
				ProjectTopicName topicName = ProjectTopicName.of(PROJECT_ID, topicId);
				logger.info("project id is : " + PROJECT_ID);
				Publisher publisher = publisherMap.get(topicId);
				try {
					if (publisher == null) {
						synchronized (UpdateInvoiceObject.class) {
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
							.putAttributes("correlationId", "1210").putAttributes("orderId", orderID).build();
                    //System.out.println("pubsubMessage:" + pubsubMessage);
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
			
			// validate update_invoice json with spanner DB.
			public void updateInvoiceRequest_SpannerValidation(@Named("test_id") String testid, String requestJson, String orderId) throws JSONException {
				JSONObject restRequest = new JSONObject(requestJson);
				int invoice_count= restRequest.getJSONArray("invoices").length();
				String invoices = restRequest.getJSONArray("invoices").getJSONObject(0).getString("invoiceId");
				for(int i=1; i< invoice_count; i++) {
					invoices= invoices + "," + restRequest.getJSONArray("invoices").getJSONObject(i).getString("invoiceId");
				}
				String query_findInvoice = "SELECT * FROM Invoice WHERE orderId ='"	+ orderId + "' and invoiceId in("+ invoices +") order by invoiceId";
				logger.info("Query to Update invoice:" + query_findInvoice);
				try {
					ResultSet resultSet = spannerdb.connectDBspannerGetResultSet(instanceId, databaseId, query_findInvoice);
					int invoice=0;
					if (resultSet != null) {
						while (resultSet.next()) {
							// invoice					
							assertEquals(resultSet.getString("orderId"), restRequest.getString("orderId"));
							assertEquals(String.valueOf(resultSet.getLong("invoiceId")), restRequest.getJSONArray("invoices").getJSONObject(invoice).getString("invoiceId"));
							/*for invoice: hard coding the StatusCode code to 5000 and StatusDesc to Invoiced instead of reading from input json, because both attribute updated by service internally*/
							assertEquals(resultSet.getString("statusCode"), "5000");
							assertEquals(resultSet.getString("statusDesc"), "INVOICED");
							assertEquals(resultSet.getString("lastUpdatedBy"), "invoice_updateInvoice");
							Timestamp lstupdts= resultSet.getTimestamp("lastUpdatedTs");
							Timestamp crtupdts= resultSet.getTimestamp("createdTs");
							if(lstupdts.compareTo(crtupdts)==1) {
								logger.info("lastUpdatedTs:" + lstupdts + " is greater than createdTs:" + crtupdts);
							}
							/*for invoice: updating the sttDetails from input json*/
							List<String> sttDetails= resultSet.getStringList("sttDetails");
							logger.info("sttDetails from DB:" + sttDetails);
							for(int i=0; i< sttDetails.size(); i++) {
							JSONObject obj= new JSONObject(sttDetails.get(i));
							assertEquals(obj.getString("division") , restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("division"));
							assertEquals(obj.getString("date"), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("date"));
							assertEquals(obj.getString("store"), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("store"));
							assertEquals(obj.getString("registerNo"), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("registerNo"));
							assertEquals(obj.getString("transactionId"), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("transactionId"));
							
							}
							//display validations
							logger.info("orderId from DB:" + resultSet.getString("orderId") + " orderId from pubsub:" + restRequest.getString("orderId"));
							logger.info("invoiceId from DB:" + String.valueOf(resultSet.getLong("invoiceId")) + " invoiceId from pubsub:" + restRequest.getJSONArray("invoices").getJSONObject(invoice).getString("invoiceId"));
							logger.info("statusCode from DB:" + resultSet.getString("statusCode") + " statusCode updated:" + "5000");
							logger.info("statusDesc from DB:" + resultSet.getString("statusDesc") + " statusDesc updated:" + "INVOICED");
							logger.info("lastUpdatedBy from DB:" + resultSet.getString("lastUpdatedBy") + " lastUpdatedBy updated:" + "invoice_updateInvoice");
							logger.info("lastUpdatedTs from DB:" + resultSet.getTimestamp("lastUpdatedTs") + " createdTs from DB:" + resultSet.getTimestamp("createdTs"));
							for(int i=0; i< sttDetails.size(); i++) {
								JSONObject obj= new JSONObject(sttDetails.get(i));
								logger.info("sttDetails: division from DB:" + obj.getString("division") + " division from pubsub:" + restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("sttDetails").getJSONObject(i).getString("division"));
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
							//validating lineId and other fields for 1st line item from each invoice
							if (resultSet2 != null) {
								while (resultSet2.next()) {
									assertEquals(String.valueOf(resultSet2.getLong("lineId")), restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("invoiceLines").getJSONObject(0).getString("lineId"));
									assertEquals(resultSet2.getString("statusCode"),restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("invoiceLines").getJSONObject(0).getString("statusCode"));
									assertEquals(resultSet2.getString("statusDesc"),restRequest.getJSONArray("invoices").getJSONObject(invoice).getJSONArray("invoiceLines").getJSONObject(0).getString("statusDesc"));
									//display validations
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


}
