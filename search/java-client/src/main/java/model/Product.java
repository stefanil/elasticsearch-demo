package model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a product.
 */
public class Product {

  private String name;

  private String description;

  private List<Feature> features;

  public Product() {
  }

  public Product(final String name, final String description, final List<Feature> features) {
    this.name = name;
    this.description = description;
    this.features = features;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public List<Feature> getFeatures() {
    return features;
  }

  public void setFeatures(final List<Feature> features) {
    this.features = features;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Product))
      return false;
    final Product product = (Product) o;
    return Objects.equals(name, product.name) &&
        Objects.equals(description, product.description) &&
        Objects.equals(features, product.features);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, features);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Product{");
    sb.append("name='").append(name).append('\'');
    sb.append(", description='").append(description).append('\'');
    sb.append(", features=").append(features);
    sb.append('}');
    return sb.toString();
  }
}
