package ua.edu.lab2;

import java.io.ByteArrayInputStream;
import java.util.*;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SaxKnifeParser implements KnifeParser {
    @Override
    public List<Knife> parse(byte[] xml) throws Exception {
        XmlSupport.validate(xml);
        var factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        var handler = new Handler();
        factory.newSAXParser().parse(new ByteArrayInputStream(xml), handler);
        return handler.result;
    }
    private static class Handler extends DefaultHandler {
        private final List<Knife> result = new ArrayList<>();
        private final StringBuilder text = new StringBuilder();
        private KnifeBuilder builder;
        @Override
        public void startElement(String uri, String local, String name, Attributes attrs) {
            text.setLength(0);
            if (name.equals("Item")) builder = new KnifeBuilder(attrs.getValue("id"));
        }
        @Override
        public void characters(char[] chars, int start, int length) {
            // SAX може передати текст частинами, тому накопичуємо його.
            text.append(chars, start, length);
        }
        @Override
        public void endElement(String uri, String local, String name) {
            if (builder != null) builder.set(name, text.toString());
            if (name.equals("Item")) {
                result.add(builder.build());
                builder = null;
            }
            text.setLength(0);
        }
    }
}
