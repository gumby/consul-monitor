package com.cleo.clarify.chassis;

import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import com.cleo.clarify.chassis.config.ConfigModule;
import com.cleo.clarify.chassis.consul.ConsulModule;
import com.cleo.clarify.chassis.grpc.GrpcModule;
import com.cleo.clarify.chassis.health.HealthModule;
import com.cleo.clarify.chassis.health.checks.ChecksModule;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

public class ChassisModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(GuiceResteasyBootstrapServletContextListener.class).in(Scopes.SINGLETON);
		
		install(new ConfigModule());
		install(new HealthModule());
		install(new ChecksModule());
		install(new ConsulModule());
		install(new GrpcModule());
		install(new ServletModule() {
			
			@Override
			public void configureServlets() {
				bind(HttpServletDispatcher.class).in(Scopes.SINGLETON);
				serve("/*").with(HttpServletDispatcher.class);
			}
		});
	}

}
