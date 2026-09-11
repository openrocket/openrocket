package info.openrocket.core.file.rocksim.export;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Jackson deserializer for individual {@link BasePartDTO} elements in a list.
 *
 * <p>When deserializing a {@code List<BasePartDTO>}, this deserializer maps each XML element
 * name (e.g., {@code <BodyTube>}, {@code <NoseCone>}) to the corresponding concrete DTO class.</p>
 *
 * <p>Use with {@code @JsonDeserialize(contentUsing=PartListDeserializer.class)} on
 * {@code List<BasePartDTO>} fields.</p>
 */
public class PartListDeserializer extends StdDeserializer<BasePartDTO> {

    static final Map<String, Class<? extends BasePartDTO>> ELEMENT_TYPE_MAP = new HashMap<>();

    static {
        ELEMENT_TYPE_MAP.put("BodyTube", BodyTubeDTO.class);
        ELEMENT_TYPE_MAP.put("InnerBodyTube", InnerBodyTubeDTO.class);
        ELEMENT_TYPE_MAP.put("NoseCone", NoseConeDTO.class);
        ELEMENT_TYPE_MAP.put("Transition", TransitionDTO.class);
        ELEMENT_TYPE_MAP.put("Ring", CenteringRingDTO.class);
        ELEMENT_TYPE_MAP.put("FinSet", FinSetDTO.class);
        ELEMENT_TYPE_MAP.put("CustomFinSet", CustomFinSetDTO.class);
        ELEMENT_TYPE_MAP.put("TubeFinSet", TubeFinSetDTO.class);
        ELEMENT_TYPE_MAP.put("LaunchLug", LaunchLugDTO.class);
        ELEMENT_TYPE_MAP.put("Streamer", StreamerDTO.class);
        ELEMENT_TYPE_MAP.put("Parachute", ParachuteDTO.class);
        ELEMENT_TYPE_MAP.put("MassObject", MassObjectDTO.class);
        ELEMENT_TYPE_MAP.put("ExternalPod", PodSetDTO.class);
    }

    public PartListDeserializer() {
        super(BasePartDTO.class);
    }

    @Override
    public BasePartDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // In Jackson XML, when called as contentUsing from a list, the parser may be
        // at FIELD_NAME (element name) or START_OBJECT depending on context.
        // currentName() at START_OBJECT returns the element's own name in XML.
        String elementName = p.currentName();
        return deserializeByName(p, ctxt, elementName);
    }

    /**
     * Deserializes a {@link BasePartDTO} from the current parser position, using the given
     * element name to determine the concrete subtype.
     */
    static BasePartDTO deserializeByName(JsonParser p, DeserializationContext ctxt,
                                          String elementName) throws IOException {
        if (elementName == null) {
            // Try to get element name from the current parsing context
            if (p.getCurrentName() != null) {
                elementName = p.getCurrentName();
            }
        }
        Class<? extends BasePartDTO> targetClass = elementName != null ? ELEMENT_TYPE_MAP.get(elementName) : null;
        if (targetClass == null) {
            p.skipChildren();
            return null;
        }
        return ctxt.readValue(p, targetClass);
    }
}
