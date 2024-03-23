package info.openrocket.core.preset.xml;

import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPresetFactory;
import info.openrocket.core.preset.TypedPropertyMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.StringReader;
import java.io.StringWriter;

/**
 */
public class BaseComponentDTOTest {

    @Test
    public void testImage() throws Exception {
        TypedPropertyMap presetspec = new TypedPropertyMap();
        presetspec.put(ComponentPreset.TYPE, ComponentPreset.Type.BODY_TUBE);
        presetspec.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer("manufacturer"));
        presetspec.put(ComponentPreset.PARTNO, "partno");
        presetspec.put(ComponentPreset.LENGTH, 2.0);
        presetspec.put(ComponentPreset.OUTER_DIAMETER, 2.0);
        presetspec.put(ComponentPreset.INNER_DIAMETER, 1.0);
        presetspec.put(ComponentPreset.MASS, 100.0);
        ComponentPreset preset = ComponentPresetFactory.create(presetspec);

        // Convert the presets to a BodyTubeDTO
        BodyTubeDTO dto = new BodyTubeDTO(preset);

        // Add an image to the DTO.
        BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("/test_image.png"));
        dto.setImage(image);

        JAXBContext binder = JAXBContext.newInstance(OpenRocketComponentDTO.class);
        Marshaller marshaller = binder.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();

        // Serialize the dto to XML
        marshaller.marshal(dto, sw);
        String xml = sw.toString();

        // Read the XML back to create the dto again
        Unmarshaller unmarshaller = binder.createUnmarshaller();
        BodyTubeDTO redone = (BodyTubeDTO) unmarshaller.unmarshal(new StringReader(xml));

        // Compare the image.
        Assertions.assertArrayEquals(((DataBufferByte) image.getData().getDataBuffer()).getData(),
                ((DataBufferByte) redone.getImage().getData().getDataBuffer()).getData());

        // Assert the rest of the attributes.
        Assertions.assertEquals(dto.getInsideDiameter(), redone.getInsideDiameter(), 0.00001);
        Assertions.assertEquals(dto.getLength(), redone.getLength(), 0.00001);
        Assertions.assertEquals(dto.getOutsideDiameter(), redone.getOutsideDiameter(), 0.00001);
        Assertions.assertEquals(dto.getDescription(), redone.getDescription());
        Assertions.assertEquals(dto.getManufacturer(), redone.getManufacturer());
        Assertions.assertEquals(dto.getMass(), redone.getMass(), 0.00001);
        Assertions.assertEquals(dto.getPartNo(), redone.getPartNo());

        // Uncomment if you want to write the image to a file to view it.
        // ImageIO.write(redone.getImage(), "png", new FileOutputStream("redone.png"));
    }
}
