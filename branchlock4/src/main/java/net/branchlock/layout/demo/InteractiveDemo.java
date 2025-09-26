package net.branchlock.layout.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InteractiveDemo {
  private static final double PI = 3.14159;

  @Deprecated(since = "1.2")
  private final String secretAPIKey;
  private final int mode;
  private final List<Double> circleRadii = new ArrayList<>();

  public InteractiveDemo(String secretAPIKey, int mode) {
    this.secretAPIKey = secretAPIKey;
    this.mode = mode;
  }

  public static void main(String[] args) {
    var interactiveDemo = new InteractiveDemo("0f4d58642c9bcfc1ee71c6c2483f462a", 1);
    interactiveDemo.retrieveRadii();
    interactiveDemo.calculateResults();
  }

  public void retrieveRadii() {
    try {
      URL url = new URI("https://branchlock.net/circleRadii?key=" + secretAPIKey).toURL();
      BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        circleRadii.add(Double.parseDouble(line));
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to retrieve circle radii from server. ", e);
    }
  }

  private void calculateResults() {
    double result = switch (mode) {
      case 0, 1 -> {
        System.out.println("Summing areas...");
        double sum = 0;
        for (int i = 0; i < circleRadii.size(); i++) {
          double radius = circleRadii.get(i);
          sum += PI * radius * radius;
        }
        yield sum;
      }
      case 2 -> {
        System.out.println("Summing circumferences...");
        double sum = 0;
        for (int i = 0; i < circleRadii.size(); i++) {
          double radius = circleRadii.get(i);
          sum += 2 * PI * radius;
        }
        yield sum;
      }
      default -> throw new IllegalArgumentException("Invalid mode: " + mode);
    };
    System.out.println("Result: " + result);
    circleRadii.clear();
  }
}
