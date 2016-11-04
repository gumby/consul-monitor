package com.cleo.clarify.chassis.grpc;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;

import org.junit.ClassRule;
import org.junit.Test;

import com.cleo.clarify.chassis.Service;
import com.cleo.clarify.chassis.grpc.test.Greeting;
import com.cleo.clarify.chassis.grpc.test.Howdy;
import com.cleo.clarify.chassis.grpc.test.TestServiceGrpc;
import com.cleo.clarify.chassis.grpc.test.TestServiceGrpc.TestServiceBlockingStub;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.pszymczyk.consul.junit.ConsulResource;

import io.grpc.BindableService;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.StreamObserver;

public class GrpcBindingTest {
		
	@ClassRule
	public static final ConsulResource consul = new ConsulResource();
	
	@Test
	public void binds_grpc_service() {
		startService();
		
		ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress("127.0.0.1", 9090).usePlaintext(true);
		Channel channel = builder.build();
		await().atMost(10, TimeUnit.SECONDS).ignoreExceptionsInstanceOf(StatusRuntimeException.class).until(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return checkServingStatus(channel);
			}
		});
		TestServiceBlockingStub blockingStub = TestServiceGrpc.newBlockingStub(channel);
		Howdy howdy = blockingStub.testing(Greeting.newBuilder().setGreeting("Hello, World.").setName("Ted").build());
		
		assertThat(howdy.getName(), equalTo("Ted"));
	}
	
	private boolean checkServingStatus(Channel channel) {
		HealthCheckResponse response = 
				HealthGrpc.newBlockingStub(channel).withDeadlineAfter(5, TimeUnit.SECONDS)
				.check(HealthCheckRequest.newBuilder().setService("")
				.build());
		return response.getStatus().equals(ServingStatus.SERVING);
	}
	
	private void startService() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				System.setProperty("discovery.port", String.valueOf(consul.getHttpPort()));
				new Service() {

					@Override
					public Module[] getModules() {
						return new Module[] {
								new GrpcTestServiceModule()
						};
					}
					
				}.run();
			}
			
		}).start();
	}
	
	private static final class GrpcTestServiceModule extends AbstractModule {
		
		@Override
		protected void configure() {
			bind(BindableService.class).to(GrpcTestService.class).in(Scopes.SINGLETON);
		}
		
	}
	
	private static final class GrpcTestService extends TestServiceGrpc.TestServiceImplBase {
		
		@Override
		public void testing(Greeting request, StreamObserver<Howdy> responseObserver) {
			responseObserver.onNext(Howdy.newBuilder().setName(request.getName()).build());
			responseObserver.onCompleted();
		}
	
	}

}
