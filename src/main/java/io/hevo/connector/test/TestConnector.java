package io.hevo.connector.test;

import io.hevo.connector.GenericConnector;
import io.hevo.connector.model.*;
import io.hevo.connector.offset.Offset;
import io.hevo.connector.processor.ConnectorProcessor;
import io.hevo.connector.ui.Auth;
import io.hevo.connector.ui.FieldValidatorConfig;
import io.hevo.connector.ui.Group;
import io.hevo.connector.ui.OptionsRef;
import io.hevo.connector.ui.OptionsRefType;
import io.hevo.connector.ui.Property;
import io.hevo.connector.ui.PropertyType;
import java.util.*;

public class TestConnector implements GenericConnector {

  @Property(
      name = "account_type",
      displayName = "Account Type",
      defaultValue = "PRODUCTION",
      fieldOrder = 2,
      optionsRef =
          @OptionsRef(
              type = OptionsRefType.STATIC,
              allowedValues = {"PRODUCTION", "SANDBOX"}))
  private AccountType accountType;

  @Auth(name = "auth_type", displayName = "Auth Type", type = AuthType.OAUTH, fieldOrder = 1)
  private AuthCredentials authCredentials;

  @Property(
      name = "historical_time_frame",
      displayName = "Historical Sync Time Frame in Months",
      defaultValue = "3",
      type = PropertyType.LONG,
      group = Group.Type.ADDITIONAL_SETTINGS,
      fieldValidatorConfigs = {
        @FieldValidatorConfig(
            type = FieldValidatorConfig.Type.MIN,
            value = "1",
            errorMessage = "Minimum historical duration allowed is 1 month"),
        @FieldValidatorConfig(
            type = FieldValidatorConfig.Type.MAX,
            value = "60",
            errorMessage = "Maximum historical duration allowed is 60 months")
      },
      fieldOrder = 3,
      editable = true)
  private long historicalTimeFrame;

  @Override
  public void initializeConnection() {
    // Implement sample test connection
  }

  @Override
  public List<ObjectDetails> getObjects() {
    return new LinkedList<>();
  }

  @Override
  public List<ObjectStats> fetchSchemaStatsFromSource() {
    return new LinkedList<>();
  }

  @Override
  public List<ObjectSchema> fetchSchemaFromSource(List<ObjectDetails> objectDetails) {
    return Collections.emptyList();
  }

  @Override
  public ExecutionResult fetchDataFromSource(
      ConnectorContext context, ConnectorProcessor processor) {
    return new ExecutionResult(
        Map.of(context.schema().objectDetail(), new ExecutionResultStats(0L)),
        Offset.empty(),
        false);
  }

  @Override
  public void close() {
    // Do nothing
  }
}
