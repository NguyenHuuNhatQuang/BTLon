package model.item;

public class ItemFactory {

    public static Item createItem(String type, String id, String name,
                                  Object extraInfo) {
        return switch (type.toUpperCase()) {
            case "ELECTRONICS" -> new Electronics(id, name, (int) extraInfo);
            case "ART"         -> new Art(id, name, (String) extraInfo);
            case "VEHICLE"     -> new Vehicle(id, name, (int) extraInfo);
            case "JEWELRY"     -> new Jewelry(id, name, (String) extraInfo);
            case "WATCH"       -> new Watch(id, name, (String) extraInfo);
            default -> throw new IllegalArgumentException("Loại item không hỗ trợ: " + type);
        };
    }
}