package com.cleo.clarify.chassis.consul;

import com.cleo.clarify.chassis.discovery.ConsulDiscovery;
import com.cleo.clarify.chassis.discovery.ServiceDiscovery;
import com.google.common.net.HostAndPort;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.orbitz.consul.Consul;
import com.typesafe.config.Config;

public class ConsulModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConsulRegistrator.class).in(Scopes.SINGLETON);
		bind(ConsulLifecycleListener.class).in(Scopes.SINGLETON);
		bind(ServiceDiscovery.class).to(ConsulDiscovery.class).in(Scopes.SINGLETON);
	}
	
    @Provides
    @Singleton
    public Consul consul(Config config) {
        String host = config.getString("discovery.consul.host");
        int port = config.getInt("discovery.consul.port");

        return Consul.builder()
        		.withHostAndPort(HostAndPort.fromParts(host, port))
        		.build();
    }

}
