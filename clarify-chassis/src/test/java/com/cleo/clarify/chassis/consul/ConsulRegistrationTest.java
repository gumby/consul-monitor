package com.cleo.clarify.chassis.consul;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.cleo.clarify.chassis.config.ConfigModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.ImmutableCatalogOptions;

public class ConsulRegistrationTest  {
        
	private static Injector injector;
	
	@BeforeClass
	public static void createInjection() {
		injector = Guice.createInjector(Stage.PRODUCTION, new ConfigModule(), new ConsulModule());
	}
	
    @Test
    public void registers_rpc_service() {
    	injector.getBinding(ConsulRegistrator.class).getProvider().get().registerRpc();
    	
    	CatalogOptions catOpts = ImmutableCatalogOptions.builder().tag("rpc").build();
    	ConsulResponse<List<ServiceHealth>> serviceResponse =  consul().healthClient().getAllServiceInstances("test", catOpts);
    	Service service = serviceResponse.getResponse().get(0).getService();
    	
    	assertThat(service.getService(), equalTo("test"));
    	assertThat(service.getTags(), contains("rpc"));
    }
    
    @Test
    public void registers_http_service() {
    	injector.getBinding(ConsulRegistrator.class).getProvider().get().registerApi();
    	
    	CatalogOptions catOpts = ImmutableCatalogOptions.builder().tag("api").build();
    	ConsulResponse<List<ServiceHealth>> serviceResponse =  consul().healthClient().getAllServiceInstances("test", catOpts);
    	Service service = serviceResponse.getResponse().get(0).getService();
    	
    	assertThat(service.getService(), equalTo("test"));
    	assertThat(service.getTags(), contains("api"));
    }
    
    private Consul consul() {
    	return injector.getBinding(Consul.class).getProvider().get();
    }
}
