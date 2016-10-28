package com.cleo.clarify.hello;

import com.cleo.clarify.chassis.Service;
import com.google.inject.Module;

public class HelloService extends Service {

	@Override
	public Module[] getModules() {
		return new Module[]{};
	}
	
	public static void main(String[] args) {
		new HelloService().run();
	}

}
