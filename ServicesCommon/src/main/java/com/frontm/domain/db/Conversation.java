package com.frontm.domain.db;

import static com.frontm.util.StringUtil.isNotEmpty;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "frontm-mobilehub-1030065648-Conversations")
public class Conversation {
	private String uuid;
	private String conversationOwner;
	private List<String> participants;
	private List<String> onChannels;
	private String bot;
	private Boolean closed;

	@DynamoDBHashKey
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getConversationOwner() {
		return conversationOwner;
	}

	public void setConversationOwner(String conversationOwner) {
		this.conversationOwner = conversationOwner;
	}

	public List<String> getParticipants() {
		return participants;
	}

	public void setParticipants(List<String> participants) {
		this.participants = participants;
	}

	public List<String> getOnChannels() {
		return onChannels;
	}

	public void setOnChannels(List<String> onChannels) {
		this.onChannels = onChannels;
	}

	public String getBot() {
		return bot;
	}

	public void setBot(String bot) {
		this.bot = bot;
	}

	public Boolean isClosed() {
		return closed;
	}

	public void setClosed(Boolean closed) {
		this.closed = closed;
	}

	@Override
	public String toString() {
		return "Conversation [uuid=" + uuid + ", conversationOwner=" + conversationOwner + ", participants="
				+ participants + ", onChannels=" + onChannels + ", bot=" + bot + ", closed=" + closed + "]";
	}

	@DynamoDBIgnore
	public boolean isAnyFieldExceptUuidPresent() {
		return isNotEmpty(bot) || isNotEmpty(participants)
				|| isNotEmpty(onChannels) || closed != null;
	}
}
