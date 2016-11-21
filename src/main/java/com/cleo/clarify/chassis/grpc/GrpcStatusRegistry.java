package com.cleo.clarify.chassis.grpc;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.inject.Inject;
import com.typesafe.config.Config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub;

public class GrpcStatusRegistry {
	
	private HealthBlockingStub healthStub;
	private List<String> services = new ArrayList<>();
	
	@Inject
	public GrpcStatusRegistry(Config config) {
		int grpcPort = config.getInt("registry.rpc.port");
		String host = config.getString("registry.api.host");
		ManagedChannel channel = ManagedChannelBuilder.forAddress(host, grpcPort).usePlaintext(true).build();
		this.healthStub = HealthGrpc.newBlockingStub(channel);
		this.services.add(""); // The root health service
	}
	
	public void addServiceCheck(String serviceName) {
		services.add(serviceName);
	}
	
	public SortedMap<String, ServingStatus> runServingChecks() {
		// This could possibly use the future stub to speed things up but
		// we don't expect many services to be health checked.
		SortedMap<String, ServingStatus> statusMap = new TreeMap<>();
		services.stream().forEach((serviceName) -> {
			statusMap.put(serviceName, healthStub.check(healthRequest(serviceName)).getStatus());
		});
		return statusMap;
	}

	private HealthCheckRequest healthRequest(String serviceName) {
		return HealthCheckRequest.newBuilder().setService(serviceName).build();
	}

}
