package com.github.rmannibuau.jaxrs.js.installer;

import com.github.rmannibuau.jaxrs.js.handler.AngularJSInterceptor;
import com.github.rmannibuau.jaxrs.js.handler.JQueryInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.server.cxf.rs.event.ServerCreated;

import static java.util.Arrays.asList;

public class JsClientExtension {
    public void install(@Observes final ServerCreated serverCreated) {
        final Endpoint endpoint = serverCreated.getServer().getEndpoint();
        endpoint.getInInterceptors().addAll(asList(
            new JQueryInterceptor(serverCreated.getMapping()),
            new AngularJSInterceptor(serverCreated.getMapping())));
    }
}
