package com.frontm.db;

import static com.frontm.exception.FrontMException.FRONTM_ERROR_CODE.UNAVAILABLE_COMMAND;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.frontm.domain.db.Command;
import com.frontm.exception.FrontMException;
import org.apache.log4j.Logger;

public class CommandsDAO extends BaseDAO {
	private static final Logger logger = Logger.getLogger(CommandsDAO.class);

	public Command getCommandFromDB(String commandName) throws FrontMException {
		Map<String, AttributeValue> queryValues = new HashMap<String, AttributeValue>();
		queryValues.put(":val1", new AttributeValue().withS(commandName));
		DynamoDBQueryExpression<Command> queryExpression = new DynamoDBQueryExpression<Command>()
				.withKeyConditionExpression("commandName = :val1")
				.withExpressionAttributeValues(queryValues);

		PaginatedQueryList<Command> results = getMapper().query(Command.class, queryExpression);
		if (results.isEmpty()) {
			logger.info("Command not found: " + commandName);
			throw new FrontMException(UNAVAILABLE_COMMAND);
		}

		return results.get(0);
	}
}
