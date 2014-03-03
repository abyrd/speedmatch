package com.goabout.speedmatch;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ApplicationHandler;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Main {
	
	static {
        // Remove existing handlers attached to the j.u.l root logger
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        // Bridge j.u.l (used by Jersey) to the SLF4J root logger
        SLF4JBridgeHandler.install();
    }
	
	public static void main(String[] args) throws IOException,
			URISyntaxException {
		final URI uri = new URI("http://localhost:3232");
		System.out.println("Starting grizzly...");
		ApplicationHandler appHandler = new ApplicationHandler(
				SpeedMatchApplication.class);
		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri,
				appHandler);
		server.start();
		System.in.read();
		System.exit(0);
	}
}

// ResourceConfig rcfg =
// ResourceConfig.forApplicationClass(SpeedMatchApplication.class);
// HttpHandler handler = RuntimeDelegate.getInstance()
// .createEndpoint(new SpeedMatchApplication(), HttpHandler.class);
