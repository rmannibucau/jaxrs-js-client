package com.github.rmannibuau.jaxrs.js.installer;

import com.github.rmannibuau.jaxrs.js.handler.JsInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.server.cxf.rs.event.ServerCreated;

public class JsClientExtension {
    public void install(@Observes final ServerCreated serverCreated) {
        final Endpoint endpoint = serverCreated.getServer().getEndpoint();
        endpoint.getInInterceptors().add(new JsInterceptor(serverCreated.getMapping()));
    }
}
