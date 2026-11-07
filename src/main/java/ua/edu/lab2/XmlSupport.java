package ua.edu.lab2;

import java.io.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;

public final class XmlSupport {
    private XmlSupport() { }
    public static InputStream resource(String name) throws IOException {
        InputStream in = XmlSupport.class.getResourceAsStream("/" + name);
        if (in == null) throw new FileNotFoundException(name);
        return in;
    }
    public static void validate(byte[] xml) throws Exception {
        var factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try (var xsd = resource("knives.xsd")) {
            factory.newSchema(new StreamSource(xsd)).newValidator()
                    .validate(new StreamSource(new ByteArrayInputStream(xml)));
        }
    }
    public static Document document(byte[] xml) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    }
    public static void transform(byte[] xml, OutputStream output) throws Exception {
        validate(xml);
        var factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        try (var xsl = resource("group-by-type.xsl")) {
            factory.newTransformer(new StreamSource(xsl)).transform(
                    new StreamSource(new ByteArrayInputStream(xml)), new StreamResult(output));
        }
    }
}
