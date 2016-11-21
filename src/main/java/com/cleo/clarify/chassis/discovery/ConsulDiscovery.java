package com.cleo.clarify.chassis.discovery;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.ImmutableCatalogOptions;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.orbitz.consul.option.QueryOptions;

/**
 * Service Discovery using a Consul agent.
 */
public class ConsulDiscovery implements ServiceDiscovery {

	private ConcurrentHashMap<ServiceNameAndType, List<HostAndPort>> services = new ConcurrentHashMap<>();

	private HealthClient healthClient;

	private Random random = new Random();

	@Inject
	public ConsulDiscovery(Consul consul) {
		healthClient = consul.healthClient();
	}

	@Override
	public HostAndPort discover(String service, String transportType) {
		ServiceNameAndType nameAndType = ServiceNameAndType.from(service, transportType);
		List<HostAndPort> registrations = services.get(nameAndType);
		if (registrations!= null && registrations.size() > 0) {
			return registrations.get(random.nextInt(registrations.size()));
		} else {
			doDiscover(nameAndType);
			return null;
		}
	}

	private void doDiscover(ServiceNameAndType nameAndType) {
		final ConsulResponse<List<ServiceHealth>> response = healthClient.getHealthyServiceInstances(nameAndType.serviceName,
				ImmutableCatalogOptions.builder().tag(nameAndType.serviceType).build(), ImmutableQueryOptions.BLANK);
		AtomicReference<BigInteger> index = new AtomicReference<>(response.getIndex());

		update(nameAndType, response);

		healthClient.getHealthyServiceInstances(
				nameAndType.serviceName, 
				ImmutableCatalogOptions.builder().tag(nameAndType.serviceType).build(), 
				QueryOptions.BLANK,
				new ConsulResponseCallback<List<ServiceHealth>>() {


					@Override
					public void onComplete(ConsulResponse<List<ServiceHealth>> consulResponse) {
						update(nameAndType, consulResponse);
						index.set(consulResponse.getIndex());
						healthClient.getHealthyServiceInstances(
								nameAndType.serviceName,
								ImmutableCatalogOptions.builder().tag(nameAndType.serviceType).build(),
								ImmutableQueryOptions.builder().wait("1m").index(index.get()).build(),
								this);
					}

					@Override
					public void onFailure(Throwable throwable) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						healthClient.getHealthyServiceInstances(
								nameAndType.serviceName,
								ImmutableCatalogOptions.builder().tag(nameAndType.serviceType).build(),
								ImmutableQueryOptions.builder().wait("1m").index(index.get()).build(),
								this);
					}
				});
	}

	/**
	 * Updates the internal map of service registrations with those specified in
	 * the Consul response.
	 *
	 * @param service The service name.
	 * @param response A {@link ConsulResponse} object containing the service registrations.
	 */
	private void update(ServiceNameAndType nameAndType, ConsulResponse<List<ServiceHealth>> response) {
		services.put(nameAndType, response.getResponse().stream()
				.map((health) -> HostAndPort.fromParts(health.getService().getAddress(), health.getService().getPort()))
				.collect(Collectors.toList()));
	}

	private static final class ServiceNameAndType {

		private final String serviceName;
		private final String serviceType;

		private ServiceNameAndType(String serviceName, String serviceType) {
			this.serviceName = serviceName;
			this.serviceType = serviceType;
		}

		public static ServiceNameAndType from(String serviceName, String serviceType) {
			return new ServiceNameAndType(serviceName, serviceType);
		}

		@Override
		public int hashCode() {
			return serviceName.hashCode() + serviceType.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == null) return false;
			if (!(other instanceof ServiceNameAndType)) return false;
			ServiceNameAndType otherKey = (ServiceNameAndType) other;
			return this.serviceName.equals(otherKey.serviceName) && 
					this.serviceType.equals(otherKey.serviceType);
		}

		@Override
		public String toString() {
			return String.format("%s-%s", serviceName, serviceType);
		}

	}
}
