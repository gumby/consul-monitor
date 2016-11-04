package com.cleo.clarify.chassis.discovery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.net.HostAndPort;
import com.google.inject.Inject;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.cache.ConsulCache.Listener;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.ImmutableCatalogOptions;

public class ConsulDiscovery implements ServiceDiscovery {
	
	private final HealthClient healthClient;
	private Map<ServiceKey, List<HostAndPort>> healthCache = new ConcurrentHashMap<>();
	
	@Inject
	public ConsulDiscovery(Consul consul) {
		healthClient = consul.healthClient();
	}
	
	@Override
	public void discover(String serviceName, String serviceType, DiscoveredCallback callback) {
		if (healthCache.containsKey(ServiceKey.from(serviceName, serviceType))) {
			checkCache(serviceName, serviceType, callback);
		} else {
			ServiceHealthCache svHealth = startHealthListener(serviceName, serviceType);
			try {
				svHealth.awaitInitialized(3, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				callback.onFailure(e);
			}
			checkCache(serviceName, serviceType, callback);
		}	
	}

	private ServiceHealthCache startHealthListener(String serviceName, String serviceType) {
		CatalogOptions catOpts = ImmutableCatalogOptions.builder().tag(serviceType).build();
		ServiceHealthCache svHealth = ServiceHealthCache.newCache(healthClient, serviceName, true, catOpts, 10);
		svHealth.addListener(new Listener<ServiceHealthKey, ServiceHealth>() {

			@Override
			public void notify(Map<ServiceHealthKey, ServiceHealth> newValues) {
				ServiceKey key = ServiceKey.from(serviceName, serviceType);
				if (newValues.isEmpty()) {
					healthCache.remove(key);
				}
				else {
					List<HostAndPort> locations = newValues.values().stream()
							.map(ConsulDiscovery::healthToLocation)
						.collect(Collectors.toList());
					healthCache.put(key, locations);
				}
			}	
		});
		try {
			svHealth.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return svHealth;
	}

	private void checkCache(String serviceName, String serviceType, DiscoveredCallback callback) {
		List<HostAndPort> services = healthCache.get(ServiceKey.from(serviceName, serviceType));
		if (services != null && services.size() > 0) {
			callback.onSuccess(services.get(0));
		} else {
			callback.onFailure(new RuntimeException());
		}
	}

	private static HostAndPort healthToLocation(ServiceHealth health) {
		return HostAndPort.fromParts(health.getService().getAddress(), health.getService().getPort());
	}
	
	private static final class ServiceKey {

		private final String serviceName;
		private final String serviceType;
		
		private ServiceKey(String serviceName, String serviceType) {
			this.serviceName = serviceName;
			this.serviceType = serviceType;
		}
		
		public static ServiceKey from(String serviceName, String serviceType) {
			return new ServiceKey(serviceName, serviceType);
		}
		
		@Override
		public int hashCode() {
			return serviceName.hashCode() + serviceType.hashCode();
		}
		
		@Override
		public boolean equals(Object other) {
			if (other == null) return false;
			if (!(other instanceof ServiceKey)) return false;
			ServiceKey otherKey = (ServiceKey) other;
			return this.serviceName.equals(otherKey.serviceName) && 
					this.serviceType.equals(otherKey.serviceType);
		}
		
		@Override
		public String toString() {
			return String.format("%s-%s", serviceName, serviceType);
		}
		
	}

}
