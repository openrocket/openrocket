package info.openrocket.core.file.rocksim.export;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Jackson deserializer for {@code List<EngineSetDTO>} fields wrapped in a
 * {@code Stage#Engines} container element.
 *
 * <p>Mirrors {@link AttachedPartsDeserializer}: the {@code EngineSet} items are named
 * by {@link EngineSetSerializer} rather than by the field, so reading them back needs
 * explicit handling.  Iterating the tokens also tolerates an empty container element
 * (e.g. {@code <Stage1Engines/>}) without desynchronizing the parser.</p>
 */
public class EngineSetListDeserializer extends StdDeserializer<List<EngineSetDTO>> {

    @SuppressWarnings("unchecked")
    public EngineSetListDeserializer() {
        super((Class<List<EngineSetDTO>>) (Class<?>) List.class);
    }

    @Override
    public List<EngineSetDTO> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<EngineSetDTO> result = new ArrayList<>();

        JsonToken token = p.currentToken();
        if (token == JsonToken.START_OBJECT) {
            token = p.nextToken();
        }

        while (token == JsonToken.FIELD_NAME) {
            String elementName = p.currentName();
            p.nextToken(); // move to the element value

            if (RockSimCommonConstants.ENGINE_SET.equals(elementName)) {
                EngineSetDTO engine = p.readValueAs(EngineSetDTO.class);
                if (engine != null) {
                    result.add(engine);
                }
            } else {
                p.skipChildren();
            }

            token = p.nextToken(); // move to next FIELD_NAME or END_OBJECT
        }

        return result;
    }
}
