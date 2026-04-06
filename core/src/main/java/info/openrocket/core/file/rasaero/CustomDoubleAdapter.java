package info.openrocket.core.file.rasaero;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CustomDoubleAdapter {

    public static class Serializer extends StdSerializer<Double> {
        public Serializer() {
            super(Double.class);
        }

        @Override
        public void serialize(Double value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            DecimalFormat df = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.US));
            gen.writeString(df.format(value));
        }
    }

    public static class Deserializer extends StdDeserializer<Double> {
        public Deserializer() {
            super(Double.class);
        }

        @Override
        public Double deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            return Double.parseDouble(p.getText());
        }
    }
}
