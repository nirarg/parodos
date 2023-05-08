package com.redhat.parodos.tasks.migrationtoolkit;

import com.redhat.parodos.workflow.exception.MissingParameterException;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.APP_ID;
import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.APP_NAME;
import static com.redhat.parodos.tasks.migrationtoolkit.TestConsts.REPO_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateApplicationTaskTest {

	CreateApplicationTask underTest;

	@Mock
	MTAApplicationClient mockClient;

	WorkContext ctx;

	@Before
	public void setUp() {
		underTest = new CreateApplicationTask();
		underTest.mtaClient = mockClient;
		ctx = new WorkContext();
	}

	@Test
	@SneakyThrows
	public void missingMandatoryParameters() {
		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isInstanceOf(MissingParameterException.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(execute.getWorkContext().get("application")).isNull();
		verify(mockClient, Mockito.times(0)).create(any());
	}

	@Test
	@SneakyThrows
	public void createFails() {
		when(mockClient.create(any(App.class))).thenReturn(new Result.Failure<>(new Exception("some error from MTA")));
		ctx.put("applicationName", APP_NAME);
		ctx.put("repositoryURL", REPO_URL);

		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isInstanceOf(Exception.class);
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.FAILED);
		assertThat(execute.getWorkContext().get("application")).isNull();
		verify(mockClient, Mockito.times(1)).create(any());
	}

	@Test
	@SneakyThrows
	public void createCompletes() {
		ctx.put("applicationName", APP_NAME);
		ctx.put("repositoryURL", REPO_URL);

		when(mockClient.create(any()))
				.thenReturn(new Result.Success<>(new App(APP_ID, APP_NAME, new Repository("git", REPO_URL))));

		WorkReport execute = underTest.execute(ctx);

		assertThat(execute.getError()).isNull();
		assertThat(execute.getStatus()).isEqualTo(WorkStatus.COMPLETED);
		assertThat(execute.getWorkContext().get("application"))
				.isEqualTo(new App(APP_ID, APP_NAME, new Repository("git", REPO_URL)));

		// 0 is wanted explicitly because it is an empty ID for the server request. (IDs
		// are generated by the server)
		verify(mockClient, Mockito.times(1)).create(new App(0, APP_NAME, new Repository("git", REPO_URL)));
	}

}