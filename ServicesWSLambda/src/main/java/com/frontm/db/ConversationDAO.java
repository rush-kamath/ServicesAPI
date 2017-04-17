package com.frontm.db;

import com.frontm.domain.db.Conversation;

public class ConversationDAO extends BaseDAO {
	public void saveConversation(Conversation conversation) {
		getMapper().save(conversation);
	}
}
