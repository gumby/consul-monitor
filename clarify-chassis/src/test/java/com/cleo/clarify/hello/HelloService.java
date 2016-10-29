package com.cleo.clarify.hello;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;

import com.cleo.clarify.chassis.Service;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import com.google.inject.Scopes;

public class HelloService extends Service {

	@Override
	public Module[] getModules() {
		return new Module[] {
	            new RequestScopeModule() {

	                @Override
	                protected void configure()
	                {
	                    bind(DataResource.class).in(Scopes.SINGLETON);
	                }
	            }
	        };
	}
	
	@Path("/")
    public static class DataResource {

        @GET
        @Produces("application/json")
        public Map<String, Boolean> getData() {
            return ImmutableMap.of("value", true);
        }
    }
	
	public static void main(String[] args) {
		new HelloService().run();
	}

}
