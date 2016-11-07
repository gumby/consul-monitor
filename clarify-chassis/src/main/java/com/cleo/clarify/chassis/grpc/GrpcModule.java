package com.cleo.clarify.chassis.grpc;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import io.grpc.BindableService;
import io.grpc.services.HealthStatusManager;

public class GrpcModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(GrpcServer.class).in(Scopes.SINGLETON);
		bind(GrpcStatusRegistry.class).in(Scopes.SINGLETON);
		bindListener(Matchers.any(), new TypeListener() {
			private Provider<GrpcServer> serverProvider = getProvider(GrpcServer.class);
			private Provider<GrpcStatusRegistry> registryProvider = getProvider(GrpcStatusRegistry.class);
			
			@Override
			public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
				if (BindableService.class.isAssignableFrom(type.getRawType())) {
					encounter.register((InjectionListener<I>) injectee -> {
						BindableService service = (BindableService) injectee;
						serverProvider.get().addService(service);
						registryProvider.get().addServiceCheck(service.bindService().getServiceDescriptor().getName());
					});
				}
			}
		});
	}

	@Provides
	@Singleton
	public HealthStatusManager grpcHealthManager() {
		return new HealthStatusManager();
	}

}
