package com.cleo.clarify.chassis;

import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;

import com.cleo.clarify.chassis.consul.ConsulLifecycleListener;
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
		
		int port = injector.getInstance(Config.class).getInt("discovery.advertised.port");
		final Server server = new Server(port);
		
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
			
		}, "ShutdownHook"));
		
		try {
			server.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public abstract Module[] getModules();
}
