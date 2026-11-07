package ua.edu.lab2;

import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) throws Exception {
        try (var config = XmlSupport.resource("logging.properties")) {
            LogManager.getLogManager().readConfiguration(config);
        }
        try {
            run(args);
        } catch (Exception error) {
            LOG.log(Level.SEVERE, "Помилка обробки XML: " + error.getMessage(), error);
            throw error;
        }
    }
    public static void run(String[] args) throws Exception {
        byte[] xml;
        if (args.length > 0) xml = Files.readAllBytes(Path.of(args[0]));
        else try (var input = XmlSupport.resource("knives.xml")) { xml = input.readAllBytes(); }
        List<Knife> baseline = null;
        for (KnifeParser parser : List.of(new DomKnifeParser(), new SaxKnifeParser(), new StaxKnifeParser())) {
            List<Knife> knives = parser.parse(xml);
            if (baseline != null && !baseline.equals(knives))
                throw new IllegalStateException("Результати парсерів відрізняються");
            baseline = new ArrayList<>(knives);
            knives.sort(new KnifeComparator());
            LOG.info(parser.getClass().getSimpleName() + ": XML валідний; об'єктів: " + knives.size());
            for (Knife knife : knives) LOG.info(knife.toString());
        }
        Path output = Path.of(args.length > 1 ? args[1] : "output/grouped.xml");
        if (output.toAbsolutePath().getParent() != null)
            Files.createDirectories(output.toAbsolutePath().getParent());
        try (var stream = Files.newOutputStream(output)) { XmlSupport.transform(xml, stream); }
        LOG.info("Результати трьох парсерів збігаються. XSL: " + output);
    }
}
