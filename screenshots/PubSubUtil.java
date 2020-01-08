package com.mays.mst.automation.op.module;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.testng.Assert;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.macys.mst.artemis.reports.StepDetail;

public class PubSubUtil implements MessageReceiver {
	private static Logger LOGGER = Logger.getLogger(PubSubUtil.class.getName());

	public static List<String> getListOfMatchingMessages() {
		return ListOfMatchingMessages;
	}

	public static void setListOfMatchingMessages(List<String> listOfMatchingMessages) {
		ListOfMatchingMessages = listOfMatchingMessages;
	}

	public static List<String> ListOfMatchingMessages = new LinkedList<>();

	public static String getMatchingMessage() {
		return MatchingMessage;
	}

	public static void setMatchingMessage(String matchingMessage) {
		MatchingMessage = matchingMessage;
	}

	public static String MatchingMessage;
	public static Subscriber subscriber = null;
	private static final BlockingQueue<PubsubMessage> messages = new LinkedBlockingDeque<>();

	public synchronized void publishMessage(String ProjectId, String topic, List<String> messages) throws Exception {
		ProjectTopicName topicName = ProjectTopicName.of(ProjectId, topic);
		Publisher publisher = null;

		try {

			publisher = Publisher.newBuilder(topicName).build();
			for (String message : messages) {
				ByteString data = ByteString.copyFromUtf8(message);
				PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
				ApiFuture<String> future = publisher.publish(pubsubMessage);
				ApiFutures.addCallback(future, new ApiFutureCallback<String>() {
					@Override
					public void onFailure(Throwable throwable) {
						if (throwable instanceof ApiException) {
							ApiException apiException = ((ApiException) throwable);

							LOGGER.info("ERROR code :" + apiException.getStatusCode().getCode());
							LOGGER.info(apiException.isRetryable());
						}
						LOGGER.info("ERROR PUBLISHING MESSAGE :" + message);
						StepDetail.addDetail("ERROR PUBLISHING MESSAGE :" + message, true);
					}

					@Override
					public void onSuccess(String messageId) {
						LOGGER.info("Message Successfully published " + messageId);
						StepDetail.addDetail("Message Successfully published " + messageId, true);
					}
				}, MoreExecutors.directExecutor());
			}

		} finally {
			if (publisher != null) {
				publisher.shutdown();
				publisher.awaitTermination(1, TimeUnit.MINUTES);
				LOGGER.info("PUBLISHER STOPPED");
				StepDetail.addDetail("PUBLISHER STOPPED", true);
			}
		}

	}

