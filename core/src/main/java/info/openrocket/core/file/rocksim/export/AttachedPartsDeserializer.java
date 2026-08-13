package info.openrocket.core.file.rocksim.export;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Jackson deserializer for {@code List<BasePartDTO>} fields wrapped in
 * an XML container element (e.g., {@code <AttachedParts>}).
 *
 * <p>Use with {@code @JsonDeserialize(using=AttachedPartsDeserializer.class)} on
 * {@code List<BasePartDTO>} fields that have an {@code @JacksonXmlElementWrapper}.</p>
 */
public class AttachedPartsDeserializer extends StdDeserializer<List<BasePartDTO>> {

    @SuppressWarnings("unchecked")
    public AttachedPartsDeserializer() {
        super((Class<List<BasePartDTO>>) (Class<?>) List.class);
    }

    @Override
    public List<BasePartDTO> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<BasePartDTO> result = new ArrayList<>();

        // The parser is positioned at the first token inside the wrapper element.
        // Iterate over all child elements, treating each FIELD_NAME as an element name.
        JsonToken token = p.currentToken();

        // If we're at START_OBJECT (wrapper start), advance into its content
        if (token == JsonToken.START_OBJECT) {
            token = p.nextToken();
        }

        while (token == JsonToken.FIELD_NAME) {
            String elementName = p.currentName();
            p.nextToken(); // move to START_OBJECT of the element value

            BasePartDTO part = PartListDeserializer.deserializeByName(p, ctxt, elementName);
            if (part != null) {
                result.add(part);
            }

            token = p.nextToken(); // move to next FIELD_NAME or END_OBJECT
        }

        return result;
    }
}
