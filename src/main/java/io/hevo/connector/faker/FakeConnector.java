package io.hevo.connector.faker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hevo.connector.GenericConnector;
import io.hevo.connector.exceptions.ConnectorException;
import io.hevo.connector.model.*;
import io.hevo.connector.model.enums.OpType;
import io.hevo.connector.model.enums.SourceObjectStatus;
import io.hevo.connector.model.field.data.datum.hudt.*;
import io.hevo.connector.model.field.schema.base.Field;
import io.hevo.connector.model.field.schema.enumeration.FieldState;
import io.hevo.connector.model.field.schema.hudt.*;
import io.hevo.connector.processor.ConnectorProcessor;
import io.hevo.connector.ui.Group;
import io.hevo.connector.ui.Property;
import io.hevo.connector.ui.PropertyType;
import java.io.File;
import java.io.IOException;
import java.util.*;
import net.datafaker.Faker;

@Group(type = Group.Type.CONNECTION, title = "Faker Connector", order = 1)
public class FakeConnector implements GenericConnector {
  @Property(
      name = "Schema Source",
      displayName = "Schema Source",
      defaultValue = "INLINE",
      fieldOrder = 1,
      optionsRef =
          @io.hevo.connector.ui.OptionsRef(
              type = io.hevo.connector.ui.OptionsRefType.STATIC,
              allowedValues = {"INLINE", "FILE"}))
  private String schemaSource;

  @Property(
      name = "Inline Schema",
      displayName = "Inline Schema",
      defaultValue =
          "{\"type\": \"record\", \"name\": \"TestRecord\", \"fields\": [{\"name\": \"id\", \"type\": \"string\"}, {\"name\": \"value\", \"type\": \"int\"}]}",
      type = PropertyType.STRING,
      fieldOrder = 2,
      editable = true)
  private String inlineSchema;

  @Property(
      name = "File Path",
      displayName = "File Path",
      defaultValue = "",
      type = PropertyType.STRING,
      fieldOrder = 3)
  private String filePath;

  record Column(String name, String type) {}

  record Table(String name, List<Column> columns) {}

  record Schema(String name, Map<String, Table> tables) {}

  private Schema schema;

  @Override
  public void initializeConnection() {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      if ("INLINE".equals(schemaSource)) {
        // Read and parse schema from inlineSchema
        schema = objectMapper.readValue(inlineSchema, Schema.class);
      } else if ("FILE".equals(schemaSource)) {
        // Read and parse schema from filePath
        schema = objectMapper.readValue(new File(filePath), Schema.class);
      } else {
        throw new IllegalArgumentException("Unknown schema source: " + schemaSource);
      }
      // Optionally, store or use the parsed table as needed
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse schema", e);
    }
  }

  @Override
  public java.util.List<ObjectDetails> getObjects() {
    if (schema == null || schema.tables() == null) {
      return java.util.Collections.emptyList();
    }
    List<ObjectDetails> objects = new ArrayList<>();
    for (Map.Entry<String, Table> entry : schema.tables.entrySet()) {
      ObjectDetails.builder()
          .catalog("Faker")
          .schema(schema.name())
          .table(entry.getKey())
          .type("TABLE")
          .sourceObjectStatus(SourceObjectStatus.ACTIVE)
          .build();
    }
    return objects;
  }

  @Override
  public List<ObjectStats> fetchSchemaStatsFromSource() throws ConnectorException {
    return List.of();
  }

  @Override
  public List<ObjectSchema> fetchSchemaFromSource(List<ObjectDetails> objectDetails)
      throws ConnectorException {
    List<ObjectSchema> objectSchemas = new ArrayList<>();
    if (schema == null || schema.tables() == null) {
      return objectSchemas;
    }

    for (ObjectDetails details : objectDetails) {
      Table table = schema.tables().get(details.tableName());
      if (table != null) {
        ObjectDetails.Builder objDetail =
            ObjectDetails.builder()
                .catalog("Faker")
                .schema(schema.name())
                .table(table.name())
                .type("TABLE")
                .sourceObjectStatus(SourceObjectStatus.ACTIVE);

        Set<Field> fields = new HashSet<>();

        List<Column> columns = table.columns();
        int position = 0;
        for (Column column : columns) {
          HField field = null;
          if (column.type.equals("INT")) {
            field =
                new HIntegerField.Builder(column.name, column.type, position, FieldState.ACTIVE)
                    .build();
          } else if (column.type.equals("STRING")) {
            field =
                new HVarcharField.Builder(column.name, column.type, position, FieldState.ACTIVE)
                    .build();
          } else if (column.type.equals("FLOAT")) {
            field =
                new HDoubleField.Builder(column.name, column.type, position, FieldState.ACTIVE)
                    .build();
          } else {
            field = new HUnsupportedField.Builder(column.name, column.type, position).build();
          }

          fields.add(field);
          position++;
        }
        objectSchemas.add(new ObjectSchema(objDetail.build(), fields));
      }
    }
    return objectSchemas;
  }

  @Override
  public ExecutionResult fetchDataFromSource(ConnectorContext context, ConnectorProcessor processor)
      throws ConnectorException {
    ObjectSchema schema = context.schema();
    ObjectDetails details = schema.objectDetail();
    List<Field> selectedFields =
        context.schema().fields().stream()
            .sorted(Comparator.comparingInt(f -> f.properties().position()))
            .toList();

    List<HStruct> rows = generateRows(schema, selectedFields);
    for (HStruct row : rows) {
      processor.publish(
          row,
          ConnectorMeta.builder()
              .opType(OpType.READ)
              .sourceModifiedAt(OptionalLong.of(System.currentTimeMillis()))
              .build());
    }
    return new ExecutionResult(Map.of(details, new ExecutionResultStats(100L)), null, false);
  }

  private List<HStruct> generateRows(ObjectSchema schema, List<Field> selectedFields) {
    List<HStruct> rows = new ArrayList<>();
    Faker faker = new Faker();
    for (int i = 0; i < 100; i++) {
      List<HDatum> fieldValues = new ArrayList<>(selectedFields.size());
      for (Field field : selectedFields) {
        switch (field.logicalType()) {
          case "INT" -> fieldValues.add(new HInteger(faker.number().numberBetween(1, 1000)));
          case "STRING" -> fieldValues.add(new HVarchar(faker.beer().hop()));
          case "FLOAT" -> fieldValues.add(new HDouble(faker.number().randomDouble(3, 1, 1000)));
          default -> fieldValues.add(new HUnsupported());
        }
      }
      rows.add(new HStruct(fieldValues));
    }
    return rows;
  }

  @Override
  public void close() throws ConnectorException {}
}
