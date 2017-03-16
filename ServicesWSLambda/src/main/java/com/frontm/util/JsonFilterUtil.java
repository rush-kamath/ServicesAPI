package com.frontm.util;

import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.frontm.domain.FrontMFilter;
import com.frontm.exception.FrontMException;
import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;

import net.minidev.json.JSONArray;

public class JsonFilterUtil {
	private static final Logger logger = Logger.getLogger(JsonFilterUtil.class);
	private static final String ERROR_TYPE = "Json filter error: ";

	private static final String REL_OP_AND = "&&";
	private static final String REL_OP_OR = "||";

	private static final String LOG_OP_EQ = "==";
	private static final String LOG_OP_NEQ = "!=";
	private static final String LOG_OP_LT = "<";
	private static final String LOG_OP_LTE = "<=";
	private static final String LOG_OP_GT = ">";
	private static final String LOG_OP_GTE = ">=";
	private static final String LOG_OP_IN = "IN";
	private static final String LOG_OP_NIN = "NOTIN";
	private static final String LOG_OP_EXISTS = "EXISTS";
	private static final String LOG_OP_NOTEXISTS = "NOTEXISTS";

	private static final char DOT_SEPERATOR = '.';
	private static final String IN_CLAUSE_SEPERATOR = ",";

	/**
	 * The filter input is a series of filter expressions 2 filter expressions
	 * are separated either by && or || The logical operators are ==, !=,
	 * <, <=, >, >=, IN, NOTIN, EXISTS, NOTEXISTS The LHS of a filter is as
	 * follows: ServiceName.RootElementName.<attribute>. Followed by "." and
	 * optional functions like length(). The RHS of the filter will be ->
	 * ignored for EXISTS and NOTEXISTS operators -> parsed to integer for
	 * <, <=, > and >= operators -> separated by commas for IN and NOTIN
	 * operators. -> parsed as string for == and != operators
	 * 
	 * If any expression is unbalanced, an exception is thrown
	 */
	public static String filterJson(String inputJson, String filterInput) throws FrontMException {
		try {
			long startMS = System.currentTimeMillis();
			FrontMFilter filter = parseFilter(filterInput);
			logger.info(filter + "Time taken to parse: " + (System.currentTimeMillis() - startMS));

			startMS = System.currentTimeMillis();
			JSONArray filteredResult = JsonPath.parse(inputJson).read(filter.getRootElementPath(), filter.getFilter());

			ObjectMapper m = new ObjectMapper();
			ObjectNode objectNode = m.createObjectNode();
			objectNode.set(filter.getRootElement(), m.readTree(filteredResult.toJSONString()));

			final String finalJson = m.writeValueAsString(objectNode);
			logger.info("Filtered Results: " + filteredResult.size() + " : " + finalJson);
			logger.info("Time taken to filter: " + (System.currentTimeMillis() - startMS));
			return finalJson;

		} catch (FrontMException e) {
			logger.error("FrontMException while filtering json: " + e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error(e);
			final String errorMessage = e.getMessage();
			logger.error("FrontMException while filtering json: " + errorMessage);
			throw new FrontMException(ERROR_TYPE + errorMessage);
		}
	}

	private static FrontMFilter parseFilter(String filterInput) throws FrontMException {
		FrontMFilter frontMFilter = new FrontMFilter();
		Filter jsonPathFilter = filter(where("$").exists(true));
		final int inputLen = filterInput.length();

		int curPosition = 0;
		String nextRelOp = REL_OP_AND;
		boolean isServiceAndRootElemNull = true;
		while (curPosition < inputLen) {
			int orIndex = getIndexOf(filterInput, REL_OP_OR, curPosition, inputLen);
			int andIndex = getIndexOf(filterInput, REL_OP_AND, curPosition, inputLen);
			int newPos = (orIndex < andIndex) ? orIndex : andIndex;

			final String filterExpression = filterInput.substring(curPosition, newPos).trim();
			if (filterExpression.isEmpty()) {
				throw new FrontMException(ERROR_TYPE + "Invalid filter input. Unbalanced && and || operations");
			}

			if (isServiceAndRootElemNull) {
				populateServiceAndRootElem(frontMFilter, filterExpression);
				isServiceAndRootElemNull = false;
			}

			String finalFilterExpr = filterExpression.substring(frontMFilter.getServAndRootElem().length(),
					filterExpression.length());
			jsonPathFilter = addFilterCondition(jsonPathFilter, nextRelOp, finalFilterExpr);

			nextRelOp = (orIndex < andIndex) ? REL_OP_OR : REL_OP_AND;
			curPosition = newPos + 2;
		}

		frontMFilter.setFilter(jsonPathFilter);
		return frontMFilter;
	}

	private static int getIndexOf(String filterInput, String operator, int curPosition, int maxLength) {
		int index = filterInput.indexOf(operator, curPosition);
		return (index == -1) ? maxLength : index;
	}

	private static void populateServiceAndRootElem(FrontMFilter frontMFilter, String filterExpression) {
		final int firstDotIndex = filterExpression.indexOf(DOT_SEPERATOR);
		int secondDotIndex = filterExpression.indexOf(DOT_SEPERATOR, firstDotIndex + 1);
		String rootElemName = filterExpression.substring(firstDotIndex + 1, secondDotIndex);

		frontMFilter.setServAndRootElem(filterExpression.substring(0, secondDotIndex + 1));
		frontMFilter.setRootElementPath("$." + rootElemName + "[?]");
		frontMFilter.setRootElement(rootElemName);
	}

	private static Filter addFilterCondition(Filter filter, String relOp, final String filterExpr)
			throws FrontMException {
		switch (relOp) {
		case REL_OP_AND:
			filter = filter.and(getFilterCriteria(filterExpr));
			break;
		case REL_OP_OR:
			filter = filter.or(getFilterCriteria(filterExpr));
			break;
		}
		return filter;
	}

	private static Predicate getFilterCriteria(String filterExpression) throws FrontMException {
		String filterOperator = getLogicalOperator(filterExpression);
		String lhs = filterExpression.substring(0, filterExpression.indexOf(filterOperator)).trim();
		String rhs = filterExpression.substring(filterExpression.indexOf(filterOperator) + filterOperator.length(),
				filterExpression.length()).trim();

		Criteria criteria = where(lhs);
		switch (filterOperator) {
		case LOG_OP_EQ:
			criteria.is(rhs);
			break;
		case LOG_OP_NEQ:
			criteria.ne(rhs);
			break;
		case LOG_OP_LT:
			criteria.lt(Integer.parseInt(rhs));
			break;
		case LOG_OP_LTE:
			criteria.lte(Integer.parseInt(rhs));
			break;
		case LOG_OP_GT:
			criteria.gt(Integer.parseInt(rhs));
			break;
		case LOG_OP_GTE:
			criteria.gte(Integer.parseInt(rhs));
			break;
		case LOG_OP_IN:
			criteria.in(getInClauseRhs(rhs));
			break;
		case LOG_OP_NIN:
			criteria.nin(getInClauseRhs(rhs));
			break;
		case LOG_OP_EXISTS:
			criteria.exists(true);
			break;
		case LOG_OP_NOTEXISTS:
			criteria.exists(false);
			break;
		}

		return criteria;
	}

	private static List<String> getInClauseRhs(String rhs) {
		return Arrays.stream(rhs.split(IN_CLAUSE_SEPERATOR)).map(String::trim).collect(Collectors.toList());
	}

	/*
	 * Matches the first logical operator. If the expression contains more than
	 * one, the remaining expressions will be considered part of the rhs. for
	 * ex. a<b - lhs = a, oper = <, rhs = b a==<b - lhs = a oper = '==' rhs =
	 * '<b' exception thrown if the expression does not have any of the defined
	 * logical operators.
	 */
	private static String getLogicalOperator(String filterExpression) throws FrontMException {
		try {
			// filtering is done 2 times. since if the operator is <=, it can
			// match both the < and <= operators
			// the logic below filters on <= first and then <
			final List<String> firstOpList = Arrays.asList(LOG_OP_EQ, LOG_OP_GTE, LOG_OP_LTE, LOG_OP_NEQ, LOG_OP_NIN,
					LOG_OP_NOTEXISTS);
			final Optional<String> filter = firstOpList.stream().filter(filterExpression::contains).findAny();

			if (filter.isPresent()) {
				return filter.get();
			}

			final List<String> secondOpList = Arrays.asList(LOG_OP_GT, LOG_OP_LT, LOG_OP_IN, LOG_OP_EXISTS);
			return secondOpList.stream().filter(filterExpression::contains).findAny().get();
		} catch (Exception e) {
			throw new FrontMException(ERROR_TYPE + "Invalid filter input. Unbalanced logical operation in expression: "
					+ filterExpression);
		}
	}
}
