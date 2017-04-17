package com.frontm.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.frontm.domain.jaxbclasses.FrontMDBCacheItem;
import com.frontm.exception.FrontMException;

public class JaxbParserUtil {
	private static final Logger logger = Logger.getLogger(JaxbParserUtil.class);

	public static List<Map<String, String>> parseXMLToDBCacheItems(String fileContents, String jaxbClassName, String instanceId)
			throws FrontMException {

		try {
			final Long startMillis = System.currentTimeMillis();

			final Class<? extends FrontMDBCacheItem> dbCacheItemClass = getDBCacheClass(jaxbClassName);
			final JAXBContext jaxbContext = JAXBContext.newInstance(dbCacheItemClass);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			final InputStream in = new ByteArrayInputStream(fileContents.getBytes());
			final FrontMDBCacheItem topLevelHierarchy = (FrontMDBCacheItem) unmarshaller.unmarshal(in);
			topLevelHierarchy.setInstanceId(instanceId);
			final List<Map<String, String>> flatHierarchies = topLevelHierarchy.getFlatHierarchy();

			logger.info("Time to parse xml: " + (System.currentTimeMillis() - startMillis));
			return flatHierarchies;
		} catch (FrontMException e) {
			throw e;
		} catch (Exception e) {
			throw new FrontMException("Unable to parse xml using jaxb. Error: " + e.getMessage());
		}
	}

	private static Class<? extends FrontMDBCacheItem> getDBCacheClass(String jaxbClassName) throws FrontMException {
		Class<?> jaxbClass = null;
		try {
			jaxbClass = Class.forName(jaxbClassName);
		} catch (ClassNotFoundException e) {
			throw new FrontMException(
					" Unable to load jaxb class: " + jaxbClassName + ". Error message:" + e.getMessage());
		}

		final Class<FrontMDBCacheItem> dbCacheClass = FrontMDBCacheItem.class;
		if (!dbCacheClass.isAssignableFrom(jaxbClass)) {
			throw new FrontMException(jaxbClass + " should be a subclass of " + dbCacheClass);
		}
		final Class<? extends FrontMDBCacheItem> dbCacheItemClass = jaxbClass.asSubclass(dbCacheClass);
		return dbCacheItemClass;
	}
}
