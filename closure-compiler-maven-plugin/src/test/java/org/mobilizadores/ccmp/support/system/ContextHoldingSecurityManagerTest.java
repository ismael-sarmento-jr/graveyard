package org.mobilizadores.ccmp.support.system;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class ContextHoldingSecurityManagerTest {

	static ContextHoldingSecurityManager chsm = ContextHoldingSecurityManager.getInstance();

	@BeforeClass
	public static void config() {
		System.setSecurityManager(chsm);		
	}
	
	@Before
	public void setUp() {
		chsm.disableExit();
	}
	
	@After
	public void tearDown() {
		chsm.enableExit();
	}
	
	@Test
	public void shouldNotAllowExit_attemptFromExternalThread() {
		boolean exceptionThrown = false;
		try {
			System.exit(0);
		} catch (SystemExitNotAllowedException e) {
			exceptionThrown = true;
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		Assert.assertTrue(exceptionThrown);
	}
	
	@Test
	public void shouldNotAllowExit_attemptFromInternalThread() {
		boolean exceptionThrown = false;
		Thread currentThread = Thread.currentThread();
		String originalName = currentThread.getName();
		try {
			currentThread.setName(JscompGroupThreadFactory.JSCOMP_PREFIX);
			System.exit(0);
		} catch (CompilerExitAttemptException e) {
			exceptionThrown = true;
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		} finally {
			currentThread.setName(originalName);
		}
		Assert.assertTrue(exceptionThrown);
	}
	
}

