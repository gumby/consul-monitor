package com.cleo.clarify.chassis.some;

import com.cleo.clarify.chassis.Service;
import com.cleo.clarify.chassis.grpc.GrpcTestServiceModule;
import com.google.inject.Module;
import com.pszymczyk.consul.ConsulProcess;
import com.pszymczyk.consul.ConsulStarterBuilder;

public class SomeService extends Service {
	
	private ConsulProcess consul;
	
	public SomeService() {
//		startConsul();
	}
	
	private void startConsul() {
		this.consul = ConsulStarterBuilder.consulStarter().withHttpPort(8500).build().start();
		try {
			Thread.sleep(15000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new SomeService().run();
	}

	@Override
	public Module[] getModules() {
		return new Module[] {
				new GrpcTestServiceModule()
		};
	}
}
