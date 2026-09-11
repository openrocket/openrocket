package info.openrocket.core.file.rocksim.export;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;

import javax.xml.namespace.QName;
import java.io.IOException;

/**
 * Custom Jackson serializer that writes each {@link EngineSetDTO} as an
 * {@code EngineSet} element.
 *
 * <p>Use with {@code @JsonSerialize(contentUsing = EngineSetSerializer.class)} on
 * {@code List<EngineSetDTO>} fields, together with {@code @JacksonXmlElementWrapper}
 * for the {@code Stage#Engines} container.  The item name is forced here rather than
 * through {@code @JacksonXmlProperty} so the three per-stage lists keep distinct
 * property names instead of colliding on a shared {@code EngineSet} name.</p>
 */
public class EngineSetSerializer extends StdSerializer<EngineSetDTO> {

    public EngineSetSerializer() {
        super(EngineSetDTO.class);
    }

    @Override
    public void serialize(EngineSetDTO value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (gen instanceof ToXmlGenerator xmlGen) {
            xmlGen.setNextName(new QName(RockSimCommonConstants.ENGINE_SET));
        }
        provider.defaultSerializeValue(value, gen);
    }
}
