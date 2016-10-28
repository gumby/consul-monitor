package com.cleo.clarify.chassis.health;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class HealthModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(HealthCheckRegistry.class).toProvider(new Provider<HealthCheckRegistry>() {
			private HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
			
			@Override
			public HealthCheckRegistry get() {
				return healthCheckRegistry;
			}
			
		});
		bind(HealthResource.class).in(Scopes.SINGLETON);
		setCheckListener();
	}

	private void setCheckListener() {
		Provider<HealthCheckRegistry> healthCheckRegistryProvider = getProvider(HealthCheckRegistry.class);
		bindListener(Matchers.any(), new TypeListener() {

			final Converter<String, String> converter =
                    CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN);
			
			@Override
			public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
				if (HealthCheck.class.isAssignableFrom(type.getRawType())) {
					encounter.register((InjectionListener<I>) injectee -> {
						HealthCheck check = (HealthCheck) injectee;
						String name = converter.convert(check.getClass().getSimpleName());
						healthCheckRegistryProvider.get().register(name, check);
					});
				}
			}
		});
	}

}
