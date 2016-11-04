package com.cleo.clarify.chassis.consul;

import static com.jayway.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.ClassRule;

import com.cleo.clarify.chassis.config.ConfigModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.pszymczyk.consul.junit.ConsulResource;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class BaseConsulTest {
	
    @ClassRule
    public static final ConsulResource consul = new ConsulResource();
    static OkHttpClient client = new OkHttpClient();
    static Injector injector;

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

	@BeforeClass
	public static void createInjection() {
		injector = Guice.createInjector(Stage.PRODUCTION, new ConfigModule(), new ConsulModule());
	}

}
