package com.redhat.parodos.tasks.azure;

import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.azure.resourcemanager.AzureResourceManager;
import org.mockito.invocation.InvocationOnMock;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureCreateVirtualMachineTaskTest {


    private static WorkContext ctx;
    private static AzureResourceManager azureResourceManager;

    // Define all Azure resources mocks
    private static AzureCreateVirtualMachineTask underTest;
    private static ResourceGroups resourceGroups;
    private static final String[] requiredParamsKeys = {AzureCreateVirtualMachineTask.VM_USER_NAME_KEY, AzureCreateVirtualMachineTask.VM_SSH_PUBLIC_KEY_KEY,
            AzureCreateVirtualMachineTask.AZURE_TENANT_ID_KEY, AzureCreateVirtualMachineTask.AZURE_SUBSCRIPTION_ID_KEY, AzureCreateVirtualMachineTask.AZURE_CLIENT_ID_KEY,
            AzureCreateVirtualMachineTask.AZURE_CLIENT_SECRET_KEY, AzureCreateVirtualMachineTask.AZURE_RESOURCES_PREFIX_KEY};
    @BeforeEach
    public void setUp() throws Exception {
        // Initiate all Azure resources mocks
        azureResourceManager = mock(AzureResourceManager.class);
        resourceGroups = mock(ResourceGroups.class);

        underTest = new AzureCreateVirtualMachineTask(azureResourceManager);
        underTest.setBeanName("AzureCreateVirtualMachineTask");
        ctx = new WorkContext();

        HashMap<String, String> map = new HashMap<>();
        for (String paramKey : requiredParamsKeys) {
            map.put(paramKey, paramKey + "-testValue");
            WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, underTest.getName(),
                    WorkContextDelegate.Resource.ARGUMENTS, map);
        }
    }

    @Test
    public void testErr() {
        WorkReport result = underTest.execute(ctx);
        when(azureResourceManager.resourceGroups()).thenReturn(resourceGroups);
        when(resourceGroups.define(AzureCreateVirtualMachineTask.AZURE_RESOURCES_PREFIX_KEY + "-testValueResourceGroup")).thenAnswer(new Answer<T>() {
            public T answer(InvocationOnMock invocation) throws Throwable {
                T value = (T) invocation.getArguments()[0];

                return value;
            }
        });
    }

    @Test
    public void testMissingRequiredParamErr() {
        WorkContext ctx = new WorkContext();
        HashMap<String, String> map = new HashMap<>();

        for (String paramKey : requiredParamsKeys) {
            WorkReport result = underTest.execute(ctx);
            assertEquals(WorkStatus.FAILED, result.getStatus());
            assertEquals(MissingParameterException.class, result.getError().getClass());
            assertEquals("missing parameter(s) for ParameterName: " + paramKey, result.getError().getMessage());

            map.put(paramKey, paramKey + "-testValue");
            WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, underTest.getName(),
                    WorkContextDelegate.Resource.ARGUMENTS, map);
        }
    }

}
