package model.item;

public class Jewelry extends Item {
  private String material; // Chất liệu: Vàng 18K, Bạc 925, Bạch kim...

  public Jewelry(String id, String name, String material) {
    super(id, name);
    this.material = material;
  }

  @Override
  public String getDetails() {
    return "Jewelry - Tên: " + getName() + " | Chất liệu: " + material;
  }
}