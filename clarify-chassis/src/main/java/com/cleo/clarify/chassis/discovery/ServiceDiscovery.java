package com.cleo.clarify.chassis.discovery;

import com.google.common.net.HostAndPort;

public interface ServiceDiscovery {
	
	HostAndPort discover(String service);

}
