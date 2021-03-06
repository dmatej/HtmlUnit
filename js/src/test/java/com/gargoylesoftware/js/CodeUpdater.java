/*
 * Copyright (c) 2015 Gargoyle Software Inc.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (http://www.gnu.org/licenses/).
 */
package com.gargoylesoftware.js;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * This is meant to overwrite the current code base by original Nashorn.
 * Mainly, the package rename and using external ASM instead of JDK internal one.
 */
public class CodeUpdater {

    public static void main(final String[] args) throws Exception {
        process(new File("../nashorn/src/jdk.scripting.nashorn/share/classes"), true);
        process(new File("../nashorn/buildtools/nasgen/src"), false);
    }

    private static void process(final File dir, final boolean isMain) throws IOException {
        for (final File file : dir.listFiles()) {
            if (file.isDirectory()) {
                process(file, isMain);
            }
            else if (file.getName().endsWith(".java") && !file.getName().equals("package-info.java")) {
                processFile(file, isMain);
            }
        }
    }

    private static void processFile(final File originalFile, final boolean isMain) throws IOException {
        String relativePath = originalFile.getPath().replace('\\', '/');
        relativePath = "com/gargoylesoftware/js/"
                + relativePath.substring(relativePath.indexOf("/jdk/") + "/jdk/".length());
        final String root = isMain ? "src/main/java/" : "src/test/java/";
        final File localFile = new File(root + relativePath);
        if (!localFile.exists()) {
            System.out.println("File doesn't locally exist: " + relativePath);
            return;
        }
        final List<String> originalLines = FileUtils.readLines(originalFile);
        final List<String> localLines = FileUtils.readLines(localFile);

        while (!isCodeStart(originalLines.get(0))) {
            originalLines.remove(0);
        }
        for (int i = 0; i < localLines.size(); i++) {
            if (isCodeStart(localLines.get(i))) {
                while (i < localLines.size()) {
                    localLines.remove(i);
                }
                break;
            }
        }
        for (int i = 0; i < originalLines.size(); i++) {
            String line = originalLines.get(i);
            line = line.replace("jdk.internal.org.objectweb.asm", "org.objectweb.asm");
            line = line.replace("jdk.nashorn.internal", "com.gargoylesoftware.js.nashorn.internal");
            line = line.replace("jdk/nashorn/internal", "com/gargoylesoftware/js/nashorn/internal");
            line = line.replace("jdk/nashorn/javaadapters", "com/gargoylesoftware/js/nashorn/javaadapters");
            line = line.replace("jdk.nashorn.api", "com.gargoylesoftware.js.nashorn.api");
            line = line.replace("jdk.nashorn.tools", "com.gargoylesoftware.js.nashorn.tools");
            line = line.replace("jdk.internal.dynalink", "com.gargoylesoftware.js.internal.dynalink");
            line = line.replace("  @Constructor",
                    "  @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Constructor");
            line = line.replace("  @Property",
                    "  @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Property");
            originalLines.set(i, line);
            if (line.equals("@jdk.Exported")) {
                originalLines.remove(i--);
            }
        }
        localLines.addAll(originalLines);
        FileUtils.writeLines(localFile, localLines);
    }

    private static boolean isCodeStart(final String line) {
        return !line.isEmpty() && Character.isAlphabetic(line.charAt(0)) && !line.startsWith("package")
                && !line.startsWith("import");
    }
}
