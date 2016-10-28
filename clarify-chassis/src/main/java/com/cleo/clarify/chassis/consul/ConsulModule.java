package com.cleo.clarify.chassis.consul;

import com.cleo.clarify.chassis.discovery.ServiceDiscovery;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class ConsulModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConsulRegistrator.class).in(Scopes.SINGLETON);
		bind(ConsulLifecycleListener.class).in(Scopes.SINGLETON);
		bind(ServiceDiscovery.class).in(Scopes.SINGLETON);
	}

}
