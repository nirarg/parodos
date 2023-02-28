package com.redhat.parodos.workflow.execution.continuation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.parodos.workflow.WorkFlowStatus;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.execution.service.WorkFlowServiceImpl;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflow.task.WorkFlowTaskStatus;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Matches;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkFlowContinuationServiceTest {

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowRepository workFlowRepository;

	private WorkFlowTaskRepository workFlowTaskRepository;

	private WorkFlowServiceImpl workFlowService;

	private WorkFlowContinuationService service;

	@BeforeEach
	void initEach() {
		this.workFlowDefinitionRepository = Mockito.mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = Mockito.mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowRepository = Mockito.mock(WorkFlowRepository.class);
		this.workFlowTaskRepository = Mockito.mock(WorkFlowTaskRepository.class);
		this.workFlowService = Mockito.mock(WorkFlowServiceImpl.class);
		this.service = new WorkFlowContinuationService(this.workFlowDefinitionRepository,
				this.workFlowTaskDefinitionRepository, this.workFlowRepository, this.workFlowTaskRepository,
				this.workFlowService, new ObjectMapper());
	}

	@Test
	void workFlowSkipCompletedJobs() {
		// given
		WorkFlowExecution wf = this.sampleWorkFlowExecution();
		wf.setStatus(WorkFlowStatus.COMPLETED);

		WorkFlowExecution wfFailed = this.sampleWorkFlowExecution();
		wfFailed.setStatus(WorkFlowStatus.FAILED);

		Mockito.when(this.workFlowRepository.findAll()).thenReturn(List.of(wf, wfFailed));

		// when
		this.service.workFlowRunAfterStartup();

		// then
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findAll();
		Mockito.verify(this.workFlowService, Mockito.times(0)).execute(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
	}

	@Test
	void workFlowCompleteInProgress() {
		// given
		WorkFlowExecution wfExecution = this.sampleWorkFlowExecution();
		Mockito.when(this.workFlowRepository.findAll()).thenReturn(List.of(wfExecution));
		Mockito.when(this.workFlowDefinitionRepository.findById(Mockito.any()))
				.thenReturn(Optional.of(sampleWorkFlowDefinition("test")));

		// when
		this.service.workFlowRunAfterStartup();

		// then
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findAll();
		Mockito.verify(this.workFlowService, Mockito.times(1)).execute(
				Mockito.eq(wfExecution.getProjectId().toString()), Mockito.eq("test"),
				ArgumentMatchers.argThat(argument -> {
					if (argument instanceof HashMap) {
						return ((HashMap<?, ?>) argument).isEmpty();
					}
					return false;
				}), Mockito.anyMap());
	}

	@Test
	void workFlowCompleteWithTaskExecutions() {
		// given
		WorkFlowExecution wfExecution = this.sampleWorkFlowExecution();
		Mockito.when(this.workFlowRepository.findAll()).thenReturn(List.of(wfExecution));
		Mockito.when(this.workFlowDefinitionRepository.findById(Mockito.any()))
				.thenReturn(Optional.of(sampleWorkFlowDefinition("test")));

		WorkFlowTaskDefinition wfTaskDef = sampleWorkFlowTaskDefinition();
		Mockito.when(this.workFlowTaskDefinitionRepository.findById(Mockito.any())).thenReturn(Optional.of(wfTaskDef));
		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("{\"test\": \"test\"}")
				.results("res").status(WorkFlowTaskStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDef.getId())
				.workFlowExecutionId(wfExecution.getId()).build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		Mockito.when(this.workFlowTaskRepository.findByWorkFlowExecutionId(wfExecution.getId()))
				.thenReturn(List.of(workFlowTaskExecution));

		// when
		this.service.workFlowRunAfterStartup();

		// then
		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findAll();
		Mockito.verify(this.workFlowService, Mockito.times(1)).execute(
				Mockito.eq(wfExecution.getProjectId().toString()), Mockito.eq("test"),
				ArgumentMatchers.argThat(argument -> {
					if (argument instanceof HashMap) {
						return !((HashMap<?, ?>) argument).isEmpty();
					}
					return false;
				}), Mockito.anyMap());
	}

	@Test
	void workFlowCompleteWithInvalidJson() {
		// given
		WorkFlowExecution wfExecution = this.sampleWorkFlowExecution();
		Mockito.when(this.workFlowRepository.findAll()).thenReturn(List.of(wfExecution));
		Mockito.when(this.workFlowDefinitionRepository.findById(Mockito.any()))
				.thenReturn(Optional.of(sampleWorkFlowDefinition("test")));

		WorkFlowTaskDefinition wfTaskDef = sampleWorkFlowTaskDefinition();
		Mockito.when(this.workFlowTaskDefinitionRepository.findById(Mockito.any())).thenReturn(Optional.of(wfTaskDef));
		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("invalid")
				.results("res").status(WorkFlowTaskStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDef.getId())
				.workFlowExecutionId(wfExecution.getId()).build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		Mockito.when(this.workFlowTaskRepository.findByWorkFlowExecutionId(wfExecution.getId()))
				.thenReturn(List.of(workFlowTaskExecution));

		// when
		Exception exception = assertThrows(RuntimeException.class, () -> {
			this.service.workFlowRunAfterStartup();
		});

		// then
		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("JsonParseException"));

		Mockito.verify(this.workFlowRepository, Mockito.times(1)).findAll();
		Mockito.verify(this.workFlowService, Mockito.times(0)).execute(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any());
	}

	private WorkFlowExecution sampleWorkFlowExecution() {
		WorkFlowExecution wf = WorkFlowExecution.builder().projectId(UUID.randomUUID())
				.status(WorkFlowStatus.IN_PROGRESS).build();
		wf.setId(UUID.randomUUID());
		wf.setArguments("{\"test\": \"test\"}");
		return wf;
	}

	private WorkFlowTaskDefinition sampleWorkFlowTaskDefinition() {
		WorkFlowTaskDefinition wfTaskDef = WorkFlowTaskDefinition.builder().name("test").build();
		wfTaskDef.setId(UUID.randomUUID());
		return wfTaskDef;
	}

	private WorkFlowDefinition sampleWorkFlowDefinition(String name) {
		WorkFlowDefinition wf = WorkFlowDefinition.builder().name(name).build();
		wf.setId(UUID.randomUUID());
		return wf;
	}

}
