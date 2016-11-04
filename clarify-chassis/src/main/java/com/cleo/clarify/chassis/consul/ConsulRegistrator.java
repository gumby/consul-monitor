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
	private final int apiPort;
	private final int rpcPort;
	private final String serviceName;
	private final String apiServiceId;
	private final String rpcServiceId;
	
	@Inject
	public ConsulRegistrator(Consul consul, Config config) {
		client = consul.agentClient();
		host = config.getString("registry.api.host");
		apiPort = config.getInt("registry.api.port");
		rpcPort = config.getInt("registry.rpc.port");
		serviceName = config.getString("service.name");
		apiServiceId = String.format("%s-%s", serviceName, UUID.randomUUID().toString());
		rpcServiceId = String.format("%s-%s", serviceName, UUID.randomUUID().toString());
	}
	
	public void register() {
		registerRpc();
		registerHttp();
	}
	
	public void deregister() {
		client.deregister(apiServiceId);
		client.deregister(rpcServiceId);
	}
	
	private void registerHttp() {
		URL healthUrl = null;
		try {
			healthUrl = new URL("http", host, apiPort, "/health");
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
				.port(apiPort)
				.id(apiServiceId)
				.name(serviceName)
				.addTags("api")
				.check(healthCheck)
				.build();
		
		client.register(registration);
	}
	
	private void registerRpc() {
		Registration registration = ImmutableRegistration.builder()
				.address(host)
				.port(rpcPort)
				.id(rpcServiceId)
				.name(serviceName)
				.addTags("rpc")
				.build();
		client.register(registration);
	}
}
