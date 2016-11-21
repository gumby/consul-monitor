package com.cleo.clarify.chassis.grpc;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cleo.clarify.chassis.Service;
import com.cleo.clarify.chassis.grpc.TestServiceGrpc.TestServiceBlockingStub;
import com.google.inject.Module;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;

public class GrpcBindingTest {
	
	private static Thread serviceThread;
	
	@BeforeClass
	public static void startService() {
		serviceThread = new Thread(new Runnable() {

			@Override
			public void run() {
				new Service() {

					@Override
					public Module[] getModules() {
						return new Module[] {
								new GrpcTestServiceModule()
						};
					}
					
				}.run();
			}
			
		});
		serviceThread.start();
	}
	
	@AfterClass
	public static void stopService() {
		serviceThread.interrupt();
	}
	
	@Test
	public void binds_grpc_service() {		
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

}
