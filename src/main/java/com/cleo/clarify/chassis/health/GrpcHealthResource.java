package com.cleo.clarify.chassis.health;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.cleo.clarify.chassis.grpc.GrpcStatusRegistry;
import com.google.inject.Inject;
import com.google.inject.Provider;

import io.grpc.health.v1.HealthCheckResponse.ServingStatus;

@Path("rpc-health")
public class GrpcHealthResource {

	@Inject
	Provider<GrpcStatusRegistry> grpcRegistry;
	
	@GET
	@Produces("application/json")
	public Response health() {
		Map<String, ServingStatus> checks = grpcRegistry.get().runServingChecks();
		boolean serving = checks.values().stream().allMatch((status) -> status.equals(ServingStatus.SERVING)); 
		return Response.status(serving ? OK : SERVICE_UNAVAILABLE).entity(checks).build();
	}
}
