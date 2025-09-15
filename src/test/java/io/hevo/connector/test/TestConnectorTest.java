package io.hevo.connector.test;

import io.hevo.connector.model.ObjectStats;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class TestConnectorTest {

  @Test
  void testFetchSchemaStatsReturnsEmptyList() {
    try (TestConnector testConnector = new TestConnector()) {
      List<ObjectStats> objectStats = testConnector.fetchSchemaStatsFromSource();
      Assertions.assertTrue(objectStats.isEmpty(), "Expected empty schema stats list");
    }
  }
}
