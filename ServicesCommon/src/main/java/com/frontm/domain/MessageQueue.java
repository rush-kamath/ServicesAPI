package com.frontm.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.frontm.serialize.ContentSerailizer;

public class MessageQueue {
	private String tableName;
	private String returnValues;
	private Item item;

	@JsonProperty("TableName")
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@JsonProperty("ReturnValues")
	public String getReturnValues() {
		return returnValues;
	}

	public void setReturnValues(String returnValues) {
		this.returnValues = returnValues;
	}

	@JsonProperty("Item")
	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	@Override
	public String toString() {
		return "MessageQueue [tableName=" + tableName + ", returnValues=" + returnValues + ", item=" + item + "]";
	}

	public static class Item {
		private String userUuid;
		private Long createdOn;
		private Content content;
		private String conversation;
		private Boolean push;
		private String createdBy;
		private Boolean notifyToOwner;

		@JsonSerialize(using = ContentSerailizer.class)
		public static class Content {
			private String contentType;
			private String details;
			private String error;

			public String getContentType() {
				return contentType;
			}

			public void setContentType(String contentType) {
				this.contentType = contentType;
			}

			public String getDetails() {
				return details;
			}

			public void setDetails(String details) {
				this.details = details;
			}

			public String getError() {
				return error;
			}

			public void setError(String error) {
				this.error = error;
			}

			@Override
			public String toString() {
				return "Content [contentType=" + contentType + ", details=" + details + ", error=" + error + "]";
			}
		}

		public String getUserUuid() {
			return userUuid;
		}

		public void setUserUuid(String userUuid) {
			this.userUuid = userUuid;
		}

		public Long getCreatedOn() {
			return createdOn;
		}

		public void setCreatedOn(Long createdOn) {
			this.createdOn = createdOn;
		}

		public Content getContent() {
			return content;
		}

		public void setContent(Content content) {
			this.content = content;
		}

		public String getConversation() {
			return conversation;
		}

		public void setConversation(String conversation) {
			this.conversation = conversation;
		}

		public Boolean getPush() {
			return push;
		}

		public void setPush(Boolean push) {
			this.push = push;
		}

		public String getCreatedBy() {
			return createdBy;
		}

		public void setCreatedBy(String createdBy) {
			this.createdBy = createdBy;
		}

		public Boolean getNotifyToOwner() {
			return notifyToOwner;
		}

		public void setNotifyToOwner(Boolean notifyToOwner) {
			this.notifyToOwner = notifyToOwner;
		}

		@Override
		public String toString() {
			return "MessageQueue [userUuid=" + userUuid + ", createdOn=" + createdOn + ", content=" + content
					+ ", conversation=" + conversation + ", push=" + push + ", createdBy=" + createdBy
					+ ", notifyToOwner=" + notifyToOwner + "]";
		}
	}
}
