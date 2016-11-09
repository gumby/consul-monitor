package com.cleo.clarify.chassis;

import static com.jayway.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.cleo.clarify.chassis.consul.ConsulDiscoveryTest;
import com.cleo.clarify.chassis.consul.ConsulRegistrationTest;
import com.cleo.clarify.chassis.grpc.GrpcBindingTest;
import com.cleo.clarify.chassis.grpc.GrpcHealthStatusTest;
import com.pszymczyk.consul.junit.ConsulResource;

import okhttp3.OkHttpClient;
import okhttp3.Request;

@RunWith(Suite.class)
@SuiteClasses({
	ConsulDiscoveryTest.class,
	ConsulRegistrationTest.class,
	GrpcBindingTest.class,
	GrpcHealthStatusTest.class,
})
public class AllTests {

	@ClassRule
    public static final ConsulResource consul = new ConsulResource();
    static OkHttpClient client = new OkHttpClient();

	@BeforeClass
	public static void startConsulAndSetPort() throws Throwable {
	    await().atMost(30, TimeUnit.SECONDS).until(() -> {
	        Request request = new Request.Builder()
	                .url("http://localhost:" + consul.getHttpPort() + "/v1/agent/self")
	                .build();
	
	        return client.newCall(request).execute().code() == 200;
	    });
	    System.setProperty("discovery.port", String.valueOf(consul.getHttpPort()));
	}
	
    @After
    public void resetConsul() {
    	consul.reset();
    }

}
