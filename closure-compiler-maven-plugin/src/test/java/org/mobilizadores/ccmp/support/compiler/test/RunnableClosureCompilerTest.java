package org.mobilizadores.ccmp.support.compiler.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.mojo.test.BasePluginTestCase;
import org.mobilizadores.ccmp.support.compiler.RunnableClosureCompiler;
import org.mobilizadores.ccmp.support.notification.Notification;
import org.mobilizadores.ccmp.support.notification.TaskStatus;
import org.mobilizadores.ccmp.support.system.CompilerExitAttemptException;

@RunWith(BlockJUnit4ClassRunner.class)
public class RunnableClosureCompilerTest extends BasePluginTestCase implements Observer {

	String[] args = { "--js", "test1.js", "--js", "test2.js",
			"--externs", "extern1.js", "--js_output_file", "ouptput.js" };

	@Before
	public void setUp() {
		super.setUpEnvironment();
	}

	@Test(timeout = 3000)
	public void shouldRunSuccesfully_100ConcurrentCompressions_andNotifications() {
		CountDownLatch latch = new CountDownLatch(100);
		int threadsRun = 0;
		try {
			for (; threadsRun < 100; threadsRun++) {
				RunnableClosureCompiler spiedRcc = getSpiedRcc();
				super.executionEnvironment.execute(spiedRcc);
				latch.countDown();
			}
			latch.await(3, TimeUnit.SECONDS);
			Assert.assertEquals(100, threadsRun);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		super.executionEnvironment.awaitTasksTerminationAfterShutdown();
	}

	private RunnableClosureCompiler getSpiedRcc() {
		RunnableClosureCompiler rcc = new RunnableClosureCompiler(this.args, super.executionEnvironment.getLock(), System.out);
		RunnableClosureCompiler spiedRcc = spy(rcc);
		doThrow(new CompilerExitAttemptException(TaskStatus.SUCCESS.getCode())).when(spiedRcc)
				.runClosureCompiler(any());
		spiedRcc.addObserver(this);
		return spiedRcc;
	}
	
	@Override
	public void update(Observable o, Object arg) {		
		Notification notif = (Notification) arg;
		Assert.assertNotNull(notif);
		Assert.assertArrayEquals(this.args, notif.getArgs());
	}

}
