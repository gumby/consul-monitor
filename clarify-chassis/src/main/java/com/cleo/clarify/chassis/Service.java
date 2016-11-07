package com.cleo.clarify.chassis;

import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;

import com.cleo.clarify.chassis.consul.ConsulLifecycleListener;
import com.cleo.clarify.chassis.grpc.GrpcServer;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;
import com.typesafe.config.Config;

public abstract class Service {

	public void run() {
		List<Module> serviceModules = Lists.asList(new ChassisModule(), getModules());
		Injector injector = Guice.createInjector(Stage.PRODUCTION, serviceModules);
		
		Server jettyServer = startJetty(injector);
		injector.getInstance(GrpcServer.class).start();
		
		try {
			jettyServer.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Server startJetty(Injector injector) {
		int port = injector.getInstance(Config.class).getInt("registry.api.port");
		final Server server = new org.eclipse.jetty.server.Server(port);
		
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
		return server;
	}
	
	public abstract Module[] getModules();
	
}
