package com.platform.recs.e2e;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ScreenshotOnFailureListener implements AfterEachCallback {
    private static WebDriver staticDriver;

    public static void setDriver(WebDriver driver) {
        staticDriver = driver;
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Optional<Throwable> exception = context.getExecutionException();
        if (exception.isPresent() && staticDriver != null) {
            Path screenshotsDir = Paths.get("target", "screenshots");
            Files.createDirectories(screenshotsDir);
            String testName = context.getDisplayName().replaceAll("[^a-zA-Z0-9-_]", "_");
            Path destination = screenshotsDir.resolve(testName + "_failure.png");
            Files.deleteIfExists(destination);
            if (staticDriver instanceof org.openqa.selenium.chrome.ChromeDriver) {
                try {
                    org.openqa.selenium.chrome.ChromeDriver chrome = (org.openqa.selenium.chrome.ChromeDriver) staticDriver;
                    java.util.Map<String, Object> params = new java.util.HashMap<>();
                    params.put("format", "png");
                    params.put("captureBeyondViewport", true);
                    Object result = chrome.executeCdpCommand("Page.captureScreenshot", params);
                    if (result instanceof java.util.Map) {
                        Object data = ((java.util.Map<?, ?>) result).get("data");
                        if (data instanceof String) {
                            Files.write(destination, java.util.Base64.getDecoder().decode((String) data));
                            System.err.println("Full-page screenshot saved: " + destination.toAbsolutePath());
                            return;
                        }
                    }
                } catch (Throwable ignored) {
                    // Fall back to viewport screenshot below
                }
            }
            if (staticDriver instanceof TakesScreenshot) {
                File src = ((TakesScreenshot) staticDriver).getScreenshotAs(OutputType.FILE);
                Files.copy(src.toPath(), destination);
                System.err.println("Screenshot saved: " + destination.toAbsolutePath());
            }
        }
    }
}
