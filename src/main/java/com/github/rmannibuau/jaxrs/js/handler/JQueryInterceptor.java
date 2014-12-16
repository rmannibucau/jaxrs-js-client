package com.github.rmannibuau.jaxrs.js.handler;

import com.github.rmannibuau.jaxrs.js.generator.JQueryClientGenerator;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.JAXRSServiceImpl;
import org.apache.cxf.jaxrs.impl.UriInfoImpl;
import org.apache.cxf.jaxrs.interceptor.JAXRSInInterceptor;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JQueryInterceptor extends AbstractPhaseInterceptor<Message> {
    private final JQueryClientGenerator delegate = new JQueryClientGenerator();

    private final ConcurrentMap<Key, String> cache = new ConcurrentHashMap<Key, String>();
    private final String context;

    public JQueryInterceptor(final String context) {
        super(Phase.UNMARSHAL);
        addBefore(JAXRSInInterceptor.class.getName());
        this.context = context;
    }

    @Override
    public void handleMessage(final Message message) throws Fault {
        final Service service = message.getExchange().get(Service.class);
        final List<ClassResourceInfo> resources = JAXRSServiceImpl.class.cast(service).getClassResourceInfos();

        MultivaluedMap<String, String> queryParameters;
        if ("GET".equals(message.get(Message.HTTP_REQUEST_METHOD)) && (queryParameters = new UriInfoImpl(message, null).getQueryParameters()).containsKey("jqueryclient")) {
            final String jsClient = queryParameters.getFirst("jqueryclient");
            final String namespace = queryParameters.getFirst("jsnamespace");
            String generated = cache.get(new Key(service, jsClient, namespace));
            if (generated == null) {
                generated = delegate.generate(namespace, jsClient, resources, context);
                cache.putIfAbsent(new Key(service, jsClient, namespace), generated);
            }
            message.getExchange().put(Response.class, Response.ok(generated).type("application/javascript").build());
        }
    }

    private static final class Key {
        private final Service service;
        private final String name;
        private final String namespace;
        private final int hash;

        private Key(final Service service, final String name, final String namespace) {
            this.service = service;
            this.name = name;
            this.namespace = namespace;

            int result = service.hashCode();
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
            this.hash = result;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || Key.class != o.getClass()) {
                return false;
            }

            final Key key = Key.class.cast(o);
            return !(name != null ? !name.equals(key.name) : key.name != null)
                    && !(namespace != null ? !namespace.equals(key.namespace) : key.namespace != null)
                    && service.equals(key.service);

        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
