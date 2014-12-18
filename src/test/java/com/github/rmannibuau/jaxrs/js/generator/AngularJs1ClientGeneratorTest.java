package com.github.rmannibuau.jaxrs.js.generator;

import com.github.rmannibucau.rules.api.phantomjs.PhantomJsRule;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposerRule;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testing.SimpleLog;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import javax.script.ScriptException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;

@SimpleLog
@EnableServices("jaxrs")
public class AngularJs1ClientGeneratorTest {
    @Rule
    public final ApplicationComposerRule rule = new ApplicationComposerRule(this);

    @ClassRule
    public static final PhantomJsRule PHANTOM_JS = new PhantomJsRule();

    @RandomPort("httpejbd")
    private URL httpEjbdPort;

    @Module
    @Classes(innerClassesAsBean = true)
    public WebApp war() {
        return new WebApp();
    }

    @Test
    public void js() throws IOException, ScriptException, InterruptedException {
        final String home = httpEjbdPort.toExternalForm() + "openejb/home";
        final PhantomJSDriver driver = PHANTOM_JS.getDriver();
        driver.get(home);
        driver.executeAsyncScript( // wait for angular to have done its work
                "var callback = arguments[arguments.length - 1];" +
                        "var e1 = document.querySelector('body');" +
                        "if (window.angular) {" +
                        "angular.element(e1).injector().get('$browser').notifyWhenNoOutstandingRequests(callback);" +
                        "} else {callback()}"
        );
        final String pageSource = driver.getPageSource();
        assertTrue(pageSource, pageSource.contains("\"base\":\"/openejb/api\""));
        assertTrue(pageSource, pageSource.contains("\"headers\":{\"Content-Type\":\"text/plain\"}"));
        assertTrue(pageSource, pageSource.contains("\"AResource\":"));
        assertTrue(pageSource, pageSource.contains("header1header2path 1path2query1query2matrix1matrix 2"));
    }

    @Test
    public void raw() throws IOException {
        final String js = IO.slurp(new URL(httpEjbdPort.toExternalForm() + "openejb/api?angularjsclient"));
        assertTrue(js.contains("(function () {angular.module('App', []).factory('AppClient', "));
    }

    @ApplicationPath("api")
    public static class App extends Application {}

    @Path("the-resource")
    public static class AResource {
        @GET
        @Path("{p}/{p2}")
        @Produces(MediaType.TEXT_PLAIN) // angular is not as nice as jquery to guess the type
        public String aGet(@HeaderParam("h") String h,@HeaderParam("h2") String h2,
                           @PathParam("p") String p,@PathParam("p2") String p2,
                           @QueryParam("q") String q,@QueryParam("q2") String q2,
                           @MatrixParam("m") String m,@MatrixParam("m2") String m2) {
            return h + h2 + p + p2 + q + q2 + m + m2;
        }

        @HEAD
        public void aHead(@HeaderParam("h") String h) {
            // no-op
        }

        @DELETE
        public void aDelete(@HeaderParam("h") String h) {
            // no-op
        }

        @POST
        public String aPost(@HeaderParam("h") String h, final String body) {
            return null;
        }

        @PUT
        public String aPut(@HeaderParam("h") String h, final String body) {
            return null;
        }
    }

    @WebServlet(urlPatterns = "/home")
    public static class Home extends HttpServlet {
        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write(
                    "<html ng-app=\"myapp\">" +
                    "<head>" +
                        "<script src=\"angularjs.js\"></script>" +
                        "<script src=\"api?angularjsclient\"></script>" +
                    "</head>" +
                    "<body>" +
                    "<div ng-view>" +
                    "       {{ data }}" +
                    "       {{ client }}" +
                    "</div>" +
                    "<div id=\"messages\"></div>" +
                    "<script>" +
                        "(function () {" +
                            "angular.module('myapp', ['App'])" +
                            "  .run(['$rootScope', 'AppClient', function ($scope, AppClient) {" +
                            "     AppClient.headers={'Content-Type':'text/plain'};" +
                            "     $scope.client = AppClient;" +
                            "     $scope.data = AppClient.AResource.aGet('header1', 'header2', 'path 1', 'path2', 'query1', 'query2', 'matrix1', 'matrix 2')" +
                            "       .success(function (data) {" +
                            "           $scope.data = data;" +
                            "       })" +
                            "       .error(function(data, status, headers, config) { /* debug */" +
                            "           angular.element('#messages').innerHTML = data + status;" +
                            "       });" +
                            "}]);" +
                        "})();" +
                    "</script>" +
                    "</body>" +
                    "</html>"
            );
        }
    }

    @WebServlet(urlPatterns = "/angularjs.js")
    public static class Angular extends HttpServlet {
        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("application/javascript");
            final String jquery = IO.slurp(Thread.currentThread().getContextClassLoader().getResourceAsStream("angular-1.3.7.js"));
            resp.getWriter().write(jquery);
        }
    }
}
