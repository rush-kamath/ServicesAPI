package com.frontm.domain;

public class MessageQueue {
	private String userUuid;
	private Long createdOn;
	private String content;
	private String contentType;
	private String conversation;
	private Boolean push;
	private String createdBy;
	

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
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

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String toString() {
		return "MessageQueue [userUuid=" + userUuid + ", createdOn=" + createdOn + ", content=" + content
				+ ", contentType=" + contentType + ", conversation=" + conversation + ", push=" + push
				+ ", createdBy=" + createdBy + "]";
	}

}
