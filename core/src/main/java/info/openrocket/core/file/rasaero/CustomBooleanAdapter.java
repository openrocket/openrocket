package info.openrocket.core.file.rasaero;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class CustomBooleanAdapter {

    public static class Serializer extends StdSerializer<Boolean> {
        public Serializer() {
            super(Boolean.class);
        }

        @Override
        public void serialize(Boolean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            gen.writeString(value ? "True" : "False");
        }
    }

    public static class Deserializer extends StdDeserializer<Boolean> {
        public Deserializer() {
            super(Boolean.class);
        }

        @Override
        public Boolean deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            return "true".equalsIgnoreCase(p.getText());
        }
    }
}
