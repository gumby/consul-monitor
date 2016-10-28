package com.cleo.clarify.chassis;

import com.cleo.clarify.chassis.config.ConfigModule;
import com.cleo.clarify.chassis.consul.ConsulModule;
import com.google.inject.AbstractModule;

public class ChassisModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new ConfigModule());
		install(new ConsulModule());
	}

}
