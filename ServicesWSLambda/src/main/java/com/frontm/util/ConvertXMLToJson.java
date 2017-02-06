package com.frontm.util;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ConvertXMLToJson {
	private static final Logger logger = Logger.getLogger(ConvertXMLToJson.class);
	
	public static ObjectNode convert(String xmlString, JsonNode mapping, String serviceName) throws Exception {
		try {
			logger.info("mapping: " + Jackson.toJsonPrettyString(mapping));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
			
			XPath xPath = XPathFactory.newInstance().newXPath();

			Iterator<Entry<String, JsonNode>> inJsonFields = mapping.fields();
			ObjectMapper objMapper = new ObjectMapper();
			ObjectNode rootNode = createJsonNode(doc, xPath, inJsonFields, objMapper);
			
			final ObjectNode serviceNode = objMapper.createObjectNode();
			serviceNode.set(serviceName, rootNode);

			logger.info("serviceNode: " + Jackson.toJsonPrettyString(serviceNode));
			return serviceNode;
		} catch (Exception e) {
			logger.error("Error occured while converting XML to JSON", e);
			throw e;
		}
	}

	private static ObjectNode createJsonNode(Object xmlNode, XPath xPath,
			final Iterator<Entry<String, JsonNode>> inJsonFields, ObjectMapper objMapper)
			throws XPathExpressionException {
		final ObjectNode outParentNode = objMapper.createObjectNode();
		while (inJsonFields.hasNext()) {
			final Entry<String, JsonNode> entry = inJsonFields.next();
			final String xmlNodePath = entry.getKey();
			final String finalNodeName = parseNodeName(xmlNode, xPath, xmlNodePath);

			final JsonNode inJsonField = entry.getValue();
			addFieldToOutParentNode(xmlNode, xPath, xmlNodePath, inJsonField, finalNodeName, outParentNode, objMapper);
		}
		return outParentNode;
	}

	private static void addFieldToOutParentNode(Object xmlNode, XPath xPath, String xmlNodePath, JsonNode inJsonNode,
			String finalNodeName, ObjectNode outParentNode, ObjectMapper objMapper) throws XPathExpressionException {
		switch (inJsonNode.getNodeType()) {
		case STRING:
			String nodeValue = xPath.compile(inJsonNode.textValue()).evaluate(xmlNode);
			outParentNode.put(finalNodeName, nodeValue);
			break;
		case ARRAY:
			NodeList nodeList = (NodeList) xPath.compile(xmlNodePath).evaluate(xmlNode, XPathConstants.NODESET);
			ArrayNode arrayNode = objMapper.createArrayNode();
			if (nodeList != null) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					final Element item = (Element) nodeList.item(i);
					if (item == null) {
						continue;
					}
					for (final JsonNode objNode : inJsonNode) {
						arrayNode.add(createJsonNode(item, xPath, objNode.fields(), objMapper));
					}
				}
			}
			outParentNode.set(finalNodeName, arrayNode);
			break;
		case OBJECT:
			Node childNode = (Node) xPath.compile(xmlNodePath).evaluate(xmlNode, XPathConstants.NODE);
			if (childNode != null) {
				outParentNode.set(finalNodeName, createJsonNode(childNode, xPath, inJsonNode.fields(), objMapper));
			}
			break;
		default:
			break;
		}
	}

	private static String parseNodeName(Object xmlNode, XPath xPath, String xmlNodePath)
			throws XPathExpressionException {
		if (!xmlNodePath.contains("/")) {
			return xmlNodePath;
		}

		String[] nodeNameSplit = xmlNodePath.split("/");
		String lastSplit = nodeNameSplit[nodeNameSplit.length - 1];
		if (xmlNodePath.endsWith("/")) {
			return xPath.compile(lastSplit).evaluate(xmlNode);
		} else if(xmlNodePath.startsWith("/")) {
			return nodeNameSplit[1];
		} else {
			return nodeNameSplit[0];
		}
	}
}
