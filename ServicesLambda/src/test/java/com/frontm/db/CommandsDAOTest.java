package com.frontm.db;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.frontm.domain.db.Command;
import com.frontm.exception.FrontMException;
import com.frontm.exception.FrontMException.FRONTM_ERROR_CODE;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class CommandsDAOTest {
	@Mock DynamoDBMapper mockMapper;
	@Mock PaginatedQueryList<Command> results;
	final String commandName = "commandName";

	private static AmazonDynamoDB actualDynamoDB;

	@BeforeClass
	public static void initialize() {
		actualDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.withCredentials(new ProfileCredentialsProvider()).build();
	}

	private Command callDaoWithMocks(String commandName) throws FrontMException {
		CommandsDAO dao = new CommandsDAO();
		dao.setDynamoDBMapper(mockMapper);

		return dao.getCommandFromDB(commandName);
	}

	@Test
	public void testUnavailableCommand() {
		when(mockMapper.query(eq(Command.class), any(DynamoDBQueryExpression.class))).thenReturn(results);
		when(results.isEmpty()).thenReturn(true);

		try {
			callDaoWithMocks(commandName);
		} catch (FrontMException e) {
			assertEquals(FRONTM_ERROR_CODE.UNAVAILABLE_COMMAND, e.getErrorCode());
		}
	}

	@Test
	public void testCommandsInDB() throws Exception {
		Command command = new Command();

		when(mockMapper.query(eq(Command.class), any(DynamoDBQueryExpression.class))).thenReturn(results);
		when(results.isEmpty()).thenReturn(false);
		when(results.get(0)).thenReturn(command);

		assertEquals(command, callDaoWithMocks(commandName));
	}

	private Command callDaoWithRealDB(String commandName) throws FrontMException {
		CommandsDAO dao = new CommandsDAO();
		dao.setDynamoDBMapper(new DynamoDBMapper(actualDynamoDB));

		return dao.getCommandFromDB(commandName);
	}

	@Test
	public void testUnavailableCommandsRealDB() {
		try {
			callDaoWithRealDB(commandName);
		} catch (FrontMException e) {
			assertEquals(FRONTM_ERROR_CODE.UNAVAILABLE_COMMAND, e.getErrorCode());
		}
	}

	@Test
	public void testAvailableService() throws Exception {
		final String validCommandName = "BuildCache";
		Command command = callDaoWithRealDB(validCommandName);
		assertEquals(validCommandName, command.getCommandName());
	}
}
