package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

/**
 * ============================================================
 *  Main.java  —  Application Entry Point
 * ============================================================
 * Starts an embedded Grizzly HTTP server.
 * All endpoints are available under http://localhost:8080/api/v1/
 */
public class Main {

    public static final String BASE_URI = "http://0.0.0.0:8080/api/v1/";
    public static final String DISPLAY_URI = "http://localhost:8080/api/v1/";

    public static void main(String[] args) throws Exception {

        ResourceConfig config = new ResourceConfig()
                .packages(
                        "com.smartcampus.resources",
                        "com.smartcampus.exceptions",
                        "com.smartcampus.filters"
                )
                .register(JacksonFeature.class);

        HttpServer server = GrizzlyHttpServerFactory
                .createHttpServer(URI.create(BASE_URI), config);

        System.out.println("================================================");
        System.out.println("  Smart Campus API is RUNNING!");
        System.out.println("  Discovery : " + DISPLAY_URI);
        System.out.println("  Rooms     : http://localhost:8080/api/v1/rooms");
        System.out.println("  Sensors   : http://localhost:8080/api/v1/sensors");
        System.out.println("================================================");
        System.out.println("  STOP: click the red [Stop] button in NetBeans");
        System.out.println("        OR press Ctrl+C in the Output window.");
        System.out.println("================================================");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutdown signal received — stopping server...");
            server.shutdownNow();
            System.out.println("Server stopped. Port 8080 is now free.");
        }, "shutdown-hook"));

        Thread.currentThread().join();
    }
}
