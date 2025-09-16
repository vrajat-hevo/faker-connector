# faker-connector
A Hevo SDK Connector to generate fake data.

# Getting Started

```declarative
git clone git@github.com:vrajat-hevo/faker-connector.git
cd faker-connector
cp -r ../hevo-sdk/test-connector/* .
```

## Gradle Configuration

Major sections in `build.gradle.kts` are missing. The following sections were copied from `sqlserver-connector` and modified to suit this connector. 

Add `gradle.properties` and `settings.gradle.kts` files. 

## Testing

### Dependency Injection makes it hard to setup unit tests. 

Example:

```declarative
    FakerConnector connector = new FakerConnector();
    String inlineSchema =
        "{\"name\":\"TestSchema\",\"tables\":{\"TestTable\":{\"name\":\"TestTable\",\"columns\":[{\"name\":\"id\",\"type\":\"INT\"},{\"name\":\"value\",\"type\":\"STRING\"}]}}}";
    // Use reflection to set private fields
    try {
      java.lang.reflect.Field schemaSourceField =
          FakerConnector.class.getDeclaredField("schemaSource");
      schemaSourceField.setAccessible(true);
      schemaSourceField.set(connector, "INLINE");
      java.lang.reflect.Field inlineSchemaField =
          FakerConnector.class.getDeclaredField("inlineSchema");
      inlineSchemaField.setAccessible(true);
      inlineSchemaField.set(connector, inlineSchema);
    } catch (Exception e) {
      fail("Reflection failed: " + e.getMessage());
    }
    connector.initializeConnection();
```

Is there a better way or documented way for Dependency Injection in tests?

### GenericTestConnector

- GenericTestConnector is not part of `hevo-sdk` and files have to be copied over. Is there a better way to reuse this code?
- It is in main/source code root. Should it be in test/source code root along with an example of how to use it.
- Also there is no way to integrate it with mvn/gradle test framework.
- Configuration requires reflection. Code is available but developer may have to understand it to debug config issues.
- executeConnector uses prompts to test different features.
- executeConnector has to be edited to manage offsets.
- Run through Intellij (easiest)
- 

# Open Questions
- How does a developer get the latest version of hevo-sdk?
- - I checked the releases tab in github repo.