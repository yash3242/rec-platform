package com.recplatform.e2e;

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
        if (exception.isPresent() && staticDriver instanceof TakesScreenshot) {
            Path screenshotsDir = Paths.get("target", "screenshots");
            Files.createDirectories(screenshotsDir);
            File src = ((TakesScreenshot) staticDriver).getScreenshotAs(OutputType.FILE);
            String testName = context.getDisplayName().replaceAll("[^a-zA-Z0-9-_]", "_");
            Path destination = screenshotsDir.resolve(testName + "_failure.png");
            Files.deleteIfExists(destination);
            Files.copy(src.toPath(), destination);
            System.err.println("Screenshot saved: " + destination.toAbsolutePath());
        }
    }
}
