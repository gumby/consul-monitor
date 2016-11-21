package com.cleo.clarify.chassis.consul;

public interface ServiceRegistrator {

	public void registerApi();
	
	public void registerRpc();

	public void unregisterApi();
	
	public void unregisterRpc();
}
