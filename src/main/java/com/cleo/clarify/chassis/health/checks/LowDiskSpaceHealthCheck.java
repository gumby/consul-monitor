package com.cleo.clarify.chassis.health.checks;

import java.io.File;

import com.codahale.metrics.health.HealthCheck;

public class LowDiskSpaceHealthCheck extends HealthCheck {

	private static final long THRESHOLD = 10 * 1024 * 1024;
	
	@Override
	protected Result check() throws Exception {
		File f = new File(".");
		long free = f.getFreeSpace();
		if (free < THRESHOLD) {
			return Result.unhealthy(String.format("Low disk space: %d", free));
		} else {
			return Result.healthy("%d bytes free", free);
		}
	}

}
