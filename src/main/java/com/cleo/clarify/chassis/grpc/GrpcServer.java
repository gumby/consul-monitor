package com.cleo.clarify.chassis.grpc;


import java.io.IOException;

import com.cleo.clarify.chassis.consul.ServiceRegistrator;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.services.HealthStatusManager;

public class GrpcServer {

	private int grpcPort;
	private ServiceRegistrator registrator;
	private ServerBuilder<?> builder;
	private Server server;
	private HealthStatusManager healthManager;
	
	@Inject
	public GrpcServer(Config config, ServiceRegistrator registrator, HealthStatusManager healthManager) {
		this.grpcPort = config.getInt("registry.rpc.port");
		this.registrator = registrator;
		this.healthManager = healthManager;
		this.builder = ServerBuilder.forPort(grpcPort);
	}
	
	public void start() {
		builder.addService(healthManager.getHealthService());
		this.server = builder.build();
		try {
			server.start();
			registrator.registerRpc();
			healthManager.setStatus("", ServingStatus.SERVING);
			startShutdownHook();
		} catch (IOException e) {
			throw new RuntimeException("Unable to start gRPC server.", e);
		}
	}
	
	public void stop() {
		registrator.unregisterRpc();
		server.shutdown();
	}
	
	public void addService(BindableService service) {
		builder.addService(service);
	}
	
	private void startShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				registrator.unregisterRpc();
				server.shutdown();
			}
			
		}, "shutdownhook-grpc"));
	}

	
}
