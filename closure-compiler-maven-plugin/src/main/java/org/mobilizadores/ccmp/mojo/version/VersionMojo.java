package org.mobilizadores.ccmp.mojo.version;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.mobilizadores.ccmp.mojo.ClosureCompilerMojo;

@Mojo(name = "version", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class VersionMojo extends ClosureCompilerMojo {
	
	public VersionMojo() throws InstantiationException {
		super();	
		this.executor = new VersionMojoExecutor(this);
	}

}
