package com.cleo.clarify.chassis.api;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cleo.clarify.chassis.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;

import io.grpc.StatusRuntimeException;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ApiResourceTest {
	
	private static Thread serviceThread;
	private OkHttpClient client = new OkHttpClient();
	
	@BeforeClass
	public static void startService() {
		serviceThread = new Thread(new Runnable() {

			@Override
			public void run() {
				new TestService().run();
			}
			
		});
		serviceThread.start();
	}
	
	@AfterClass
	public static void stopService() {
		serviceThread.interrupt();
	}
	
	@Test
	public void service_api_resource_published() throws IOException {
		waitForService();
		
		Request req = new Request.Builder().url("http://localhost:8080/test-resource").build();
		okhttp3.Response response = client.newCall(req).execute();
		
		assertThat(response.code(), equalTo(200));		
	}

	private void waitForService() {
		await().atMost(10, TimeUnit.SECONDS).ignoreExceptionsInstanceOf(StatusRuntimeException.class).until(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				try (Socket socket = new Socket()) {
			        socket.connect(new InetSocketAddress("localhost", 8080), 500);
			        return true;
			    } catch (IOException e) {
			        return false;
			    }
			}
		});
	}
	
	private static final class TestService extends Service {

		@Override
		public Module[] getModules() {
			return new Module[] { new ApiModule() };
		}
		
	}
	
	private static final class ApiModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(TestResource.class).in(Scopes.SINGLETON);
		}
		
	}
	
	@Path("test-resource")
	public static final class TestResource {
		
		@GET
		@Produces("application/json")
		public Response test() {
			return Response.ok().build();
		}
	}

}
