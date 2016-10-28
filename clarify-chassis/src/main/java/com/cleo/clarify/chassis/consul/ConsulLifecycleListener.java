package com.cleo.clarify.chassis.consul;

import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;

import com.google.inject.Inject;

public class ConsulLifecycleListener extends AbstractLifeCycleListener {

	private ConsulRegistrator consulRegistrator;
	
	@Inject
	public ConsulLifecycleListener(ConsulRegistrator consulRegistrator) {
		this.consulRegistrator = consulRegistrator;
	}
	
	@Override
	public void lifeCycleStarted(LifeCycle event) {
		consulRegistrator.register();
	}
	
	@Override
	public void lifeCycleStopping(LifeCycle event) {
		consulRegistrator.deregister();
	}

}
