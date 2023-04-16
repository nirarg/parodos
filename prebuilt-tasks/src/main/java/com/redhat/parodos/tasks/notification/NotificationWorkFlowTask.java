package com.redhat.parodos.tasks.notification;

import com.redhat.parodos.notification.sdk.api.ApiClient;
import com.redhat.parodos.notification.sdk.api.ApiException;
import com.redhat.parodos.notification.sdk.api.Configuration;
import com.redhat.parodos.notification.sdk.api.NotificationMessageApi;
import com.redhat.parodos.notification.sdk.model.NotificationMessageCreateRequestDTO;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflow.task.BaseWorkFlowTask;
import com.redhat.parodos.workflow.task.enums.WorkFlowTaskOutput;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import com.redhat.parodos.workflow.utils.CredUtils;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.springframework.http.HttpHeaders;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

@Slf4j
public class NotificationWorkFlowTask extends BaseWorkFlowTask {

	private final NotificationMessageApi apiInstance;

	public NotificationWorkFlowTask(String basePath) {
		this(basePath, null);
	}

	protected NotificationWorkFlowTask(String basePath, NotificationMessageApi apiInstance) {
		if (apiInstance == null) {
			ApiClient apiClient = Configuration.getDefaultApiClient();
			apiClient.addDefaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + CredUtils.getBase64Creds("test", "test"));
			apiClient.setBasePath(basePath); // basePath = "http://localhost:8080"
			apiInstance = new NotificationMessageApi(apiClient);
		}
		this.apiInstance = apiInstance;
	}

	@Override
	public @NonNull List<WorkFlowTaskParameter> getWorkFlowTaskParameters() {
		LinkedList<WorkFlowTaskParameter> params = new LinkedList<>();
		params.add(WorkFlowTaskParameter.builder().key("type").type(WorkFlowTaskParameterType.TEXT).optional(false)
				.description("Message Type").build());
		params.add(WorkFlowTaskParameter.builder().key("body").type(WorkFlowTaskParameterType.TEXT).optional(false)
				.description("Message Body").build());
		params.add(WorkFlowTaskParameter.builder().key("subject").type(WorkFlowTaskParameterType.TEXT).optional(false)
				.description("Message Subject").build());
		// TODO Add an option for List parameter
		params.add(WorkFlowTaskParameter.builder().key("userNames").type(WorkFlowTaskParameterType.TEXT).optional(true)
				.description("Comma separated list of user names").build());
		params.add(WorkFlowTaskParameter.builder().key("groupNames").type(WorkFlowTaskParameterType.TEXT).optional(true)
				.description("Comma separated list of group names").build());
		return params;
	}

	@Override
	public @NonNull List<WorkFlowTaskOutput> getWorkFlowTaskOutputs() {
		return super.getWorkFlowTaskOutputs();
	}

	@Override
	public HashMap<String, Map<String, Object>> getAsJsonSchema() {
		return super.getAsJsonSchema();
	}

	@Override
	public WorkReport execute(WorkContext workContext) {
		NotificationMessageCreateRequestDTO notificationMessageCreateRequestDTO = new NotificationMessageCreateRequestDTO();

		try {
			notificationMessageCreateRequestDTO.messageType(getRequiredParameterValue(workContext, "type"));
			notificationMessageCreateRequestDTO.body(getRequiredParameterValue(workContext, "body"));
			notificationMessageCreateRequestDTO.subject(getRequiredParameterValue(workContext, "subject"));
			List<String> userNames = toList(getOptionalParameterValue(workContext, "userNames", null));
			List<String> groupNames = toList(getOptionalParameterValue(workContext, "groupNames", null));
			if ((userNames == null || userNames.isEmpty()) && (groupNames == null || groupNames.isEmpty())) {
				throw new MissingParameterException("User Names or Group Names must be provided");
			}
			notificationMessageCreateRequestDTO.usernames(userNames);
			notificationMessageCreateRequestDTO.groupnames(groupNames);
		}
		catch (MissingParameterException e) {
			log.error("Exception when trying to get required parameter:", e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		try {
			this.apiInstance.create(notificationMessageCreateRequestDTO);
		}
		catch (ApiException e) {
			log.error("Exception when calling NotificationMessageApi#create:", e);
			return new DefaultWorkReport(WorkStatus.FAILED, workContext, e);
		}

		return new DefaultWorkReport(WorkStatus.COMPLETED, workContext);
	}

	private List<String> toList(String str) {
		if (str == null) {
			return null;
		}
		return Arrays.asList(str.split("\\s*;\\s*"));
	}

}
