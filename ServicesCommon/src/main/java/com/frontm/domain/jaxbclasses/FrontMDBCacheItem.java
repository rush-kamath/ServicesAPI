package com.frontm.domain.jaxbclasses;

import java.util.List;
import java.util.Map;

public abstract class FrontMDBCacheItem {
	private String instanceId;

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	protected void addInstanceIdToMap(Map<String, String> map) {
		map.put("instanceId", instanceId);
	}

	public abstract List<Map<String, String>> getFlatHierarchy();
}
