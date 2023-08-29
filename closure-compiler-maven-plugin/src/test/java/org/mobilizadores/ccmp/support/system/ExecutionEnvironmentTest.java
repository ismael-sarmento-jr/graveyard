package org.mobilizadores.ccmp.support.system;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class ExecutionEnvironmentTest {

	ExecutionEnvironment env = new ExecutionEnvironment();
	boolean r1ran = false;
	boolean r2ran = false;
	
	@Test
	public void shouldWait_ForTasksToFinish() {
		Runnable r1 = () -> {
			try {
				Thread.sleep(2000);
				r1ran = true;
			} catch (InterruptedException e) {}
		};
		Runnable r2 = () -> {
			try {
				Thread.sleep(2000);
				r2ran = true;
			} catch (InterruptedException e) {}
		};
		
		try {
			this.env.getExecutorPoolService().execute(r1);
			this.env.getExecutorPoolService().execute(r2);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		this.env.awaitTasksTerminationAfterShutdown();
		Assert.assertTrue(r1ran);
		Assert.assertTrue(r2ran);
	}
}
