/*
 * Parodos Workflow Service API
 * This is the API documentation for the Parodos Workflow Service. It provides operations to execute assessments to determine infrastructure options (tooling + environments). Also executes infrastructure task workflows to call downstream systems to stand-up an infrastructure option.
 *
 * The version of the OpenAPI document: v1.0.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.redhat.parodos.sdk.api;

import com.redhat.parodos.sdk.api.ApiException;
import com.redhat.parodos.sdk.model.WorkFlowDefinitionResponseDTO;
import org.junit.Test;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for WorkflowDefinitionApi
 */
@Ignore
public class WorkflowDefinitionApiTest {

	private final WorkflowDefinitionApi api = new WorkflowDefinitionApi();

	/**
	 * Returns information about a workflow definition by id
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void getWorkFlowDefinitionByIdTest() throws ApiException {
		String id = null;
		WorkFlowDefinitionResponseDTO response = api.getWorkFlowDefinitionById(id);
		// TODO: test validations
	}

	/**
	 * Returns a list of workflow definition
	 * @throws ApiException if the Api call fails
	 */
	@Test
	public void getWorkFlowDefinitionsTest() throws ApiException {
		String name = null;
		List<WorkFlowDefinitionResponseDTO> response = api.getWorkFlowDefinitions(name);
		// TODO: test validations
	}

}
