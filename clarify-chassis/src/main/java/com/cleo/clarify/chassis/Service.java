package com.cleo.clarify.chassis;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;

import com.cleo.clarify.chassis.consul.ConsulLifecycleListener;
import com.google.common.collect.Lists;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.GuiceFilter;
import com.typesafe.config.Config;

import io.grpc.BindableService;
import io.grpc.ServerBuilder;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.services.HealthStatusManager;

public abstract class Service {

	public void run() {
		List<Module> serviceModules = Lists.asList(new ChassisModule(), getModules());
		Injector injector = Guice.createInjector(Stage.PRODUCTION, serviceModules);
		
		startJetty(injector);
		io.grpc.Server grpc = startGrpc(injector);
		
		try {
			grpc.awaitTermination();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void startJetty(Injector injector) {
		int port = injector.getInstance(Config.class).getInt("registry.api.port");
		final org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(port);
		
		ServletContextHandler context = 
				new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
		context.addEventListener(injector.getInstance(GuiceResteasyBootstrapServletContextListener.class));
		context.addFilter(GuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC));
		context.addServlet(DefaultServlet.class, "/*");
		
		try {
			server.addLifeCycleListener(injector.getInstance(ConsulLifecycleListener.class));
            server.setStopAtShutdown(true);
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					server.stop();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
		}, "shutdownhook-jetty"));
	}
	
	private io.grpc.Server startGrpc(Injector injector) {
		int grpcPort = injector.getInstance(Config.class).getInt("registry.rpc.port");
		final ServerBuilder<?> builder = ServerBuilder.forPort(grpcPort);
		List<BindableService> services = injector.findBindingsByType(TypeLiteral.get(BindableService.class))
				.stream()
				.map(Service::toBindable)
				.collect(Collectors.toList());
		services.forEach((service) -> builder.addService(service));
		HealthStatusManager manager = new HealthStatusManager();
		builder.addService(manager.getHealthService());
		final io.grpc.Server server = builder.build();
		try {
			server.start();
			manager.setStatus("", ServingStatus.SERVING);
			services.stream()
				.map(Service::toServiceName)
				.forEach((name) -> manager.setStatus(name, ServingStatus.SERVING));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				server.shutdown();
			}
			
		}, "shutdownhook-grpc"));
		return server;
	}
	
	private static BindableService toBindable(Binding<BindableService> binding) {
		return binding.getProvider().get();
	}
	
	private static String toServiceName(BindableService service) {
		return service.bindService().getServiceDescriptor().getName();
	}
	
	public abstract Module[] getModules();
}
