package org.thoughtslive.jenkins.plugins.jira.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintStream;

import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtslive.jenkins.plugins.jira.Site;
import org.thoughtslive.jenkins.plugins.jira.api.Comment;
import org.thoughtslive.jenkins.plugins.jira.api.ResponseData;
import org.thoughtslive.jenkins.plugins.jira.api.ResponseData.ResponseDataBuilder;
import org.thoughtslive.jenkins.plugins.jira.service.JiraService;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Unit test cases for EditCommentStep class.
 * 
 * @author Naresh Rayapati
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({EditCommentStep.class, Site.class})
public class EditCommentStepTest {

  @Mock
  TaskListener taskListenerMock;
  @Mock
  Run<?, ?> runMock;
  @Mock
  EnvVars envVarsMock;
  @Mock
  PrintStream printStreamMock;
  @Mock
  JiraService jiraServiceMock;
  @Mock
  Site siteMock;
  @Mock
  StepContext contextMock;

  EditCommentStep.Execution stepExecution;

  @Before
  public void setup() throws IOException, InterruptedException {

    // Prepare site.
    when(envVarsMock.get("JIRA_SITE")).thenReturn("LOCAL");
    when(envVarsMock.get("BUILD_URL")).thenReturn("http://localhost:8080/jira-testing/job/01");

    PowerMockito.mockStatic(Site.class);
    Mockito.when(Site.get(any())).thenReturn(siteMock);
    when(siteMock.getService()).thenReturn(jiraServiceMock);

    when(runMock.getCauses()).thenReturn(null);
    when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
    doNothing().when(printStreamMock).println();

    final ResponseDataBuilder<Comment> builder = ResponseData.builder();
    when(jiraServiceMock.updateComment(anyString(), anyString(), anyString()))
        .thenReturn(builder.successful(true).code(200).message("Success").build());

    when(contextMock.get(Run.class)).thenReturn(runMock);
    when(contextMock.get(TaskListener.class)).thenReturn(taskListenerMock);
    when(contextMock.get(EnvVars.class)).thenReturn(envVarsMock);
  }

  @Test
  public void testWithZeroComponentIdThrowsAbortException() throws Exception {
    final EditCommentStep step = new EditCommentStep("TEST-1", "", "test comment");
    stepExecution = new EditCommentStep.Execution(step, contextMock);;

    // Execute and assert Test.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.run();
    }).withMessage("commentId is empty or null.").withStackTraceContaining("AbortException")
        .withNoCause();
  }

  @Test
  public void testWithNegativeComponentIdThrowsAbortException() throws Exception {
    final EditCommentStep step = new EditCommentStep("TEST-1", null, "test comment");
    stepExecution = new EditCommentStep.Execution(step, contextMock);;

    // Execute and assert Test.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.run();
    }).withMessage("commentId is empty or null.").withStackTraceContaining("AbortException")
        .withNoCause();
  }

  @Test
  public void testWithEmptyIdOrKeyThrowsAbortException() throws Exception {
    final EditCommentStep step = new EditCommentStep("", "1000", "test comment");
    stepExecution = new EditCommentStep.Execution(step, contextMock);;

    // Execute and assert Test.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.run();
    }).withMessage("idOrKey is empty or null.").withStackTraceContaining("AbortException")
        .withNoCause();
  }

  @Test
  public void testWithEmptyCommentThrowsAbortException() throws Exception {
    final EditCommentStep step = new EditCommentStep("TEST-1", "1000", "");
    stepExecution = new EditCommentStep.Execution(step, contextMock);;

    // Execute and assert Test.
    assertThatExceptionOfType(AbortException.class).isThrownBy(() -> {
      stepExecution.run();
    }).withMessage("comment is empty or null.").withStackTraceContaining("AbortException")
        .withNoCause();
  }

  @Test
  public void testSuccessfulEditComment() throws Exception {
    final EditCommentStep step = new EditCommentStep("TEST-1", "1000", "test comment");
    stepExecution = new EditCommentStep.Execution(step, contextMock);;

    // Execute Test.
    stepExecution.run();

    // Assert Test
    verify(jiraServiceMock, times(1)).updateComment("TEST-1", "1000",
        "test comment");
    assertThat(step.isFailOnError()).isEqualTo(true);
  }
}
