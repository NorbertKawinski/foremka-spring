package com.codechievement.foremka.v1.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.Instant;

/**
 * Serializes {@link Instant} as an epoch-millisecond integer.
 *
 * <p>Saves ~19 chars per field compared with the default ISO-8601 string representation
 * (e.g. {@code 1745935282995} vs {@code "2026-04-29T14:28:02.995Z"}). Pair with
 * {@link EpochMillisInstantDeserializer} for round-trip correctness.
 */
public class EpochMillisInstantSerializer extends StdSerializer<Instant> {

    public EpochMillisInstantSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumber(value.toEpochMilli());
    }
}
