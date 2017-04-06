package com.frontm.domain.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "Command")
public class Command {
	private String commandName;
	private String lambdaName;
	
	public Command() {
		
	}
	
	public Command(String commandName) {
		this.commandName = commandName;
	}

	@DynamoDBHashKey
	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public String getLambdaName() {
		return lambdaName;
	}

	public void setLambdaName(String lambdaName) {
		this.lambdaName = lambdaName;
	}

	@Override
	public String toString() {
		return "Commands [commandName=" + commandName + ", lambdaName=" + lambdaName + "]";
	}

}
