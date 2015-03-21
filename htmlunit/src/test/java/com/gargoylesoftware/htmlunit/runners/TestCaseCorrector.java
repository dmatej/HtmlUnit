/*
 * Copyright (c) 2002-2015 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.runners;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.ComparisonFailure;
import org.junit.runners.model.FrameworkMethod;

import com.gargoylesoftware.htmlunit.BrowserRunner.NotYetImplemented;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CodeStyleTest;

/**
 * This is meant to automatically correct the test case to put either the real browser expectations,
 * or the {@link NotYetImplemented} annotation for HtmlUnit.
 *
 * <p>For the time being, {@link #correct} needs to be manually called from {@link BrowserStatement#evaluate()},
 * as the first statement inside the {@code catch}, you could use
 * <pre>TestCaseCorrector.correct(method_, realBrowser_, browserVersion_, notYetImplemented_, t);</pre>
 *
 * <p>The implementation is incomplete, Use With Caution!
 *
 * @version $Revision$
 * @author Ahmed Ashour
 */
class TestCaseCorrector {

    private TestCaseCorrector() {
    }

    static void correct(final FrameworkMethod method, final boolean realBrowser, final BrowserVersion browserVersion,
            final boolean notYetImplemented, final Throwable t) throws IOException {
        final String testRoot = "src/test/java/";
        final String browserString = browserVersion.getNickname().toUpperCase();
        final File file = new File(testRoot + method.getDeclaringClass().getName().replace('.', '/') + ".java");
        final List<String> lines = FileUtils.readLines(file);
        final String methodLine = "    public void " + method.getName() + "()";
        if (realBrowser) {
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith(methodLine)) {
                    i = addExpectation(lines, i, browserString, (ComparisonFailure) t);
                    break;
                }
                if (i == lines.size() - 2) {
                    addMethodWithExpectation(lines, i, browserString, method.getName(), (ComparisonFailure) t);
                    break;
                }
            }
        }
        else if (!notYetImplemented) {
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith(methodLine)) {
                    addNotYetImplemented(lines, i, browserString);
                    break;
                }
                if (i == lines.size() - 2) {
                    addNotYetImplementedMethod(lines, i, browserString, method.getName());
                    break;
                }
            }
        }
        else {
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith(methodLine)) {
                    removeNotYetImplemented(lines, i, browserString);
                    break;
                }
            }
        }
        FileUtils.writeLines(file, lines);
    }

    private static int addExpectation(final List<String> lines, int i,
            final String browserString, final ComparisonFailure comparisonFailure) {
        for (; !lines.get(i).startsWith("    @Alerts"); i--) {
            ;
        }
        final List<String> alerts = CodeStyleTest.alertsToList(lines, i);
        for (final Iterator<String> it = alerts.iterator(); it.hasNext();) {
            if (it.next().trim().startsWith(browserString + " = ")) {
                it.remove();
            }
        }
        alerts.add(browserString + " = " + getActualString(comparisonFailure));
        lines.remove(i);
        while (lines.get(i).startsWith("        ")) {
            lines.remove(i);
        }
        for (int x = 0; x < alerts.size(); x++) {
            String line = alerts.get(x);
            if (x == 0) {
                line = "    @Alerts(" + line;
            }
            else {
                line = "            " + line;
            }
            if (x < alerts.size() - 1) {
                line += ",";
            }
            else {
                line += ")";
            }
            lines.add(i++, line);
        }
        return i;
    }

    private static String getActualString(final ComparisonFailure failure) {
        String actual = failure.getActual();
        actual = actual.substring(1, actual.length() - 1);
        return "\"" + actual + "\"";
    }

    private static void removeNotYetImplemented(final List<String> lines,
            int i, final String browserString) {
        final String previous = lines.get(i - 1);
        if (previous.contains("@NotYetImplemented")) {
            if (previous.indexOf('(') != -1) {
                final int p0 = previous.indexOf('(') + 1;
                final int p1 = previous.lastIndexOf(')');
                String browsers = previous.substring(p0, p1);
                if (browsers.indexOf('{') != -1) {
                    browsers = browsers.substring(1, browsers.length() - 1).trim();
                }
                final Set<String> browserSet = new HashSet<>();
                for (final String browser : browsers.split(",")) {
                    browserSet.add(browser.trim());
                }
                browserSet.remove(browserString);
                if (browserSet.size() == 1) {
                    lines.set(i - 1, "    @NotYetImplemented(" + browserSet.iterator().next() + ")");
                }
                else if (browserSet.size() > 1) {
                    lines.set(i - 1, "    @NotYetImplemented({ " + StringUtils.join(browsers, ", " ) + " })");
                }
                else {
                    lines.remove(i - 1);
                }
            }
            else {
                final List<String> allBrowsers = Arrays.asList("CHROME", "IE8", "IE11", "FF24", "FF31");
                for (final Iterator<String> it = allBrowsers.iterator(); it.hasNext();) {
                    if (it.next().equals(browserString)) {
                        it.remove();
                    }
                }
                lines.set(i - 1, "    @NotYetImplemented({ " + StringUtils.join(allBrowsers, ", ") + " })");
            }
        }
    }

    private static void addNotYetImplementedMethod(final List<String> lines,
            int i, final String browserString, final String methodName) {
        String parent = methodName;
        final String child = parent.substring(parent.lastIndexOf('_') + 1);
        parent = parent.substring(1, parent.indexOf('_', 1));

        if (!lines.get(i).isEmpty()) {
            i++;
        }
        lines.add(i++, "");
        lines.add(i++, "    /**");
        lines.add(i++, "     * @throws Exception if the test fails");
        lines.add(i++, "     */");
        lines.add(i++, "    @Test");
        // alert value should come from the \@Default method
        lines.add(i++, "    @Alerts(\"false\")");
        lines.add(i++, "    @NotYetImplemented(" + browserString + ")");
        lines.add(i++, "    public void _" + parent + "_" + child + "() throws Exception {");
        lines.add(i++, "        test(\"" + parent + "\", \"" + child + "\");");
        lines.add(i++, "    }");
        lines.add(i++, "}");
        while (lines.size() > i) {
            lines.remove(i);
        }
    }

    private static void addNotYetImplemented(final List<String> lines, int i, final String browserString) {
        final String previous = lines.get(i - 1);
        if (previous.contains("@NotYetImplemented")) {
            if (previous.indexOf('(') != -1 && !previous.contains(browserString)) {
                final int p0 = previous.indexOf('(') + 1;
                final int p1 = previous.lastIndexOf(')');
                String browsers = previous.substring(p0, p1);
                if (browsers.indexOf('{') != -1) {
                    browsers = browsers.substring(1, browsers.length() - 1).trim();
                }
                browsers += ", " + browserString;
                lines.set(i - 1, "    @NotYetImplemented({ " + browsers + " })");
            }
        }
        else {
            lines.add(i, "    @NotYetImplemented(" + browserString + ")");
        }
    }

    private static void addMethodWithExpectation(final List<String> lines,
            int i, final String browserString, final String methodName, final ComparisonFailure comparisonFailure) {
        String parent = methodName;
        final String child = parent.substring(parent.lastIndexOf('_') + 1);
        parent = parent.substring(1, parent.indexOf('_', 1));

        if (!lines.get(i).isEmpty()) {
            i++;
        }
        lines.add(i++, "");
        lines.add(i++, "    /**");
        lines.add(i++, "     * @throws Exception if the test fails");
        lines.add(i++, "     */");
        lines.add(i++, "    @Test");
        // value should come from the \@Default method
        lines.add(i++, "    @Alerts(DEFAULT = \"1\",");
        lines.add(i++, "            " + browserString + " = " + getActualString(comparisonFailure) + ")");
        lines.add(i++, "    public void _" + parent + "_" + child + "() throws Exception {");
        lines.add(i++, "        test(\"" + parent + "\", \"" + child + "\");");
        lines.add(i++, "    }");
        lines.add(i++, "}");
        while (lines.size() > i) {
            lines.remove(i);
        }
    }

}
