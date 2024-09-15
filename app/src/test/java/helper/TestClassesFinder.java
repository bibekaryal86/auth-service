package helper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class TestClassesFinder {
  public static List<String> findTestClasses() {
    final String packageName = "user.management.system";
    try {
      List<String> testClasses = new ArrayList<>();
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Enumeration<URL> resources = classLoader.getResources(packageName.replace('.', '/'));

      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        File directory = new File(resource.getFile());
        if (directory.exists()) {
          processDirectory(directory, packageName, testClasses, 0);
        }
      }
      return testClasses;
    } catch (IOException ex) {
      return Collections.emptyList();
    }
  }

  private static void processDirectory(
      final File directory,
      final String packageName,
      final List<String> testClasses,
      final int level) {
    if (level > 3) return; // Limit recursion to 2 levels

    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile() && file.getName().endsWith(".class")) {
          String className = file.getName();
          String classSimpleName =
              className.substring(0, className.length() - 6); // Remove ".class"
          if (classSimpleName.endsWith("Test")) {
            String fullClassName = packageName + '.' + classSimpleName;
            try {
              Class<?> clazz = Class.forName(fullClassName);
              if (!Modifier.isAbstract(clazz.getModifiers())) {
                testClasses.add(clazz.getSimpleName());
              }
            } catch (ClassNotFoundException e) {
              // ignored
            }
          }
        } else if (file.isDirectory()) {
          String subPackageName = packageName + '.' + file.getName();
          processDirectory(file, subPackageName, testClasses, level + 1);
        }
      }
    }
  }
}
