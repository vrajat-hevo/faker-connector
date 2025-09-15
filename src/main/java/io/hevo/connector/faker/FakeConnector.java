package io.hevo.connector.faker;

import io.hevo.connector.GenericConnector;
import io.hevo.connector.exceptions.ConnectorException;
import io.hevo.connector.model.*;
import io.hevo.connector.offset.Offset;
import io.hevo.connector.processor.ConnectorProcessor;

import java.util.List;

public class FakeConnector implements GenericConnector {
    @Override
    public void initializeConnection() {
        // No-op
    }

    @Override
    public java.util.List<ObjectDetails> getObjects() {
        return java.util.Collections.emptyList();
    }

    @Override
    public List<ObjectStats> fetchSchemaStatsFromSource() throws ConnectorException {
        return List.of();
    }

    @Override
    public List<ObjectSchema> fetchSchemaFromSource(List<ObjectDetails> objectDetails) throws ConnectorException {
        return List.of();
    }

    @Override
    public ExecutionResult fetchDataFromSource(ConnectorContext context, ConnectorProcessor processor) throws ConnectorException {
        return null;
    }

    @Override
    public void close() throws ConnectorException {

    }
}
