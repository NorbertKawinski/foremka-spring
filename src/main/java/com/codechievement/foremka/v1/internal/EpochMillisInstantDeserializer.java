package com.codechievement.foremka.v1.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.Instant;

/**
 * Deserializes {@link Instant} from an epoch-millisecond integer written by
 * {@link EpochMillisInstantSerializer}. Also accepts ISO-8601 strings so that
 * persisted files written with the previous format remain readable.
 */
public class EpochMillisInstantDeserializer extends StdDeserializer<Instant> {

    public EpochMillisInstantDeserializer() {
        super(Instant.class);
    }

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
            return Instant.ofEpochMilli(p.getLongValue());
        }
        // Backwards-compat: files written before this change used ISO-8601 strings.
        return Instant.parse(p.getText());
    }
}
