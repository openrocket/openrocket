package info.openrocket.core.file.rasaero.export;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.io.IOException;

/**
 * Custom Jackson serializer for {@link BasePartDTO} elements that writes each element
 * using its own XML element name (from {@link JacksonXmlRootElement#localName()}).
 *
 * <p>This preserves the original document order of the components (NoseCone, BodyTube,
 * Transition, BoatTail, ...) instead of grouping them by type, which matters because the
 * RASAero importer constructs components sequentially and relies on their order to
 * reproduce the geometry. It mirrors the former JAXB {@code @XmlElementRefs} behaviour of
 * deriving the element name from each item's runtime type.</p>
 *
 * <p>Use with {@code @JsonSerialize(contentUsing=PartListSerializer.class)} on
 * {@code List<BasePartDTO>} fields, together with
 * {@code @JacksonXmlElementWrapper(useWrapping = false)}.</p>
 */
public class PartListSerializer extends StdSerializer<BasePartDTO> {

    public PartListSerializer() {
        super(BasePartDTO.class);
    }

    @Override
    public void serialize(BasePartDTO value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (gen instanceof ToXmlGenerator xmlGen) {
            String elementName = getElementName(value.getClass());
            xmlGen.setNextName(new QName(elementName));
        }
        provider.defaultSerializeValue(value, gen);
    }

    /**
     * Returns the XML element name for the given class, derived from
     * {@link JacksonXmlRootElement#localName()} if present, otherwise the simple class name.
     */
    static String getElementName(Class<?> cls) {
        JacksonXmlRootElement rootElement = cls.getAnnotation(JacksonXmlRootElement.class);
        if (rootElement != null && !rootElement.localName().isEmpty()) {
            return rootElement.localName();
        }
        return cls.getSimpleName();
    }
}
