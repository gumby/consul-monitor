package com.cleo.clarify.chassis.consul;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Test;

import com.cleo.clarify.chassis.discovery.DiscoveredCallback;
import com.cleo.clarify.chassis.discovery.ServiceDiscovery;
import com.google.common.net.HostAndPort;

public class ConsulDiscoveryTest extends BaseConsulTest {
	
	@After
	public void unregister() {
		injector.getBinding(ConsulRegistrator.class).getProvider().get().deregister();
	}
		
	@Test
	public void regsitered_service_is_discovered() {
		injector.getBinding(ConsulRegistrator.class).getProvider().get().register();
		ServiceDiscovery discovery = injector.getBinding(ServiceDiscovery.class).getProvider().get();
		AtomicBoolean discovered = new AtomicBoolean(false);
		
		doDiscover(discovery, discovered);
		await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return discovered.get();
			}
		});
		
		assertThat(discovered.get(), equalTo(true));
	}
	
	@Test
	public void unregistered_serivce_isnt_discovered() throws InterruptedException {
		ServiceDiscovery discovery = injector.getBinding(ServiceDiscovery.class).getProvider().get();
		AtomicBoolean discovered = new AtomicBoolean(true);
		
		doDiscover(discovery, discovered);
		await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return !discovered.get();
			}
		});
		
		assertThat(discovered.get(), equalTo(false));
	}

	private void doDiscover(ServiceDiscovery discovery, AtomicBoolean discovered) {
		discovery.discover("test", "rpc", new DiscoveredCallback() {

			@Override
			public void onSuccess(HostAndPort result) {
				discovered.set(true);
			}

			@Override
			public void onFailure(Throwable t) {
				discovered.set(false);
			}

		});
	}
}
