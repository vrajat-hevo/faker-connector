package io.hevo.connector.faker;

import static org.junit.jupiter.api.Assertions.*;

import io.hevo.connector.exceptions.ConnectorException;
import io.hevo.connector.model.ObjectDetails;
import io.hevo.connector.model.ObjectSchema;
import io.hevo.connector.model.enums.SourceObjectStatus;
import io.hevo.connector.model.field.data.datum.hudt.HStruct;
import io.hevo.connector.model.field.schema.base.Field;

import java.util.*;

import org.junit.jupiter.api.Test;

class FakerConnectorTest {
  @Test
  void testInitializeConnectionWithInlineSchema() {
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
    FakerConnector.Schema schema = connector.getSchema();
    assertNotNull(schema);
    assertEquals("TestSchema", schema.name());
    assertTrue(schema.tables().containsKey("TestTable"));
    FakerConnector.Table table = schema.tables().get("TestTable");
    assertEquals("TestTable", table.name());
    assertEquals(2, table.columns().size());
    assertEquals("id", table.columns().get(0).name());
    assertEquals("INT", table.columns().get(0).type());
    assertEquals("value", table.columns().get(1).name());
    assertEquals("STRING", table.columns().get(1).type());
  }

  @Test
  void testGetObjectsWithSetSchema() {
    FakerConnector connector = getFakerConnector();
    List<ObjectDetails> objects = connector.getObjects();
    assertNotNull(objects);
    assertEquals(1, objects.size());
    ObjectDetails obj = objects.get(0);
    assertEquals("Faker", obj.catalogName());
    assertEquals("TestSchema", obj.schemaName());
    assertEquals("TestTable", obj.tableName());
    assertEquals("TABLE", obj.type());
    assertEquals(SourceObjectStatus.ACTIVE, obj.sourceObjectStatus());
  }

  @Test
  void testFetchSchemaFromSource() throws Exception {
    FakerConnector connector = getFakerConnector();
    ObjectDetails objDetails =
        ObjectDetails.builder()
            .catalog("Faker")
            .schema("TestSchema")
            .table("TestTable")
            .type("TABLE")
            .sourceObjectStatus(SourceObjectStatus.ACTIVE)
            .build();
    List<ObjectDetails> objDetailsList = List.of(objDetails);
    List<ObjectSchema> schemas = connector.fetchSchemaFromSource(objDetailsList);
    assertNotNull(schemas);
    assertEquals(1, schemas.size());
    ObjectSchema objectSchema = schemas.get(0);
    ObjectDetails details = objectSchema.objectDetail();
    assertEquals("Faker", details.catalogName());
    assertEquals("TestSchema", details.schemaName());
    assertEquals("TestTable", details.tableName());
    assertEquals("TABLE", details.type());
    assertEquals(SourceObjectStatus.ACTIVE, details.sourceObjectStatus());
    Set<Field> fields = objectSchema.fields();
    assertEquals(2, fields.size());
    boolean foundId = false, foundValue = false;
    for (io.hevo.connector.model.field.schema.base.Field field : fields) {
      if (field.name().equals("id")) {
        assertEquals("hudt_integer", field.logicalType());
        assertEquals(
            io.hevo.connector.model.field.schema.enumeration.FieldState.ACTIVE, field.status());
        foundId = true;
      } else if (field.name().equals("value")) {
        assertEquals("hudt_varchar", field.logicalType());
        assertEquals(
            io.hevo.connector.model.field.schema.enumeration.FieldState.ACTIVE, field.status());
        foundValue = true;
      }
    }
    assertTrue(foundId);
    assertTrue(foundValue);
  }

  @Test
  void testGenerateRows() throws ConnectorException {
    FakerConnector connector = getFakerConnector();
    List<ObjectSchema> schemas = connector.fetchSchemaFromSource(connector.getObjects());
    assertEquals(1, schemas.size());
    ObjectSchema objectSchema = schemas.get(0);
    List<Field> selectedFields =
        objectSchema.fields().stream()
            .sorted(Comparator.comparingInt(f -> f.properties().position()))
            .toList();

    List<HStruct> rows = connector.generateRows(objectSchema, selectedFields);
    assertNotNull(rows);
    assertEquals(100, rows.size());
  }

  private static FakerConnector getFakerConnector() {
    FakerConnector connector = new FakerConnector();
    // Prepare schema
    List<FakerConnector.Column> columns =
        List.of(
            new FakerConnector.Column("id", "INT"), new FakerConnector.Column("value", "STRING"));
    FakerConnector.Table table = new FakerConnector.Table("TestTable", columns);
    Map<String, FakerConnector.Table> tables = new HashMap<>();
    tables.put("TestTable", table);
    FakerConnector.Schema schema = new FakerConnector.Schema("TestSchema", tables);
    connector.setSchema(schema);
    return connector;
  }
}
