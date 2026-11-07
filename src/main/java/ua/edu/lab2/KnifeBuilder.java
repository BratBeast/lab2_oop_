package ua.edu.lab2;

import java.util.HashMap;
import java.util.Map;

/** Спільне перетворення тексту XML на об'єкт для всіх парсерів. */
final class KnifeBuilder {
    private final String id;
    private final Map<String, String> fields = new HashMap<>();
    KnifeBuilder(String id) { this.id = id; }
    void set(String name, String text) { fields.put(name, text.trim()); }
    private String get(String name) { return fields.get(name); }
    private boolean bool(String name) {
        return "true".equals(get(name)) || "1".equals(get(name));
    }
    Knife build() {
        String handle = fields.containsKey("Wood") ? "дерево"
                : fields.containsKey("Plastic") ? "пластик" : "метал";
        var visual = new Knife.Visual(Double.parseDouble(get("LengthCm")),
                Double.parseDouble(get("WidthMm")), get("Material"), handle,
                fields.getOrDefault("Wood", ""), bool("Fuller"));
        return new Knife(id, get("Type"), get("Handy"), get("Origin"), visual, bool("Value"));
    }
}
