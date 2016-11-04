package com.cleo.clarify.chassis.consul;

import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;

import com.google.inject.Inject;
import com.typesafe.config.Config;

public class ConsulLifecycleListener extends AbstractLifeCycleListener {

	private final ConsulRegistrator consulRegistrator;
	private final boolean discoverable;
	
	@Inject
	public ConsulLifecycleListener(ConsulRegistrator consulRegistrator, Config config) {
		this.discoverable = config.getBoolean("service.discoverable");
		this.consulRegistrator = consulRegistrator;
	}
	
	@Override
	public void lifeCycleStarted(LifeCycle event) {
		if (discoverable) consulRegistrator.register();
	}
	
	@Override
	public void lifeCycleStopping(LifeCycle event) {
		if (discoverable) consulRegistrator.deregister();
	}

}
