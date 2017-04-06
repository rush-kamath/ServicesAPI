package com.frontm.domain;

import com.jayway.jsonpath.Filter;

public class FrontMFilter {
	// all filter expressions start with serviceName.rootElementName.xxx
	private String servAndRootElem;
	private String rootElement;
	private Filter filter;
	private String rootElementPath;

	public String getServAndRootElem() {
		return servAndRootElem;
	}

	public void setServAndRootElem(String servAndRootElem) {
		this.servAndRootElem = servAndRootElem;
	}

	public String getRootElement() {
		return rootElement;
	}

	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public String getRootElementPath() {
		return rootElementPath;
	}

	public void setRootElementPath(String rootElementPath) {
		this.rootElementPath = rootElementPath;
	}

	@Override
	public String toString() {
		return "FrontMFilter [servAndRootElem=" + servAndRootElem + ", rootElement=" + rootElement + ", filter="
				+ filter + ", rootElementPath=" + rootElementPath + "]";
	}

}
