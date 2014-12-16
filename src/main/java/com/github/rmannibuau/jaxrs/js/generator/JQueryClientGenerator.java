package com.github.rmannibuau.jaxrs.js.generator;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;

import java.util.List;

public class JQueryClientGenerator extends BaseClientGenerator {
    public String generate(final String prefix, final String rawName, final List<ClassResourceInfo> cris, final String context) {
        final boolean hasPrefix = prefix != null && !prefix.isEmpty();
        final String name = (hasPrefix ? prefix + '.' : "") + (rawName == null || rawName.isEmpty() ? "jsclient" : rawName);
        final StringBuilder builder = new StringBuilder(!hasPrefix ? "var " : "").append(name).append("=(function(_client,u) {");
        super.generate(builder, cris, context, "u");
        builder.append("})(").append(name).append(" || {}, undefined);");
        return builder.toString();
    }

    @Override
    protected void createDelegate(final StringBuilder builder) {
        builder.append("var client = new Client(_client.base, _client.headers,_client.settings);");
    }

    @Override
    protected void createClientHolder(final StringBuilder builder) {
        // no-op: done through IIFE params
    }

    @Override
    protected void generateDelegateClient(final StringBuilder builder) {
        builder.append("var Client = function (base,headers,settings) {");
        builder.append("var headerCopy = $.extend({}, headers);");
        builder.append("return {");
        builder.append("data: u,");
        builder.append("headers: headerCopy,");
        builder.append("request: function(method, url, query, matrix){");
        builder.append("var params={};");
        builder.append("params.url=base+url+(matrix?(';'+matrix):'')+(query?('?'+query):'');");
        builder.append("params.type=method;");
        builder.append("if (this.data){params.data=this.data}");
        builder.append("if(this.headers){params.headers=$.extend({}, headers, this.headers)}");
        builder.append("if(settings){params.settings=settings;}");
        builder.append("return $.ajax(params);");
        builder.append("}};};");
    }
}
