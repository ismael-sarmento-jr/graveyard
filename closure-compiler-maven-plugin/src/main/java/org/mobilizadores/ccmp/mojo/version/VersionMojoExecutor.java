package org.mobilizadores.ccmp.mojo.version;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.mobilizadores.ccmp.mojo.AbstractMojoExecutor;
import org.mobilizadores.ccmp.support.notification.Notification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VersionMojoExecutor extends AbstractMojoExecutor<VersionMojo> {
	
	public VersionMojoExecutor(VersionMojo mojo) throws InstantiationException {
		super(mojo);
		this.addSingleObserver();
	}

	@Override
	public List<String[]> getCommandList() {
		ArrayList<String[]> commands = new ArrayList<String[]>();
		commands.add(new String[] { "--version" });
		return commands; 
	}

	public void addSingleObserver() {
		Observer observer = (Observable o, Object obj) -> {
			if (obj != null) {
				Notification notif = (Notification) obj;
				this.mojo.getLog().info(notif.getDescription());
			}
		};
		this.getObservers().add(observer );
	}
	
}
