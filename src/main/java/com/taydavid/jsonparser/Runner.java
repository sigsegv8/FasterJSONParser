package com.taydavid.jsonparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletionException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taydavid.jsonparser.constants.Constants;

public class Runner {
	public static void main(String args[]) {

		byte[] jsonData = null;
		JsonNode rootNode = null;

		try {
			jsonData = Files.readAllBytes(Paths.get("res/report.json"));
			final ObjectMapper objectMapper = new ObjectMapper();
			rootNode = objectMapper.readTree(jsonData);
		} catch (IOException e) {
			throw new CompletionException(
					"IOException occured during reading of json file or bytestream of json file is invalid", e);
		}

		final JsonNode testSuitesNode = rootNode.path(Constants.TEST_SUITES_KEY);
		testSuitesNode.forEach(suite -> {
			final String suiteName = suite.get(Constants.SUITE_NAME_KEY).textValue();
			final JsonNode results = suite.get(Constants.RESULTS_KEY);

			final Map<String, String[]> passedTestCases = new TreeMap<>();
			final Map<String, String[]> failedTestCases = new TreeMap<>();

			int[] blockedTestCaseCount = { 0 };
			int[] timeExecutionGreaterThan10Count = { 0 };

			results.forEach(result -> {
				final String status = result.get(Constants.STATUS_KEY).textValue();
				final String executionTime = result.get(Constants.TIME_KEY).textValue();
				final String testName = result.get(Constants.TEST_NAME_KEY).textValue();
				if (StringUtils.equalsIgnoreCase(status, Constants.STATUS.PASS.name())) {
					passedTestCases.put(testName, new String[] { executionTime, status });
				} else if (StringUtils.equalsIgnoreCase(status, Constants.STATUS.FAIL.name())) {
					failedTestCases.put(testName, new String[] { executionTime, status });
				} else if (StringUtils.equalsIgnoreCase(status, Constants.STATUS.BLOCKED.name())) {
					blockedTestCaseCount[0]++;
				}

				if (!StringUtils.isEmpty(executionTime) && Double.valueOf(executionTime) > 10) {
					timeExecutionGreaterThan10Count[0]++;
				}
			});

			// Print test report to console
			System.out.println("SUITE NAME: " + suiteName);
			System.out.println("TOTAL PASSED: " + passedTestCases.size());
			passedTestCases.entrySet().forEach(entry -> System.out.println("Test Case Name: " + entry.getKey()
					+ "\nExecution Time: " + entry.getValue()[0] + "\nStatus: " + entry.getValue()[1] + "\n"));
			System.out.println("TOTAL FAILED: " + failedTestCases.size());
			failedTestCases.entrySet().forEach(entry -> System.out.println("\nTest Case Name: " + entry.getKey()
					+ "\nExecution Time: " + entry.getValue()[0] + "\nStatus: " + entry.getValue()[1] + "\n"));
			System.out.println("Total Test Cases Blocked: " + blockedTestCaseCount[0]);
			System.out.println("Total Test Cases Where Time Execution Duration is Greater Than 10 seconds: "
					+ timeExecutionGreaterThan10Count[0] + "\n");

		});
	}

}
