package com.cleo.clarify.chassis.grpc;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import io.grpc.BindableService;

public final class GrpcTestServiceModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(BindableService.class).to(GrpcTestService.class).in(Scopes.SINGLETON);
	}
	
}