	public synchronized void publishMessageWithAttributes(String ProjectId, String topic,
			HashMap<String, String> values, List<String> messages) throws Exception {
		ProjectTopicName topicName = ProjectTopicName.of(ProjectId, topic);
		Publisher publisher = null;

		try {

			publisher = Publisher.newBuilder(topicName).build();
			for (String message : messages) {
				ByteString data = ByteString.copyFromUtf8(message);
				PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).putAllAttributes(values).build();
				ApiFuture<String> future = publisher.publish(pubsubMessage);
				ApiFutures.addCallback(future, new ApiFutureCallback<String>() {
					@Override
					public void onFailure(Throwable throwable) {
						if (throwable instanceof ApiException) {
							ApiException apiException = ((ApiException) throwable);

							LOGGER.info("ERROR code :" + apiException.getStatusCode().getCode());
							LOGGER.info(apiException.isRetryable());
						}
						LOGGER.info("ERROR PUBLISHING MESSAGE :" + message);
						StepDetail.addDetail("ERROR PUBLISHING MESSAGE :" + message, true);
					}

					@Override
					public void onSuccess(String messageId) {
						LOGGER.info("Message Successfully published " + messageId);
						StepDetail.addDetail("Message Successfully published " + messageId, true);
					}
				}, MoreExecutors.directExecutor());
			}

		} finally {
			if (publisher != null) {
				publisher.shutdown();
				publisher.awaitTermination(1, TimeUnit.MINUTES);
				LOGGER.info("PUBLISHER STOPPED");
				StepDetail.addDetail("PUBLISHER STOPPED", true);
			}
		}

	}

	@Override
	public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
		LOGGER.info("Receiving messages........");
		messages.offer(message);
		LOGGER.info("Message inside consumer :" + message.getData().toStringUtf8());
		StepDetail.addDetail("Message inside consumer :" + message.getData().toStringUtf8(), true);
		consumer.ack();
	}

	public synchronized void subscribeProject(String projectId, String subscriptionId) {
		ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
		subscriber = Subscriber.newBuilder(subscriptionName, new PubSubUtil()).build();
		subscriber.startAsync().awaitRunning();
	}

	public synchronized String readMessageFromPubSub(String validationContent, int delayTime)
			throws InterruptedException {
		TimeUnit.SECONDS.sleep(delayTime);
		String getMessage = null;
		PubsubMessage message;
		
		while ((message = messages.take()) != null) {
			System.out.println("published message " + message.getData().toStringUtf8());
			if (message.getData().toStringUtf8().contains(validationContent)) {
				getMessage = message.getData().toStringUtf8().toString();
				LOGGER.info("MATCHING MESSAGE FOUND :" + message.getData().toStringUtf8());
				setMatchingMessage(message.getData().toStringUtf8());
				StepDetail.addDetail("MATCHING MESSAGE FOUND :" + message.getData().toStringUtf8(), true);
				subscriber.stopAsync();
				LOGGER.info("SUBSCRIBER STOPPED");
				StepDetail.addDetail("SUBSCRIBER STOPPED", true);
				break;
			}
		}
		return getMessage;
	}

	public synchronized void readMessage(String validationContent, int delayTime) throws InterruptedException {
		TimeUnit.SECONDS.sleep(delayTime);

		PubsubMessage message;

		while ((message = messages.take()) != null) {
			if (message.getData().toStringUtf8().contains(validationContent)) {
				System.out.println("header of releaseorder is " + message.getAttributesMap());

				LOGGER.info("MATCHING MESSAGE FOUND :" + message.getData().toStringUtf8());
				setMatchingMessage(message.getData().toStringUtf8());
				StepDetail.addDetail("MATCHING MESSAGE FOUND :" + message.getData().toStringUtf8(), true);
				subscriber.stopAsync();
				LOGGER.info("SUBSCRIBER STOPPED");
				StepDetail.addDetail("SUBSCRIBER STOPPED", true);
				break;
			} else {
				LOGGER.info("Messages Size after polling :" + messages.size());
				StepDetail.addDetail("Messages Size after polling :" + messages.size(), false);
				if (messages.size() == 0) {
					subscriber.stopAsync();
					LOGGER.info("SUBSCRIBER TERMINATED WITHOUT READING MESSAGE");
					StepDetail.addDetail("SUBSCRIBER TERMINATED WITHOUT READING MESSAGE", false);
					break;
				}

			}

		}
	}

	public synchronized String readMessageByPattren(String validationContent, int delayTime)
			throws InterruptedException {
		TimeUnit.SECONDS.sleep(delayTime);
		PubsubMessage message;
		String validate = validationContent;
		String retrunMessage = null;
		Pattern validationContentPattren = Pattern.compile(validationContent);

		while ((message = messages.take()) != null) {
			Boolean matched = validationContentPattren.matcher(message.getData().toStringUtf8()).lookingAt();
			StepDetail.addDetail("BOOLEAN" + matched, matched);
			LOGGER.info("BOOLEAN" + matched);

			if (matched) {
				LOGGER.info("MATCHING MESSAGE FOUND :" + message.getData().toStringUtf8());
				setMatchingMessage(message.getData().toStringUtf8());
				StepDetail.addDetail("MATCHING MESSAGE FOUND :" + message.getData().toStringUtf8(), true);
				subscriber.stopAsync();
				LOGGER.info("SUBSCRIBER STOPPED");
				StepDetail.addDetail("SUBSCRIBER STOPPED", true);
				retrunMessage = message.getData().toStringUtf8();
				break;
			} else {
				LOGGER.info("Messages Size after polling :" + messages.size());
				StepDetail.addDetail("Messages Size after polling :" + messages.size(), false);
				if (messages.size() == 0) {
					subscriber.stopAsync();
					LOGGER.info("SUBSCRIBER TERMINATED WITHOUT READING MESSAGE");
					StepDetail.addDetail("SUBSCRIBER TERMINATED WITHOUT READING MESSAGE", false);
					break;
				}
			}
		}
		return retrunMessage;
	}

	public synchronized String readMessages(String validationContent, int delayTime) throws InterruptedException {
		TimeUnit.SECONDS.sleep(delayTime);
		PubsubMessage message;
		String validate = validationContent;
		String retrunMessage = null;

		while ((message = messages.take()) != null) {
			if (message.getData().toStringUtf8().contains(validate)) {
				LOGGER.info("MATCHING MESSAGE FOUND :" + message.getData().toStringUtf8());
				setMatchingMessage(message.getData().toStringUtf8());
				StepDetail.addDetail("MATCHING MESSAGE FOUND :" + message.getData().toStringUtf8(), true);
				subscriber.stopAsync();
				LOGGER.info("SUBSCRIBER STOPPED");
				StepDetail.addDetail("SUBSCRIBER STOPPED", true);
				retrunMessage = message.getData().toStringUtf8();
				break;
			} else {
				LOGGER.info("Messages Size after polling :" + messages.size());
				StepDetail.addDetail("Messages Size after polling :" + messages.size(), false);
				if (messages.size() == 0) {
					subscriber.stopAsync();
					LOGGER.info("SUBSCRIBER TERMINATED WITHOUT READING MESSAGE");
					StepDetail.addDetail("SUBSCRIBER TERMINATED WITHOUT READING MESSAGE", false);
					break;
				}
			}
		}
		return retrunMessage;
	}

	public synchronized void readListOfMessages(String validationContent, int delayTime) throws InterruptedException {
		TimeUnit.SECONDS.sleep(delayTime);
		PubsubMessage message;
		List<String> MatchingMessages = new LinkedList<>();

		while ((message = messages.take()) != null) {
			if (message.getData().toStringUtf8().contains(validationContent)) {
				LOGGER.info("MATCHING MESSAGE FOUND :" + message.getData().toStringUtf8());
				MatchingMessages.add(message.getData().toStringUtf8());
				StepDetail.addDetail("MATCHING MESSAGE FOUND :" + message.getData().toStringUtf8(), true);
				setListOfMatchingMessages(MatchingMessages);
			}

			LOGGER.info("Messages Size after polling :" + messages.size());
			StepDetail.addDetail("Messages Size after polling :" + messages.size(), true);

			if (messages.size() == 0) {
				subscriber.stopAsync();
				LOGGER.info("SUBSCRIBER STOPPED");
				StepDetail.addDetail("SUBSCRIBER STOPPED", true);
				break;
			}

		}
		LOGGER.info("List of Matching messages :" + getListOfMatchingMessages().toString());
		LOGGER.info("List of Matching messages size :" + getListOfMatchingMessages().size());
		StepDetail.addDetail("List of Matching messages :" + getListOfMatchingMessages().toString(), true);

		if (MatchingMessages.isEmpty()) {
			subscriber.stopAsync();
			LOGGER.info("SUBSCRIBER TERMINATED WITHOUT READING MESSAGE");
			StepDetail.addDetail("SUBSCRIBER TERMINATED WITHOUT READING MESSAGE", true);
			Assert.assertTrue(false);
		}

	}

	public void createSubscription(String subscriptionId, String topicId) throws IOException {
		try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {

			ProjectTopicName topicName = ProjectTopicName.of("mtech-wms-oms-nonprod", topicId);

			ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("mtech-wms-oms-nonprod",
					subscriptionId);
			// create a pull subscription with default acknowledgement deadline
			Subscription subscription = subscriptionAdminClient.createSubscription(subscriptionName, topicName,
					PushConfig.getDefaultInstance(), 0);

		}
	}

	public void deleteSubscription(String subscriptionId) throws IOException {
		try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
			ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("mtech-wms-oms-nonprod",
					subscriptionId);
			subscriptionAdminClient.deleteSubscription(subscriptionName);
		}

	}
}