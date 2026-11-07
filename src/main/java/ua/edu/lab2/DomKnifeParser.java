package ua.edu.lab2;

import java.util.*;
import org.w3c.dom.*;

public class DomKnifeParser implements KnifeParser {
    @Override
    public List<Knife> parse(byte[] xml) throws Exception {
        XmlSupport.validate(xml);
        NodeList items = XmlSupport.document(xml).getElementsByTagName("Item");
        List<Knife> result = new ArrayList<>();
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            var builder = new KnifeBuilder(item.getAttribute("id"));
            NodeList children = item.getElementsByTagName("*");
            for (int j = 0; j < children.getLength(); j++) {
                Element child = (Element) children.item(j);
                builder.set(child.getTagName(), child.getTextContent());
            }
            result.add(builder.build());
        }
        return result;
    }
}
