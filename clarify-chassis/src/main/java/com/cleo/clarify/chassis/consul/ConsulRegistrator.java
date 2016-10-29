package com.cleo.clarify.chassis.consul;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import com.google.inject.Inject;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegCheck;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import com.typesafe.config.Config;

public class ConsulRegistrator {

	private AgentClient client;
	private final String host;
	private final int port;
	private final String serviceName;
	private final String serviceId;
	
	@Inject
	public ConsulRegistrator(Consul consul, Config config) {
		client = consul.agentClient();
		host = config.getString("discovery.advertised.host");
		port = config.getInt("discovery.advertised.port");
		serviceName = config.getString("service.name");
		serviceId = String.format("%s-%s", serviceName, UUID.randomUUID().toString());
	}
	
	public void register() {
		URL healthUrl = null;
		try {
			healthUrl = new URL("http", host, port, "/health");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		
		Registration.RegCheck healthCheck = ImmutableRegCheck.builder()
				.http(healthUrl.toExternalForm())
				.interval("10s")
				.timeout("5s")
				.build();
		
		Registration registration = ImmutableRegistration.builder()
				.address(host)
				.port(port)
				.id(serviceId)
				.name(serviceName)
				.check(healthCheck)
				.build();
		
		client.register(registration);
	}
	
	public void deregister() {
		client.deregister(serviceId);
	}
}
