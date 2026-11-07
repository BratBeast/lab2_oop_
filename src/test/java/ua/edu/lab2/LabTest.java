package ua.edu.lab2;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class LabTest {
    @TempDir Path temp;
    static byte[] sample() throws Exception {
        try (var in = XmlSupport.resource("knives.xml")) { return in.readAllBytes(); }
    }
    static byte[] replace(String from, String to) throws Exception {
        return new String(sample(), StandardCharsets.UTF_8).replace(from, to)
                .getBytes(StandardCharsets.UTF_8);
    }
    static Stream<KnifeParser> parsers() {
        return Stream.of(new DomKnifeParser(), new SaxKnifeParser(), new StaxKnifeParser());
    }
    @ParameterizedTest @MethodSource("parsers")
    void readsAllFields(KnifeParser parser) throws Exception {
        var list = parser.parse(sample());
        assertEquals(6, list.size());
        assertEquals(new Knife("k1", "ніж", "одноручна", "Україна",
                new Knife.Visual(15, 25, "сталь", "дерево", "дуб", false), true), list.get(0));
        assertEquals("метал", list.get(1).visual().handle());
        assertEquals("", list.get(1).visual().woodType());
        assertEquals("пластик", list.get(3).visual().handle());
        assertFalse(list.get(3).value());
        assertTrue(list.get(1).visual().fuller());
        assertEquals("дворучна", list.get(4).handy());
        assertEquals("чавун", list.get(5).visual().material());
    }
    @Test void parsersAgree() throws Exception {
        var expected = new DomKnifeParser().parse(sample());
        assertEquals(expected, new SaxKnifeParser().parse(sample()));
        assertEquals(expected, new StaxKnifeParser().parse(sample()));
    }
    @ParameterizedTest @MethodSource("parsers")
    void supportsCdataAndBooleanNumbers(KnifeParser parser) throws Exception {
        byte[] xml = new String(sample(), StandardCharsets.UTF_8)
                .replace("Україна", "Ук<![CDATA[ра]]>їна")
                .replace(">true<", ">1<").replace(">false<", ">0<")
                .getBytes(StandardCharsets.UTF_8);
        assertEquals(parser.parse(sample()), parser.parse(xml));
    }
    static Stream<Arguments> invalidXml() {
        return Stream.of(
            Arguments.of("<LengthCm>15</LengthCm>", "<LengthCm>9.99</LengthCm>"),
            Arguments.of("<WidthMm>25</WidthMm>", "<WidthMm>9</WidthMm>"),
            Arguments.of("id=\"k2\"", "id=\"k1\""),
            Arguments.of("id=\"k1\"", "id=\"7bad\""),
            Arguments.of(" id=\"k1\"", ""),
            Arguments.of("<Wood>дуб</Wood>", "<Wood/>"),
            Arguments.of("<Wood>дуб</Wood>", "<Wood>   </Wood>"),
            Arguments.of("<Wood>дуб</Wood>", "<Wood>дуб</Wood><Metal/>"),
            Arguments.of("<Wood>дуб</Wood>", ""),
            Arguments.of("<Type>ніж</Type>", "<Type>невідомо</Type>"),
            Arguments.of("<Handy>одноручна</Handy>", "<Handy>три</Handy>"),
            Arguments.of("<Material>сталь</Material>", "<Material>скло</Material>"),
            Arguments.of("<Origin>Україна</Origin>", "<Origin/>"),
            Arguments.of("<Value>true</Value>", "<Value>так</Value>"),
            Arguments.of("</Knife>", ""));
    }
    @ParameterizedTest @MethodSource("invalidXml")
    void rejectsInvalidDocuments(String from, String to) throws Exception {
        byte[] xml = replace(from, to);
        assertThrows(Exception.class, () -> XmlSupport.validate(xml));
        for (var parser : parsers().toList())
            assertThrows(Exception.class, () -> parser.parse(xml));
        assertThrows(Exception.class, () -> XmlSupport.transform(xml, new ByteArrayOutputStream()));
    }
    @Test void boundaryAndDecimalsAreAccepted() throws Exception {
        var knives = new DomKnifeParser().parse(replace("<LengthCm>15</LengthCm>", "<LengthCm>10.25</LengthCm>"));
        assertEquals(10.25, knives.get(0).visual().lengthCm());
        assertEquals(10, knives.get(3).visual().lengthCm());
        assertEquals(10, knives.get(3).visual().widthMm());
    }
    @Test void sortsByLengthAndId() throws Exception {
        var list = new DomKnifeParser().parse(sample());
        list.sort(new KnifeComparator());
        assertEquals(List.of("k4", "k1", "k6", "k3", "k2", "k5"), list.stream().map(Knife::id).toList());
        var first = list.get(0);
        var sameLength = new Knife("z1", first.type(), first.handy(), first.origin(), first.visual(), first.value());
        var comparator = new KnifeComparator();
        assertTrue(comparator.compare(first, sameLength) < 0);
        assertTrue(comparator.compare(sameLength, first) > 0);
        assertEquals(0, comparator.compare(first, first));
    }
    @Test void groupsWithoutLosingItems() throws Exception {
        var out = new ByteArrayOutputStream();
        XmlSupport.transform(sample(), out);
        var doc = XmlSupport.document(out.toByteArray());
        assertEquals("KnifeGroups", doc.getDocumentElement().getTagName());
        var groups = doc.getElementsByTagName("Group");
        assertEquals(3, groups.getLength());
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < groups.getLength(); i++) {
            var group = (org.w3c.dom.Element) groups.item(i);
            var items = group.getElementsByTagName("Item");
            assertEquals(2, items.getLength());
            for (int j = 0; j < items.getLength(); j++) {
                var item = (org.w3c.dom.Element) items.item(j);
                assertTrue(ids.add(item.getAttribute("id")));
                assertEquals(group.getAttribute("type"), item.getElementsByTagName("Type").item(0).getTextContent());
                assertEquals(1, item.getElementsByTagName("Visual").getLength());
            }
        }
        assertEquals(Set.of("k1", "k2", "k3", "k4", "k5", "k6"), ids);
    }
    @Test void handlesMissingResource() {
        assertThrows(FileNotFoundException.class, () -> XmlSupport.resource("missing.xml"));
    }
    @Test void rejectsExternalEntities() throws Exception {
        byte[] xml = "<!DOCTYPE Knife [<!ENTITY e SYSTEM 'file:///missing'>]><Knife>&e;</Knife>".getBytes(StandardCharsets.UTF_8);
        assertThrows(Exception.class, () -> XmlSupport.document(xml));
        for (var parser : parsers().toList()) assertThrows(Exception.class, () -> parser.parse(xml));
    }
    @Test void applicationWritesOutput() throws Exception {
        Path input = temp.resolve("knives.xml"), output = temp.resolve("nested/grouped.xml");
        Files.write(input, sample());
        Main.main(new String[]{input.toString(), output.toString()});
        assertEquals(6, XmlSupport.document(Files.readAllBytes(output)).getElementsByTagName("Item").getLength());
    }
    @Test void applicationRejectsMissingInput() {
        assertThrows(IOException.class, () -> Main.main(new String[]{temp.resolve("missing.xml").toString()}));
    }
    @Test void defaultApplicationRuns() throws Exception {
        Main.run(new String[0]);
        assertTrue(Files.size(Path.of("output/grouped.xml")) > 0);
    }
}
