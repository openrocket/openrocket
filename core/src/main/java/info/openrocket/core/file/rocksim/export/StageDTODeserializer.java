package info.openrocket.core.file.rocksim.export;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Set;

/**
 * Custom Jackson deserializer for {@link StageDTO} that maps polymorphic child elements
 * (e.g., {@code <BodyTube>}, {@code <NoseCone>}) into the {@link StageDTO#getExternalPart() externalPart} list.
 *
 * <p>The RockSim XML format places stage parts directly inside the stage element without a wrapper,
 * requiring this custom handling since Jackson cannot otherwise know which child element names
 * belong to the {@code externalPart} list.</p>
 */
public class StageDTODeserializer extends StdDeserializer<StageDTO> {

    /** All XML element names that represent stage parts (top-level rocket components). */
    private static final Set<String> PART_ELEMENT_NAMES = Set.of(
            "BodyTube", "InnerBodyTube", "NoseCone", "Transition",
            "Ring", "FinSet", "CustomFinSet", "TubeFinSet", "LaunchLug",
            "Streamer", "Parachute", "MassObject", "ExternalPod"
    );

    public StageDTODeserializer() {
        super(StageDTO.class);
    }

    @Override
    public StageDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        StageDTO stage = new StageDTO();

        // Expect START_OBJECT for the stage element
        if (p.currentToken() == JsonToken.START_OBJECT) {
            p.nextToken();
        }

        while (p.currentToken() != JsonToken.END_OBJECT && p.currentToken() != null) {
            String fieldName = p.currentName();
            p.nextToken(); // move to value

            if (fieldName != null && PART_ELEMENT_NAMES.contains(fieldName)) {
                BasePartDTO part = PartListDeserializer.deserializeByName(p, ctxt, fieldName);
                if (part != null) {
                    stage.addExternalPart(part);
                }
            } else {
                // Skip unknown fields
                p.skipChildren();
            }

            p.nextToken(); // move to next field name or END_OBJECT
        }

        return stage;
    }
}
