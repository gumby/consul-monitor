package com.cleo.clarify.chassis.grpc;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.SortedMap;

import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cleo.clarify.chassis.consul.ServiceRegistrator;
import com.cleo.clarify.chassis.health.GrpcHealthResource;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.typesafe.config.Config;

import io.grpc.health.v1.HealthCheckResponse.ServingStatus;

public class GrpcHealthStatusTest {
	
	private static Injector injector;
	private static GrpcServer server;
	
	@BeforeClass
	public static void setupGrpcServer() {
		injector = Guice.createInjector(Stage.PRODUCTION, new MockModule(), new GrpcModule(), new GrpcTestServiceModule());
		server = injector.getInstance(GrpcServer.class);
		server.start();
	}
	
	@AfterClass
	public static void shutdownGrpcServer() {
		server.stop();
	}
		
	@Test
	public void grpc_check_contains_test_service() {
		Response response = injector.getInstance(GrpcHealthResource.class).health();
		@SuppressWarnings("unchecked")
		SortedMap<String, ServingStatus> healthChecks = (SortedMap<String, ServingStatus>) response.getEntity();
		
		assertThat(response.getStatus(), equalTo(200));
		assertThat(healthChecks.containsKey(TestServiceGrpc.SERVICE_NAME), equalTo(true));
		assertThat(healthChecks.get(TestServiceGrpc.SERVICE_NAME), equalTo(ServingStatus.SERVING));
	}
	
	private static final class MockModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(Config.class).toInstance(mockConfig());
			bind(ServiceRegistrator.class).toInstance(mock(ServiceRegistrator.class));
		}
		
		private Config mockConfig() {
			Config mocked = mock(Config.class);
			when(mocked.getInt("registry.rpc.port")).thenReturn(9090);
			when(mocked.getString("registry.api.host")).thenReturn("localhost");
			return mocked;
		}
		
	}

}
