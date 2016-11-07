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

public class ConsulRegistrator implements ServiceRegistrator {

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
		
	@Override
	public void unregisterApi() {
		client.deregister(apiServiceId);
	}
	
	@Override
	public void unregisterRpc() {
		client.deregister(rpcServiceId);
	}
	
	@Override
	public void registerApi() {
		Registration.RegCheck healthCheck = ImmutableRegCheck.builder()
				.http(healthUrl("/api-health").toExternalForm())
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
	
	@Override
	public void registerRpc() {
		Registration.RegCheck healthCheck = ImmutableRegCheck.builder()
				.http(healthUrl("/rpc-health").toExternalForm())
				.interval("10s")
				.timeout("5s")
				.build();
		
		Registration registration = ImmutableRegistration.builder()
				.address(host)
				.port(rpcPort)
				.id(rpcServiceId)
				.name(serviceName)
				.addTags("rpc")
				.check(healthCheck)
				.build();
		client.register(registration);
	}
	
	private URL healthUrl(String endpoint) {
		try {
			return new URL("http", host, apiPort, endpoint);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
