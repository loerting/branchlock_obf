package net.branchlock.decompiler;

import net.branchlock.commons.asm.Conversion;
import net.branchlock.commons.os.TimeLimiter;
import net.branchlock.structure.BClass;
import org.apache.commons.io.IOUtils;
import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CFREmulator {
  protected static final Map<String, String> options = new HashMap<>();

  static {
    options.put("showversion", "false");
    options.put("hidelongstrings", "true");
    options.put("hideutf", "false");
    options.put("innerclasses", "false");
    options.put("recover", "true");
    options.put("labelledblocks", "true");
  }

  private final BClass clazz;

  private String decompiledOutput;

  public CFREmulator(BClass clazz) {
    this.clazz = clazz;
  }

  public byte[] decompile() {
    String name = clazz.name;
    byte[] bytes = Conversion.toBytecode0(clazz);
    this.decompiledOutput = null;
    OutputSinkFactory mySink = new OutputSinkFactory() {
      @Override
      public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> available) {
        if (sinkType == SinkType.JAVA && available.contains(SinkClass.DECOMPILED)) {
          return Arrays.asList(SinkClass.DECOMPILED, SinkClass.STRING);
        } else {
          return Collections.singletonList(SinkClass.STRING);
        }
      }

      @Override
      public <T> Sink<T> getSink(SinkType sinkType, SinkClass sinkClass) {
        if (sinkType == SinkType.JAVA && sinkClass == SinkClass.DECOMPILED) {
          return x -> decompiledOutput = ((SinkReturns.Decompiled) x).getJava().substring(31);
        }
        return ignore -> {
        };
      }
    };
    ClassFileSource source = new ClassFileSource() {
      @Override
      public void informAnalysisRelativePathDetail(String usePath, String classFilePath) {
        // nothing to inform here
      }

      @Override
      public String getPossiblyRenamedPath(String path) {
        return path;
      }

      @Override
      public Pair<byte[], String> getClassFileContent(String path) throws IOException {
        String clzName = path.substring(0, path.length() - 6);
        if (clzName.equals(name)) {
          return Pair.make(bytes, clzName);
        }
        URL url = CFREmulator.class.getResource("/" + path);
        if (url != null) {
          return Pair.make(IOUtils.toByteArray(url), path);
        }
        ClassNode dummy = new ClassNode();
        dummy.name = clzName;
        dummy.version = 52;
        return Pair.make(Conversion.toBytecode0(dummy), clzName);
      }

      @Override
      public Collection<String> addJar(String jarPath) {
        throw new RuntimeException();
      }
    };
    try {
      TimeLimiter.withLimit(3000, () -> {
        CfrDriver cfrDriver =
          new CfrDriver.Builder().withClassFileSource(source).withOutputSink(mySink).withOptions(options).build();
        cfrDriver.analyse(Collections.singletonList(name));
      }, () -> decompiledOutput = "/* Decompiler took too long */");
    } catch (Throwable t) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      decompiledOutput = "/* Decompiler crash. */\n" + sw;
    }
    if (decompiledOutput == null || decompiledOutput.trim().isEmpty()) {
      decompiledOutput = "/* No decompiler output received. */";
    }
    return decompiledOutput.getBytes(StandardCharsets.UTF_8);
  }
}
