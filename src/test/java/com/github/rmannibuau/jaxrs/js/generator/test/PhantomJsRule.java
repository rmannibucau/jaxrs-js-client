package com.github.rmannibuau.jaxrs.js.generator.test;

import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.Zips;
import org.apache.ziplock.JarLocation;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class PhantomJsRule implements TestRule {
    private PhantomJSDriver driver;

    @Override
    public Statement apply(final Statement statement, final Description description) {
        final File phantomJs = new File("target/phantomjs");
        Files.mkdirs(phantomJs);
        try {
            Zips.unzip(JarLocation.jarFromPrefix("arquillian-phantom-binary"), phantomJs);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        final DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        final File exec = new File(phantomJs, "bin/phantomjs" + (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win") ? ".exe" : ""));
        exec.setExecutable(true);
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                exec.getAbsolutePath());
        final PhantomJSDriverService service = PhantomJSDriverService.createDefaultService(capabilities);
        driver = new PhantomJSDriver(service, capabilities);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                service.start();
                try {
                    statement.evaluate();
                } finally {
                    service.stop();
                }
            }
        };
    }

    public PhantomJSDriver getDriver() {
        return driver;
    }
}
