package com.cleo.clarify.chassis;

import java.util.List;

import org.eclipse.jetty.server.Server;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.typesafe.config.Config;

public abstract class Service {

	public void run() {
		List<Module> serviceModules = Lists.asList(new ChassisModule(), getModules());
		Injector injector = Guice.createInjector(Stage.PRODUCTION, serviceModules);
		int port = injector.getInstance(Config.class).getInt("service.port");
		final Server server = new Server(port);
		
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
