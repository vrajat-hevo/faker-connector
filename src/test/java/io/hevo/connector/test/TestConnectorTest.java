package io.hevo.connector.test;

import io.hevo.connector.model.ObjectStats;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestConnectorTest {

  @Test
  void testFetchSchemaStatsReturnsEmptyList() {
    try (TestConnector testConnector = new TestConnector()) {
      List<ObjectStats> objectStats = testConnector.fetchSchemaStatsFromSource();
      Assertions.assertTrue(objectStats.isEmpty(), "Expected empty schema stats list");
    }
  }
}
