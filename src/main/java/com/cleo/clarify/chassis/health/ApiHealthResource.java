package com.cleo.clarify.chassis.health;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Inject;
import com.google.inject.Provider;

@Path("api-health")
public class ApiHealthResource {

	@Inject
	private Provider<HealthCheckRegistry> healthCheckRegistryProvider;

	@GET
	@Produces("application/json")
	public Response health() {
		Map<String, HealthCheck.Result> result = healthCheckRegistryProvider.get().runHealthChecks();
		boolean healthy = result.values().stream().allMatch((checkResult) -> checkResult.isHealthy());
		return Response.status(healthy ? OK : SERVICE_UNAVAILABLE)
				.entity(result).build();
	}

}
