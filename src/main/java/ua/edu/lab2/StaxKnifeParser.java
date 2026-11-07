package ua.edu.lab2;

import java.io.ByteArrayInputStream;
import java.util.*;
import javax.xml.stream.*;

public class StaxKnifeParser implements KnifeParser {
    @Override
    public List<Knife> parse(byte[] xml) throws Exception {
        XmlSupport.validate(xml);
        var factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        var reader = factory.createXMLStreamReader(new ByteArrayInputStream(xml));
        List<Knife> result = new ArrayList<>();
        KnifeBuilder builder = null;
        StringBuilder text = new StringBuilder();
        try {
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    text.setLength(0);
                    if (reader.getLocalName().equals("Item"))
                        builder = new KnifeBuilder(reader.getAttributeValue(null, "id"));
                } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                    text.append(reader.getText());
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String name = reader.getLocalName();
                    if (builder != null) builder.set(name, text.toString());
                    if (name.equals("Item")) {
                        result.add(builder.build());
                        builder = null;
                    }
                    text.setLength(0);
                }
            }
        } finally { reader.close(); }
        return result;
    }
}
