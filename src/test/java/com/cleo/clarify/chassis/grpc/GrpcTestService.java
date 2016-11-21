package com.cleo.clarify.chassis.grpc;

import com.google.inject.Inject;

import io.grpc.ServerServiceDefinition;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.services.HealthStatusManager;
import io.grpc.stub.StreamObserver;

public final class GrpcTestService extends TestServiceGrpc.TestServiceImplBase {
	
	@Inject HealthStatusManager manager;
	
	@Override
	public ServerServiceDefinition bindService() {
		ServerServiceDefinition def =  super.bindService();
		manager.setStatus(TestServiceGrpc.SERVICE_NAME, ServingStatus.SERVING);
		return def;
	}
	
	@Override
	public void testing(Greeting request, StreamObserver<Howdy> responseObserver) {
		responseObserver.onNext(Howdy.newBuilder().setName(request.getName()).build());
		responseObserver.onCompleted();
	}

}