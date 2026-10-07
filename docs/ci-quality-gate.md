# CI Selenium Quality Gate Demo

## Ensure app is running

```powershell
cd C:\Users\Yash\Desktop\rec-platform\backend
mvn spring-boot:run
```

In another PowerShell:

```powershell
cd C:\Users\Yash\Desktop\rec-platform\frontend
npm.cmd run dev
```

## Run the Selenium suite

```powershell
cd C:\Users\Yash\Desktop\rec-platform\backend
mvn test
```

Expected result:

```text
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Introduce an intentional assertion defect

Edit this line in
`backend/src/test/java/com/platform/recs/e2e/EndToEndTests.java`:

```java
assertTrue(driver.getCurrentUrl().contains("/assets"), "Generator should redirect to assets");
```

Change it to:

```java
assertTrue(driver.getCurrentUrl().contains("/wrong-assets"), "Generator should redirect to assets");
```

Run:

```powershell
cd C:\Users\Yash\Desktop\rec-platform\backend
mvn test
```

Expected result:

```text
[ERROR] Tests run: 7, Failures: 1, Errors: 0, Skipped: 0
[ERROR] EndToEndTests.journey1_authenticationRegistrationAndRoleBasedRedirect
[INFO] BUILD FAILURE
```

This also produces:

```text
backend/target/screenshots/journey1_authenticationRegistrationAndRoleBasedRedirect___failure.png
```

## Fix the defect

Restore:

```java
assertTrue(driver.getCurrentUrl().contains("/assets"), "Generator should redirect to assets");
```

Run:

```powershell
cd C:\Users\Yash\Desktop\rec-platform\backend
mvn test
```

Expected result:

```text
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Then `mvn clean package`:

```powershell
cd C:\Users\Yash\Desktop\rec-platform\backend
mvn clean package
```

Expected result:

```text
[INFO] BUILD SUCCESS
```
