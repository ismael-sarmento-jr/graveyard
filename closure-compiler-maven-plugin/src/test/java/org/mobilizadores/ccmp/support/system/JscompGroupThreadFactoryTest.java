package org.mobilizadores.ccmp.support.system;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class JscompGroupThreadFactoryTest {

	@Test
	public void shouldCreateThread_withJscompPrefix() {
		JscompGroupThreadFactory factory = new JscompGroupThreadFactory();
		Thread thread = factory.newThread(() -> {});
		Assert.assertTrue(thread.getName().startsWith(JscompGroupThreadFactory.JSCOMP_PREFIX));
	}
}
