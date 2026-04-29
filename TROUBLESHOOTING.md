# Troubleshooting

Common issues you may encounter when working with this library.

## Exception: "No test method found in call stack"

Verify that:
- You're calling Foremka from a test method
- The test method is correctly annotated
- The test has not forked into another thread or process

Note: Foremka supports annotations up to and including JUnit 6.

There are two fallback mechanisms available:
1. Adjust TestNameDetector.ANNOTATION_CLASSES set with your own annotations
2. Set TestNameDetector.OVERRIDE_TEST_NAME manually to your current test name

## Scenarios are not loaded from repository

Usually caused by a misconfigured ScenarioRepository (File or Database), or by a deserialization error.

**For File repository, verify:**
- The path is correct
- You have write access to the file
- The file is not locked by another process
- The file is not corrupted

**For Database repository, verify:**
- The database URL, schema, and credentials are configured correctly
- Your user has proper access to the schema
- The database is up and running
- You can manually connect and run queries
- You're using a supported database (check your database repository implementation)

**For deserialization, verify:**
- Test scenarios stored in the repository are not corrupted and are valid JSON

## Warn: "Skipping scenario type that cannot be resolved"

Logged when a scenario class does not exist in the classpath.  
This typically happens when you remove or rename a scenario or input class during development.  
This warning is non-fatal; the library will recreate missing scenarios automatically as needed.

## Warn: "Failed to deserialize scenario entry of type"

Logged when a scenario class exists but cannot be deserialized.  
This typically happens when you make non-recoverable structural changes to a scenario or input class.  
This warning is non-fatal; the library will recreate affected scenarios automatically as needed.
