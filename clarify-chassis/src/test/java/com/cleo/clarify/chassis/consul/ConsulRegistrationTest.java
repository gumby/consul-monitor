package com.cleo.clarify.chassis.consul;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.option.CatalogOptions;
import com.orbitz.consul.option.ImmutableCatalogOptions;

public class ConsulRegistrationTest extends BaseConsulTest {
    
    @After
    public void resetConsul() {
    	consul.reset();
    }
    
    @Test
    public void registers_rpc_service() {
    	injector.getBinding(ConsulRegistrator.class).getProvider().get().register();
    	
    	CatalogOptions catOpts = ImmutableCatalogOptions.builder().tag("rpc").build();
    	ConsulResponse<List<ServiceHealth>> serviceResponse =  consul().healthClient().getAllServiceInstances("test", catOpts);
    	Service service = serviceResponse.getResponse().get(0).getService();
    	
    	assertThat(service.getService(), equalTo("test"));
    	assertThat(service.getTags(), contains("rpc"));
    }
    
    @Test
    public void registers_http_service() {
    	injector.getBinding(ConsulRegistrator.class).getProvider().get().register();
    	
    	CatalogOptions catOpts = ImmutableCatalogOptions.builder().tag("api").build();
    	ConsulResponse<List<ServiceHealth>> serviceResponse =  consul().healthClient().getAllServiceInstances("test", catOpts);
    	Service service = serviceResponse.getResponse().get(0).getService();
    	
    	assertThat(service.getService(), equalTo("test"));
    	assertThat(service.getTags(), contains("api"));
    }
    
    private Consul consul() {
    	return Consul.builder().withHostAndPort(HostAndPort.fromParts("localhost", consul.getHttpPort())).build();
    }
    
}
