package com.frontm.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodbv2.model.PutRequest;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.frontm.exception.FrontMException;
import com.frontm.util.JaxbParserUtil;

public class CacheTableDAO {
	private static final Logger logger = Logger.getLogger(JaxbParserUtil.class);
	private static final int BATCH_SIZE = 25;

	public static void insertItemsIntoDB(final List<Map<String, String>> dbItems, String cacheTableName)
			throws FrontMException {
		try {
			if (dbItems.isEmpty()) {
				return;
			}

			final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
			List<WriteRequest> unprocessedWrites = createDBWriteRequests(dbItems);

			for (int attempt = 0; attempt < 3; attempt++) {
				backOffLogic(attempt);

				logger.info("Attempt " + attempt + ". Inserting " + unprocessedWrites.size() + " rows");
				unprocessedWrites = insertBatches(splitIntoBatches(unprocessedWrites), dynamoDB, cacheTableName);
				if (unprocessedWrites.isEmpty()) {
					break;
				}
			}
			logFinalStatus(unprocessedWrites);
		} catch (Exception e) {
			throw new FrontMException("Error while inserting data into the db cache: " + e.getMessage());
		}

	}

	private static void backOffLogic(int attempt) {
		try {
			final long backOffMillis = attempt * 1000;
			logger.info("Unprocessed items exist. Backing off for " + backOffMillis + " milliseconds");
			Thread.sleep(backOffMillis);
		} catch (InterruptedException ignoredException) {
			logger.info("Ignoring the interrupt exception on the thread");
		}
	}

	private static void logFinalStatus(List<WriteRequest> unprocessedItems) {
		if (unprocessedItems.isEmpty()) {
			logger.info("All rows inserted successfully");
		} else {
			List<String> failedIds = new ArrayList<>();
			unprocessedItems.forEach(item -> failedIds.add(item.getPutRequest().getItem().get("id").getS()));
			logger.error("Unable to insert all rows after 3 attempts. Unprocessed ids count: " + failedIds.size());
			logger.info("Unprocessed ids are: " + failedIds);
		}
	}

	private static List<WriteRequest> insertBatches(List<List<WriteRequest>> batches, AmazonDynamoDB dynamoDB,
			String cacheTableName) {
		logger.info("Inserting in " + batches.size() + " batches");

		final Long startMillis = System.currentTimeMillis();
		final List<WriteRequest> allFailedWrites = new ArrayList<>();
		final Map<String, List<WriteRequest>> batchTableReq = new HashMap<String, List<WriteRequest>>();

		batches.forEach(batch -> {
			batchTableReq.put(cacheTableName, batch);
			final BatchWriteItemResult result = dynamoDB.batchWriteItem(new BatchWriteItemRequest(batchTableReq));
			final Map<String, List<WriteRequest>> curFailedWrites = result.getUnprocessedItems();
			if (curFailedWrites != null && curFailedWrites.containsKey(cacheTableName)) {
				allFailedWrites.addAll(curFailedWrites.get(cacheTableName));
			}
		});

		logger.info("Time to insert: " + (System.currentTimeMillis() - startMillis));
		logger.info("Failed writes counts: " + allFailedWrites.size());
		return allFailedWrites;
	}

	private static List<List<WriteRequest>> splitIntoBatches(List<WriteRequest> batchList) {
		int i = 0;
		List<List<WriteRequest>> batches = new ArrayList<>();
		while (i < batchList.size()) {
			int nextInc = Math.min(batchList.size() - i, BATCH_SIZE);
			batches.add(batchList.subList(i, i + nextInc));
			i = i + nextInc;
		}
		return batches;
	}

	private static List<WriteRequest> createDBWriteRequests(final List<Map<String, String>> dbItems) {
		List<WriteRequest> batchList = new ArrayList<>();
		dbItems.forEach(map -> {
			Item item = new Item().withMap("document", map);
			Map<String, AttributeValue> attributes = InternalUtils.toAttributeValues(item);
			batchList.add(new WriteRequest(new PutRequest(attributes.get("document").getM())));
		});
		return batchList;
	}
}
