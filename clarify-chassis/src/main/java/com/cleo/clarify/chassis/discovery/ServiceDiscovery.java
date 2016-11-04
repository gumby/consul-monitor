package com.cleo.clarify.chassis.discovery;

public interface ServiceDiscovery {
	
	void discover(String service, String transportType, DiscoveredCallback callback);

}
