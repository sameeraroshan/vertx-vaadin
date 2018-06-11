package com.exmple.demo;


import com.exmple.demo.services.QuoteDeplymentVerticle;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class ApplicationServiceInitListener
        implements VaadinServiceInitListener {

    static {
        QuoteDeplymentVerticle.deploy();
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {



        event.addBootstrapListener(response -> {
            // BoostrapListener to change the bootstrap page
            System.out.println("system response");
        });

        event.addDependencyFilter((dependencies, filterContext) -> {
            // DependencyFilter to add/remove/change dependencies sent to
            // the client
            return dependencies;
        });

        event.addRequestHandler((session, request, response) -> {
            // RequestHandler to change how responses are handled
            return false;
        });
    }

}