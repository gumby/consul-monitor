package com.cleo.clarify.chassis.discovery;

import static com.orbitz.consul.option.ImmutableQueryOptions.builder;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.common.net.HostAndPort;
import com.google.inject.Inject;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.QueryOptions;

public class ConsulDiscovery implements ServiceDiscovery {
	
	private final Map<String, List<HostAndPort>> services = new ConcurrentHashMap<>();
	private final HealthClient healthClient;
	private final Random random = new Random();
	
	@Inject
	public ConsulDiscovery(Consul consul) {
		healthClient = consul.healthClient();
	}
	
	@Override
	public HostAndPort discover(String service) {
		List<HostAndPort> registrations = services.get(service);
		return registrations != null ?
				registrations.get(random.nextInt(registrations.size())) :
				doDiscover(service);
	}
	
	private HostAndPort doDiscover(String service) {
		final ConsulResponse<List<ServiceHealth>> response = healthClient.getHealthyServiceInstances(service);
		AtomicReference<BigInteger> index = new AtomicReference<>(response.getIndex());
		update(service, response);
		
		healthClient.getHealthyServiceInstances(service, QueryOptions.BLANK,
				new ConsulResponseCallback<List<ServiceHealth>>() {
					
					@Override
					public void onFailure(Throwable throwable) {
						healthClient.getHealthyServiceInstances(
								service,
								builder().wait("61s").index(index.get()).build(),
								this);
					}
					
					@Override
					public void onComplete(ConsulResponse<List<ServiceHealth>> consulResponse) {
						update(service, consulResponse);
						index.set(consulResponse.getIndex());
						healthClient.getHealthyServiceInstances(
								service,
								builder().wait("60s").index(index.get()).build(),
								this);
					}
				});
		return discover(service);
	}
	
	private void update(String service, ConsulResponse<List<ServiceHealth>> response) {
		services.put(service, response.getResponse().stream()
				.map((health) -> 
					HostAndPort.fromParts(
						health.getService().getAddress(),
						health.getService().getPort()))
				.collect(Collectors.toList()));
	}

}
