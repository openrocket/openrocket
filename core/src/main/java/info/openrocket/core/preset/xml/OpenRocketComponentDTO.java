package info.openrocket.core.preset.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import info.openrocket.core.material.Material;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.InvalidComponentPresetException;

/**
 * The real 'root' element in an XML document.
 */
@JacksonXmlRootElement(localName = "OpenRocketComponent")
public class OpenRocketComponentDTO {

    private static final Logger log = LoggerFactory.getLogger(OpenRocketComponentDTO.class);

    @JacksonXmlProperty(localName = "Version")
    private final String version = "0.1";

    @JacksonXmlProperty(localName = "Legacy")
    private String legacy;

    @JacksonXmlElementWrapper(localName = "Materials")
    @JacksonXmlProperty(localName = "Material")
    List<MaterialDTO> materials = new ArrayList<>();

    @JacksonXmlElementWrapper(localName = "Components")
    @JsonDeserialize(using = ComponentListDeserializer.class)
    private List<BaseComponentDTO> components = new ArrayList<>();

    public OpenRocketComponentDTO() {
    }

    public OpenRocketComponentDTO(boolean isLegacy, final List<MaterialDTO> theMaterials,
            final List<BaseComponentDTO> theComponents) {
        setLegacy(isLegacy);
        materials = theMaterials;
        components = theComponents;
    }

    public Boolean getLegacy() {
        if (null == legacy) {
            return false;
        }
        return true;
    }

    public void setLegacy(Boolean isLegacy) {
        if (Boolean.TRUE.equals(isLegacy)) {
            legacy = "";
        } else {
            legacy = null;
        }
    }

    public List<MaterialDTO> getMaterials() {
        return materials;
    }

    public void addMaterial(final MaterialDTO theMaterial) {
        materials.add(theMaterial);
    }

    public void setMaterials(final List<MaterialDTO> theMaterials) {
        materials = theMaterials;
    }

    public List<BaseComponentDTO> getComponents() {
        return components;
    }

    public void addComponent(final BaseComponentDTO theComponent) {
        components.add(theComponent);
    }

    public void setComponents(final List<BaseComponentDTO> theComponents) {
        components = theComponents;
    }

    public List<ComponentPreset> asComponentPresets() throws InvalidComponentPresetException {
        List<ComponentPreset> result = new ArrayList<>(components.size());
		for (BaseComponentDTO component : components) {
			result.add(component.asComponentPreset(getLegacy(), materials));
		}
        return result;
    }

    public List<Material> asMaterialList() {
        List<Material> result = new ArrayList<>(materials.size());
        for (MaterialDTO material : materials) {
            result.add(material.asMaterial());
        }
        return result;
    }

    /**
     * Custom deserializer for {@code List<BaseComponentDTO>} that reads each child
     * element of the {@code <Components>} wrapper by its XML element name
     * (e.g. {@code <BodyTube>}, {@code <NoseCone>}) and dispatches to the
     * appropriate concrete subtype.
     * <p>
     * The ORC file format uses the XML element local-name as the type discriminator,
     * directly inside {@code <Components>}.  Jackson's standard
     * {@code @JacksonXmlProperty(localName = "Component")} approach can only collect
     * elements of a single fixed name, so we do the dispatch ourselves.
     * <p>
     * Because {@link BaseComponentDTO} carries
     * {@code @JsonTypeInfo(include = WRAPPER_OBJECT)}, each resolved token must still
     * be presented to Jackson as a {@code {"TypeName": {content}}} object so that the
     * built-in type resolver resolves the concrete class.  We synthesize that wrapper
     * using a {@link TokenBuffer}.
     */
    static class ComponentListDeserializer extends StdDeserializer<List<BaseComponentDTO>> {

        ComponentListDeserializer() {
            super(List.class);
        }

        @Override
        public List<BaseComponentDTO> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            List<BaseComponentDTO> result = new ArrayList<>();

            JsonToken current = p.currentToken();

            // Advance past START_OBJECT (the virtual object wrapping <Components> content)
            if (current == JsonToken.START_OBJECT) {
                current = p.nextToken();
            }

            // Each FIELD_NAME is the XML element name (= type wrapper key for WRAPPER_OBJECT).
            // jackson-dataformat-xml may coerce multiple same-named sibling elements into a
            // START_ARRAY; a single element arrives as START_OBJECT.
            while (current == JsonToken.FIELD_NAME) {
                String typeName = p.currentName();
                JsonToken valueToken = p.nextToken(); // advance to START_OBJECT or START_ARRAY

                if (valueToken == JsonToken.START_OBJECT) {
                    BaseComponentDTO item = readWrapped(p, typeName, ctxt);
                    if (item != null) {
                        result.add(item);
                    }
                } else if (valueToken == JsonToken.START_ARRAY) {
                    // Multiple elements share the same element name
                    while (p.nextToken() != JsonToken.END_ARRAY) {
                        if (p.currentToken() == JsonToken.START_OBJECT) {
                            BaseComponentDTO item = readWrapped(p, typeName, ctxt);
                            if (item != null) {
                                result.add(item);
                            }
                        } else {
                            p.skipChildren();
                        }
                    }
                } else {
                    p.skipChildren();
                }

                current = p.nextToken(); // advance to next FIELD_NAME or END_OBJECT
            }

            return result;
        }

        /**
         * Reads one component element whose content starts at the current START_OBJECT
         * token.  Wraps it in a synthetic {@code {"typeName": {content}}} token buffer
         * so that Jackson's WRAPPER_OBJECT type resolver on {@link BaseComponentDTO}
         * correctly identifies the concrete subtype and deserializes the fields.
         */
        private BaseComponentDTO readWrapped(JsonParser p, String typeName,
                DeserializationContext ctxt) throws IOException {
            // p.currentToken() == START_OBJECT (the element content)
            TokenBuffer buf = new TokenBuffer(p, ctxt);
            buf.writeStartObject();
            buf.writeFieldName(typeName);
            buf.copyCurrentStructure(p); // copies START_OBJECT ... END_OBJECT; advances p
            buf.writeEndObject();

            try (JsonParser bufParser = buf.asParser(p)) {
                bufParser.nextToken(); // advance to START_OBJECT in buffer
                return ctxt.readValue(bufParser, BaseComponentDTO.class);
            } catch (IOException e) {
                log.warn("Failed to deserialize component element <{}>: {}", typeName, e.getMessage());
                return null;
            }
        }
    }
}
