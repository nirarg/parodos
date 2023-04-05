package com.redhat.parodos.examples.ocponboarding.task;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.ocponboarding.task.dto.email.MessageRequestDTO;
import com.redhat.parodos.examples.utils.RestUtils;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Jira Ticket Email Notification Task execution test
 *
 * @author Annel Ketcha (GitHub: anludke)
 */
public class JiraTicketEmailNotificationWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static final String ISSUE_LINK = "ISSUE_LINK";

	private static final String mailServiceUrl = "mail-service-url-test";

	private static final String jiraTicketUrl = "jira-ticket-url-test";

	private JiraTicketEmailNotificationWorkFlowTask jiraTicketEmailNotificationWorkFlowTask;

	@Before
	public void setUp() {
		this.jiraTicketEmailNotificationWorkFlowTask = spy(
				(JiraTicketEmailNotificationWorkFlowTask) getConcretePersonImplementation());

		try {
			doReturn(jiraTicketUrl).when(this.jiraTicketEmailNotificationWorkFlowTask)
					.getRequiredParameterValue(Mockito.any(WorkContext.class), eq(ISSUE_LINK));
		}
		catch (MissingParameterException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getConcretePersonImplementation() {
		return new JiraTicketEmailNotificationWorkFlowTask(mailServiceUrl);
	}

	@Test
	public void executeSuccess() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);
		MessageRequestDTO messageRequestDTO = Mockito.mock(MessageRequestDTO.class);
		HttpEntity<MessageRequestDTO> requestEntity = new HttpEntity<>(messageRequestDTO);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = Mockito.mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(() -> RestUtils.executePost(eq(mailServiceUrl), eq(requestEntity)))
					.thenReturn(ResponseEntity.ok("Mail Sent"));

			// when
			WorkReport workReport = jiraTicketEmailNotificationWorkFlowTask.execute(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

	@Test
	public void executeFail() {
		// given
		WorkContext workContext = Mockito.mock(WorkContext.class);
		MessageRequestDTO messageRequestDTO = Mockito.mock(MessageRequestDTO.class);
		HttpEntity<MessageRequestDTO> requestEntity = new HttpEntity<>(messageRequestDTO);
		try (MockedStatic<RestUtils> restUtilsMockedStatic = Mockito.mockStatic(RestUtils.class)) {
			restUtilsMockedStatic.when(() -> RestUtils.executePost(eq(mailServiceUrl), eq(requestEntity)))
					.thenReturn(ResponseEntity.internalServerError().build());

			// when
			WorkReport workReport = jiraTicketEmailNotificationWorkFlowTask.execute(workContext);

			// then
			assertEquals(WorkStatus.FAILED, workReport.getStatus());
		}
	}

	@Test
	public void testGetWorkFlowTaskParameters() {
		// when
		List<WorkFlowTaskParameter> workFlowTaskParameters = jiraTicketEmailNotificationWorkFlowTask
				.getWorkFlowTaskParameters();

		// then
		assertNotNull(workFlowTaskParameters);
		assertEquals(0, workFlowTaskParameters.size());
	}

	@Test
	public void testGetWorkFlowTaskOutputs() {
		// when
		List<WorkFlowTaskOutput> workFlowTaskOutputs = jiraTicketEmailNotificationWorkFlowTask.getWorkFlowTaskOutputs();

		// then
		assertNotNull(workFlowTaskOutputs);
		assertEquals(2, workFlowTaskOutputs.size());
		assertEquals(WorkFlowTaskOutput.EXCEPTION, workFlowTaskOutputs.get(0));
		assertEquals(WorkFlowTaskOutput.OTHER, workFlowTaskOutputs.get(1));
	}

}