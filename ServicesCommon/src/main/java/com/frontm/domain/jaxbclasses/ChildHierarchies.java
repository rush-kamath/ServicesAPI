package com.frontm.domain.jaxbclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "childHierarchies")
public class ChildHierarchies extends FrontMDBCacheItem {
	@XmlElement(name = "childHierarchy", namespace = "http://sdp.cisco.com/oss/billing/entity/data")
	public List<ChildHierarchy> childHierarchy;

	@Override
	public List<Map<String, String>> getFlatHierarchy() {
		List<Map<String, String>> flatHierarchies = new ArrayList<>();
		populateHierarchyList(flatHierarchies, childHierarchy);
		return flatHierarchies;
	}
	
	private void populateHierarchyList(List<Map<String, String>> flatHierarchies,
			List<ChildHierarchy> childHierarchyList) {
		if (childHierarchyList == null) {
			return;
		}
		for (ChildHierarchy hierarchy : childHierarchyList) {
			final Map<String, String> map = hierarchy.toMap();
			addInstanceIdToMap(map);
			flatHierarchies.add(map);
			if (hierarchy.childrenHierarchy != null) {
				populateHierarchyList(flatHierarchies, hierarchy.childrenHierarchy.childHierarchy);
			}
		}
	}

	@Override
	public String toString() {
		return "ChildHierarchies [childHierarchy=" + childHierarchy + "]";
	}
}

@XmlRootElement(name = "childHierarchy")
class ChildHierarchy {
	@XmlElement(name = "name")
	String name;
	@XmlElement(name = "type")
	String type;
	@XmlElement(name = "id")
	String id;
	@XmlElement(name = "depth")
	String depth;

	@XmlElement(name = "attributeList")
	AttributeList attributeList;

	@XmlElement(name = "childrenHierarchy")
	public ChildrenHierarchy childrenHierarchy;

	@Override
	public String toString() {
		return "ChildHierarchy [name=" + name + ", type=" + type + ", id=" + id + ", depth=" + depth
				+ ", attributeList=" + attributeList + ", childrenHierarchy=" + childrenHierarchy + "]";
	}

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<>();
		map.put("id", id);
		map.put("type", type);
		map.put("name", name);
		map.put("depth", depth);
		map.putAll(attributeList.toMap());

		return map;
	}
}

class ChildrenHierarchy {
	@XmlElement(name = "childHierarchy")
	public List<ChildHierarchy> childHierarchy;

	@Override
	public String toString() {
		return childHierarchy.toString();
	}
}

class Attribute {
	@XmlElement(name = "key")
	String key;
	@XmlElement(name = "value")
	String value;

	@Override
	public String toString() {
		return key + "=" + value;
	}
}

class AttributeList {
	@XmlElement(name = "attribute")
	List<Attribute> attributeList;

	@Override
	public String toString() {
		return attributeList.toString();
	}

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<>();
		for (Attribute attribute : attributeList) {
			if (attribute.value == null) {
				continue;
			}
			map.put(attribute.key, attribute.value);
		}
		return map;
	}
}
