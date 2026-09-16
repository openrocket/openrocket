package info.openrocket.core.file.threemf.export;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class ThreeMFPackageWriter {
    private static final String CONTENT_TYPES_NAMESPACE =
            "http://schemas.openxmlformats.org/package/2006/content-types";
    private static final String RELATIONSHIPS_NAMESPACE =
            "http://schemas.openxmlformats.org/package/2006/relationships";
    private static final String MODEL_NAMESPACE =
            "http://schemas.microsoft.com/3dmanufacturing/core/2015/02";

    private ThreeMFPackageWriter() {
    }

    static void write(Path path, List<PrintablePart> parts) throws IOException {
        try (OutputStream output = Files.newOutputStream(path);
             ZipOutputStream zip = new ZipOutputStream(output)) {
            writeEntry(zip, "[Content_Types].xml", ThreeMFPackageWriter::writeContentTypes);
            writeEntry(zip, "_rels/.rels", ThreeMFPackageWriter::writeRelationships);
            writeEntry(zip, "3D/3dmodel.model", writer -> writeModel(writer, parts));
        } catch (XMLStreamException exception) {
            throw new IOException("Unable to create 3MF XML", exception);
        }
    }

    private static void writeEntry(ZipOutputStream zip, String name, XMLContentWriter contentWriter)
            throws IOException, XMLStreamException {
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(0);
        zip.putNextEntry(entry);
        XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(zip, "UTF-8");
        contentWriter.write(writer);
        writer.flush();
        zip.closeEntry();
    }

    private static void writeContentTypes(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("Types");
        writer.writeDefaultNamespace(CONTENT_TYPES_NAMESPACE);
        writeDefaultType(writer, "rels", "application/vnd.openxmlformats-package.relationships+xml");
        writeDefaultType(writer, "model", "application/vnd.ms-package.3dmanufacturing-3dmodel+xml");
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private static void writeDefaultType(XMLStreamWriter writer, String extension, String contentType)
            throws XMLStreamException {
        writer.writeEmptyElement("Default");
        writer.writeAttribute("Extension", extension);
        writer.writeAttribute("ContentType", contentType);
    }

    private static void writeRelationships(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("Relationships");
        writer.writeDefaultNamespace(RELATIONSHIPS_NAMESPACE);
        writer.writeEmptyElement("Relationship");
        writer.writeAttribute("Target", "/3D/3dmodel.model");
        writer.writeAttribute("Id", "rel0");
        writer.writeAttribute("Type", "http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel");
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private static void writeModel(XMLStreamWriter writer, List<PrintablePart> parts) throws XMLStreamException {
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("model");
        writer.writeDefaultNamespace(MODEL_NAMESPACE);
        writer.writeAttribute("unit", "millimeter");
        writer.writeAttribute(XMLConstants.XML_NS_URI, "lang", "en-US");

        writer.writeStartElement("resources");
        for (int index = 0; index < parts.size(); index++) {
            writeObject(writer, index + 1, parts.get(index));
        }
        writer.writeEndElement();

        writer.writeStartElement("build");
        for (int index = 0; index < parts.size(); index++) {
            PrintablePart part = parts.get(index);
            writer.writeEmptyElement("item");
            writer.writeAttribute("objectid", Integer.toString(index + 1));
            writer.writeAttribute("transform", "1 0 0 0 1 0 0 0 1 "
                    + number(part.getPlacementX()) + " " + number(part.getPlacementY()) + " 0");
        }
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private static void writeObject(XMLStreamWriter writer, int id, PrintablePart part) throws XMLStreamException {
        writer.writeStartElement("object");
        writer.writeAttribute("id", Integer.toString(id));
        writer.writeAttribute("name", part.getName());
        writer.writeAttribute("type", "model");
        writer.writeStartElement("mesh");
        writer.writeStartElement("vertices");
        for (double[] vertex : part.getVertices()) {
            writer.writeEmptyElement("vertex");
            writer.writeAttribute("x", number(vertex[0]));
            writer.writeAttribute("y", number(vertex[1]));
            writer.writeAttribute("z", number(vertex[2]));
        }
        writer.writeEndElement();
        writer.writeStartElement("triangles");
        for (int[] triangle : part.getTriangles()) {
            writer.writeEmptyElement("triangle");
            writer.writeAttribute("v1", Integer.toString(triangle[0]));
            writer.writeAttribute("v2", Integer.toString(triangle[1]));
            writer.writeAttribute("v3", Integer.toString(triangle[2]));
        }
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.6f", value);
    }

    @FunctionalInterface
    private interface XMLContentWriter {
        void write(XMLStreamWriter writer) throws XMLStreamException;
    }
}
