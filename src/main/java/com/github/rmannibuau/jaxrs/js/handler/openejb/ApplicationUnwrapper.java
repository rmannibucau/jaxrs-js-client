package com.github.rmannibuau.jaxrs.js.handler.openejb;

import org.apache.openejb.server.rest.InternalApplication;

import javax.ws.rs.core.Application;

// split to avoid issues if openejb is not available
public final class ApplicationUnwrapper {
    public static Application unwrap(final Object app) {
        return InternalApplication.class.cast(app).getOriginal();
    }

    private ApplicationUnwrapper() {
        // no-op
    }
}
