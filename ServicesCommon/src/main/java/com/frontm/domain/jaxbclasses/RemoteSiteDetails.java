package com.frontm.domain.jaxbclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import static com.frontm.util.StringUtil.isEmpty;

@XmlRootElement(name = "queryResponse", namespace = "http://sdp.cisco.com/oss/common")
public class RemoteSiteDetails extends FrontMDBCacheItem {
	@XmlElement(name = "responseList")
	ResponseList responseList;
	
	@Override
	public String toString() {
		return responseList.toString();
	}
	
	static class ResponseList {
		@XmlElement(name = "entity")
		InvObjectTree entity;

		@Override
		public String toString() {
			return  entity.toString();
		}
	}

	static class InvObjectTree {
		@XmlElement(name = "id")
		String id;
		@XmlElement(name = "name")
		String name;
		@XmlElement(name = "invClass")
		String invClass;
		@XmlElement(name = "depth")
		String depth;
		@XmlElement(name = "attributeList")
		AttributeList attributeList;
		@XmlElement(name = "childList")
		ChildList childList;
	
		@Override
		public String toString() {
			return "InvObjectTree [id=" + id + ", name=" + name + ", invClass=" + invClass + ", depth=" + depth
					+ ", attributeList=" + attributeList + ", childList=" + childList + "]";
		}
		
		public Map<String, String> toMap() {
			Map<String, String> map = new HashMap<>();
			map.put("id", id);
			map.put("invClass", invClass);
			map.put("name", name);
			map.put("depth", depth);
			if(attributeList != null) {
				map.putAll(attributeList.toMap());
			}
			return map;
		}
	}

	static class ChildList {
		@XmlElement(name = "invObjectTree")
		List<InvObjectTree> childList;

		@Override
		public String toString() {
			return childList.toString();
		}
	}

	static class AttributeList {
		@XmlElement(name = "attribute")
		List<Attribute> attributeList;

		@Override
		public String toString() {
			return attributeList.toString();
		}

		public Map<? extends String, ? extends String> toMap() {
			Map<String, String> map = new HashMap<>();
			for(Attribute attribute : attributeList) {
				if(attribute.value == null || attribute.displayName == null) {
					continue;
				}
				map.put(attribute.displayName, attribute.value);
			}
			return map;
		}
	}

	static class Attribute {
		@XmlElement(name = "displayName")
		String displayName;
		@XmlElement(name = "name")
		String name;
		@XmlElement(name = "value")
		String value;

		@Override
		public String toString() {
			return displayName + "=" + value;
		}
	}

	@Override
	public List<Map<String, String>> getFlatHierarchy() {
		List<Map<String, String>> flatHierarchy = new ArrayList<>();
		if(this.responseList != null && this.responseList.entity != null) {
			populateFlatHierarchy(flatHierarchy, this.responseList.entity, null);
		}
		return flatHierarchy;
	}
	
	private void populateFlatHierarchy(List<Map<String, String>> flatHierarchy, InvObjectTree objectTree, String parentId) {
		final Map<String, String> map = objectTree.toMap();
		map.put("parentId", parentId);
		addInstanceIdToMap(map);
		flatHierarchy.add(map);
		
		if(objectTree.childList == null) {
			return;
		}
		final List<InvObjectTree> childList = objectTree.childList.childList;
		if(isEmpty(childList)) {
			return;
		}
		
		childList.forEach(invObj -> populateFlatHierarchy(flatHierarchy, invObj, objectTree.id));
	}
}
