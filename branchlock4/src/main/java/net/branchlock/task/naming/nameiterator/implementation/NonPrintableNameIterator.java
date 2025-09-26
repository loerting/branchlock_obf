package net.branchlock.task.naming.nameiterator.implementation;


import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.stream.IntStream;

public final class NonPrintableNameIterator extends AlphabetIterator {
  public static final Font BASE_FONT = new Font("Monospace", Font.PLAIN, 48);
  private static final Comparator<Character> LENGTH = Comparator.comparingDouble(c -> BASE_FONT
    .getStringBounds(String.valueOf((char) c), new FontRenderContext(new AffineTransform(), true, true)).getWidth());
  private static final Comparator<Character> FILLOUT = Comparator.comparingDouble(NonPrintableNameIterator::getFilloutPercentage).reversed();
  public NonPrintableNameIterator() {
    // large letters
    // super("\uf9ca\ufad1\ufa22\ufa40\uf905\uf947\uf907\uf91b\ufa69\uf997\ufac3\ufa17\uf960\uf915\uf90b\ufa23\uf9f9\uf91f\uf98a\uf9a7\uf96e\ufad9\uf916\ufac2\ufa24\uf930\ufa10\ufa16\uf98e\uf9f0\uf978\uf9dc\u2188\uf999\ufa64\ufad8\uf954\ufa83\ufa3c\ufa12\ufa27\ufabd\uf9f8\uf986\uf948\uf99a\uf991\u1699\ufabe\uf96b\ufa47\ufa15\ufa6d\uf98c\uf9a6\uf96f\ufa31\uf9c6\ufa79\ufa06\uf98d\u168f\uf921\ufa2e\uf9ce\uf923\ufa7a\uf9b0\ufaa0\ufab8\u2182\ufa6b\uf934\ufa44\ufac4\uf967\uf965\ufa3d\uf938\u1029\ufa37\uf929\uf933\ufab2\ufa45\u0b94\uf92c\ufa2d\uf900\ua51e\ufa68\ufa48\ufa14\ufa3a\uf992\uf9a8\ufad7\ufa1f\ufa02\ufab7\ufa4f\ufa84\ufa9e\uf919\ufa2b\u1bae\uf9de\ufa43\uf92e\ufa87\ufa8e\uf92a\uf935\uf93e\u0b8a\uf932\uf9dd\ufdfb\ufa0c\ufa75\uf9c9\u2c06\uf94c\uf9c2\ufaa4\u1baf\ufa35\uf9c3");

    super("\u0620\u063d\u063e\u063f\u0750\u0751\u0752\u0753\u0754\u0755\u0756\u0757\u0758\u0759\u075a\u075b\u075c\u075d\u075e\u075f\u0760\u0761\u0762\u0763\u0767\u0768\u0769\u076a\u076b\u076c\u076d\u076e\u076f\u0770\u0771\u0772\u0773\u0774\u0775\u0776\u0777\u0778\u0779\u077a\u077b\u077c\u077d\u08a0\u08a1\u08a2\u08a3\u08a4\u08a5\u08a6\u08a7\u08a8\u08a9\u08aa\u08ab\u08ac\u08ae\u08af\u08b0\u08b1\u08b2\u08b3\u08b4\u08b6\u08b7\u08b8\u08b9\u08ba\u08bb\u08bc\u08bd\u08be\u08bf\u08c0\u08c1\u08c2\u08c3\u08c4\u08c5\u08c6\u08c7\u063b\u08ad\u141d\u1427\u0971\u18df\u02be\u02bf\u06ee\u06ef\u06ff\u02d1\u037a\u02cb\u02ce\ufe76\ufe7a\u02ca\u02cf\u0374\u1428\u06e6\u1d62\u2071\u1da5\u02c9\u02cd\ua4f8\u07f5\u09f2\u02e1\u07f4\u2097\u0559\u063c\u0674\u0824\u0828\u18de\u1d63\u16cc\u1dab\u02b3");
  }

    private static double getFilloutPercentage(char character) {
    // draw the character into a 64x64 image, check how much percentage it fills out
    // if it's less than 10%, it's probably a thin character

    BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_BYTE_BINARY);
    Graphics2D graphics = image.createGraphics();
    graphics.setFont(BASE_FONT);
    graphics.drawString(String.valueOf(character), 8, 54);
    graphics.dispose();

    int filledPixels = 0;
    for (int x = 0; x < 64; x++) {
      for (int y = 0; y < 64; y++) {
        if (image.getRGB(x, y) == Color.BLACK.getRGB()) {
          filledPixels++;
        }
      }
    }
    return filledPixels / (double) (64 * 64);
  }

  public static void main(String[] args) {
    String string = IntStream.range(0, Character.MAX_VALUE)
      .filter(Character::isJavaIdentifierStart)
      .mapToObj(i -> (char) i)
      .sorted(FILLOUT)
      .limit(128)
      .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    System.out.println(string);
    // convert to unicode escape sequences
    StringBuilder builder = new StringBuilder();
    for (char c : string.toCharArray()) {
      builder.append("\\u").append(String.format("%04x", (int) c));
    }
    System.out.println(builder.toString());
  }
}
