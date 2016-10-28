package com.cleo.clarify.chassis;

import com.cleo.clarify.chassis.config.ConfigModule;
import com.cleo.clarify.chassis.consul.ConsulModule;
import com.cleo.clarify.chassis.health.HealthModule;
import com.cleo.clarify.chassis.health.checks.ChecksModule;
import com.google.inject.AbstractModule;

public class ChassisModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new ConfigModule());
		install(new HealthModule());
		install(new ChecksModule());
		install(new ConsulModule());
	}

}
