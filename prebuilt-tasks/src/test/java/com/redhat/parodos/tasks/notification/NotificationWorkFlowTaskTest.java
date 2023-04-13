package com.redhat.parodos.tasks.notification;

import com.redhat.parodos.tasks.tibco.TibcoWorkFlowTask;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import org.junit.Test;

import javax.jms.JMSException;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class NotificationWorkFlowTaskTest {

    private final static NotificationWorkFlowTask task = new NotificationWorkFlowTask("http://localhost:8080");
    @Test
    public void testExecute() {
        WorkContext ctx = getWorkContext();
        WorkReport result = task.execute(ctx);
    }

    private WorkContext getWorkContext() {
        WorkContext ctx = new WorkContext();
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "test-type");
        map.put("body", "test-body");
        map.put("subject", "test-subject");
        map.put("userNames", "test-user-1;test-user-2");
        map.put("groupNames", "test-group-1;test-group-2");

        WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION, task.getName(),
                WorkContextDelegate.Resource.ARGUMENTS, map);
        return ctx;
    }
}
