package model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a feature with its values the user selected for the product.
 */
public class Feature {

  private String name;
  private List<String> values;

  public Feature() {
  }

  public Feature(final String name, final List<String> values) {
    this.name = name;
    this.values = values;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(final List<String> values) {
    this.values = values;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Feature))
      return false;
    final Feature feature = (Feature) o;
    return Objects.equals(name, feature.name) &&
        Objects.equals(values, feature.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, values);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Feature{");
    sb.append("name='").append(name).append('\'');
    sb.append(", values=").append(values);
    sb.append('}');
    return sb.toString();
  }
}
