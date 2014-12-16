package com.github.rmannibuau.jaxrs.js.generator;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;

import java.util.List;

/**
 * creates a module "client" based on $http, module name is taken from JAX-RS Application simplename
 * and service is the same suffixed with Client.
 */
public class AngularJs1ClientGenerator extends BaseClientGenerator {
    public String generate(final String module, final List<ClassResourceInfo> cris, final String context) {
        final StringBuilder builder = new StringBuilder("(function () {");
        builder.append("angular.module('").append(module).append("', [])");
        builder.append(".factory('").append(module).append("Client").append("', ['$http', function ($http) {");
        super.generate(builder, cris, context, "undefined");
        builder.append("}]);");
        builder.append("})();");
        return builder.toString();
    }

    @Override
    protected void createClientHolder(final StringBuilder builder) {
        builder.append("var _client={};");
    }

    @Override
    protected void createDelegate(final StringBuilder builder) {
        builder.append("var client = new Client(_client.base, _client.headers);");
    }

    @Override
    protected void generateDelegateClient(final StringBuilder builder) {
        builder.append("var Client = function (base,headers) {");
        builder.append("var headerCopy = angular.extend({}, headers);");
        builder.append("return {");
        builder.append("data: undefined,");
        builder.append("headers: headerCopy,");
        builder.append("request: function(method, url, query, matrix){");
        builder.append("var params={};");
        builder.append("params.url=base+url+(matrix?(';'+matrix):'')+(query?('?'+query):'');");
        builder.append("params.method=method;");
        builder.append("if (this.data){params.data=this.data}");
        builder.append("if(this.headers){params.headers=angular.extend({}, headers, this.headers)}");
        builder.append("return $http(params);");
        builder.append("}};};");
    }
}
