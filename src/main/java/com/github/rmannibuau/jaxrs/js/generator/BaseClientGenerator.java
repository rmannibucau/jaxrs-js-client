package com.github.rmannibuau.jaxrs.js.generator;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.Parameter;
import org.apache.cxf.jaxrs.model.ParameterType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class BaseClientGenerator {
    protected abstract void createDelegate(StringBuilder builder);

    protected abstract void createClientHolder(StringBuilder builder);

    protected abstract void generateDelegateClient(StringBuilder builder);

    protected void generate(final StringBuilder builder, final List<ClassResourceInfo> cris, final String context, final String undefined) {
        generateDelegateClient(builder);

        builder.append("var appendString = function(str, name, value, defaultValue, sep){");
        builder.append("var val = value ? value : defaultValue;");
        builder.append("return (str && str.length>0 && val?(str+sep):'')+(val?(name+'='+encodeURIComponent(val)):'');");
        builder.append("};");

        createClientHolder(builder);
        builder.append("_client.headers={};");
        builder.append("_client.base='").append(context).append("';");

        for (final ClassResourceInfo cri : cris) {
            builder.append("_client.").append(cri.getResourceClass().getSimpleName()).append("={");

            String classPath = cri.getURITemplate().getValue();
            if (classPath.endsWith("/")) {
                classPath = classPath.substring(0, classPath.length() - 1);
            }

            final List<OperationResourceInfo> operationResourceInfos = new ArrayList<OperationResourceInfo>(cri.getMethodDispatcher().getOperationResourceInfos());
            Collections.sort(operationResourceInfos, new Comparator<OperationResourceInfo>() {
                @Override
                public int compare(final OperationResourceInfo o1, final OperationResourceInfo o2) {
                    if (o1.getHttpMethod() == null) {
                        if (o2.getHttpMethod() == null) {
                            return o1.getURITemplate().getValue().compareTo(o2.getURITemplate().getValue());
                        }
                        return 1;
                    }

                    final int method = o1.getHttpMethod().compareTo(o2.getHttpMethod());
                    if (method == 0) {
                        return o1.getURITemplate().getValue().compareTo(o2.getURITemplate().getValue());
                    }
                    return method;
                }
            });

            if (!operationResourceInfos.isEmpty()) {
                for (final OperationResourceInfo ori : operationResourceInfos) {
                    if (ori.getHttpMethod() == null) {
                        continue; // not yet supported
                    }

                    builder.append(ori.getMethodToInvoke().getName()).append(":function (");

                    boolean removeLastComma = false;
                    for (final Parameter parameter : ori.getParameters()) {
                        if (isIgnored(parameter)) {
                            continue;
                        }

                        builder.append(paramName(parameter)).append(',');
                        removeLastComma = true;
                    }
                    if (removeLastComma) {
                        builder.setLength(builder.length() - 1);
                    }

                    builder.append(") {");

                    String resourcePath = ori.getURITemplate().getValue();
                    if (resourcePath.startsWith("/")) {
                        resourcePath = resourcePath.substring(1, resourcePath.length());
                    }

                    createDelegate(builder);
                    builder.append("var path='").append(classPath).append('/').append(resourcePath).append("';");
                    builder.append("var query=").append(undefined).append(";");
                    builder.append("var matrix=").append(undefined).append(";");
                    for (final Parameter parameter : ori.getParameters()) {
                        final String paramName = paramName(parameter);
                        switch (parameter.getType()) {
                            case PATH:
                                builder.append("path = path.replace(").append("'{").append(paramName).append("}', ").append(paramName).append(");");
                                break;

                            case QUERY:
                                builder.append(appendParam("query", parameter, paramName, "&", undefined));
                                break;

                            case HEADER:
                                builder.append("if (").append(paramName).append("){client.headers.").append(paramName).append('=').append(paramName).append(";}");
                                break;

                            case FORM:
                                builder.append(appendParam("client.data", parameter, paramName, "&", undefined));
                                break;

                            case MATRIX:
                                builder.append(appendParam("matrix", parameter, paramName, ";", undefined));
                                break;

                            case REQUEST_BODY:
                                builder.append("client.data = ").append(paramName).append(";");
                                break;

                            default:
                        }
                    }
                    builder.append("return client.request('").append(ori.getHttpMethod()).append("',path,query,matrix").append(");");
                    builder.append("},");
                }
                builder.setLength(builder.length() - 1);
            }
            builder.append("};");
        }
        builder.append("return _client;");
    }

    private StringBuilder appendParam(final String var, final Parameter parameter, final String paramName, final String sep, final String undefined) {
        return new StringBuilder(var).append("=appendString(").append(var).append(",'").append(paramName).append("',")
                .append(paramName).append(',').append(parameter.getDefaultValue() != null ? '\'' + parameter.getDefaultValue() + "'": undefined)
                .append(",'").append(sep).append("');");
    }

    private String paramName(final Parameter parameter) {
        final String parameterName = parameter.getName();
        return parameterName == null ? "body" : parameterName;
    }

    private boolean isIgnored(final  Parameter parameter) {
        return parameter.getType() == ParameterType.COOKIE ||
                parameter.getType() == ParameterType.CONTEXT ||
                parameter.getType() == ParameterType.UNKNOWN;
    }
}
