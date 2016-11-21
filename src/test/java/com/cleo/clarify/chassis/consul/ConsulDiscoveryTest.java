package com.cleo.clarify.chassis.consul;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.cleo.clarify.chassis.config.ConfigModule;
import com.cleo.clarify.chassis.discovery.ServiceDiscovery;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.net.HostAndPort;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public class ConsulDiscoveryTest {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(options().port(8080));
	private static Injector injector;
	
	@BeforeClass
	public static void createInjection() {
		injector = Guice.createInjector(Stage.PRODUCTION, new ConfigModule(), new ConsulModule());
	}
	
	@After
	public void unregister() {
		injector.getBinding(ConsulRegistrator.class).getProvider().get().unregisterRpc();
	}
		
	@Test
	public void regsitered_service_is_discovered() {
		wireMockRule.stubFor(get(urlEqualTo("/rpc-health")).willReturn(aResponse().withStatus(200)));
		injector.getBinding(ConsulRegistrator.class).getProvider().get().registerRpc();
		ServiceDiscovery discovery = injector.getBinding(ServiceDiscovery.class).getProvider().get();
		
		await().atMost(15, TimeUnit.SECONDS).until(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return discovery.discover("test", "rpc") != null;
			}
		});
		HostAndPort discovered = discovery.discover("test", "rpc");
		assertThat(discovered.getHostText(), equalTo("localhost"));
		assertThat(discovered.getPort(), equalTo(9090));
	}
	
	@Test
	public void unregistered_serivce_isnt_discovered() throws InterruptedException {
		ServiceDiscovery discovery = injector.getBinding(ServiceDiscovery.class).getProvider().get();
		await().atMost(15, TimeUnit.SECONDS).until(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return discovery.discover("test", "rpc") != null;
			}
		});
		HostAndPort discovered = discovery.discover("test", "rpc");
		assertThat(discovered.getHostText(), equalTo("localhost"));
		assertThat(discovered.getPort(), equalTo(9090));
	}
}
