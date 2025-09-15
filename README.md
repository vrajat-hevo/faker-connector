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

- plugins
- downloadCredentials
- spotless
- tasks (jar, shadowJar)

# Open Questions
- How does a developer get the latest version of hevo-sdk?
