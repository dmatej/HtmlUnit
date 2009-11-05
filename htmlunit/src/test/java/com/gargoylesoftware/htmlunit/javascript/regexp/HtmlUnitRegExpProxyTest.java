/*
 * Copyright (c) 2002-2009 Gargoyle Software Inc.
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
package com.gargoylesoftware.htmlunit.javascript.regexp;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.ContextFactory;
import net.sourceforge.htmlunit.corejs.javascript.JavaScriptException;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebDriverTestCase;
import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.BrowserRunner.Browser;
import com.gargoylesoftware.htmlunit.BrowserRunner.Browsers;
import com.gargoylesoftware.htmlunit.BrowserRunner.NotYetImplemented;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.Window;

/**
 * Tests for {@link HtmlUnitRegExpProxy}.
 *
 * @version $Revision$
 * @author Marc Guillemot
 * @author Ahmed Ashour
 */
@RunWith(BrowserRunner.class)
public class HtmlUnitRegExpProxyTest extends WebDriverTestCase {

    private final String str_ = "(?:<script.*?>)((\\n|\\r|.)*?)(?:<\\/script>)";
    private final String begin_ = "<div>bla</div>";
    private final String end_ = "foo\n<span>bla2</span>";
    private final String text_ = begin_ + "<script>var a = 123;</script>" + end_;
    private final String expected_ = begin_ + end_;
    private final String src_ = "var re = new RegExp(str, 'img');\n"
        + "var s = text.replace(re, '');\n"
        + "if (s != expected)\n"
        + " throw 'Expected >' + expected + '< but got >' + s + '<';";

    private final String scriptTestMatch_ = "function arrayToString(_arr) {\n"
        + "  if (_arr == null) return null;\n"
        + "  var s = '[';\n"
        + "  for (var i=0; i<_arr.length; ++i)\n"
        + "  {\n"
        + "    if (i != 0) s += ', '\n"
        + "    s += _arr[i];\n"
        + "  }\n"
        + "  s += ']';\n"
        + "  return s;\n"
        + "}\n"
        + "function assertArrEquals(actual, expected) {\n"
        + "  if (expected == null) {\n"
        + "    if (actual != null)\n"
        + "      throw 'Expected >null< got >' + actual + '<';\n"
        + "    else return;\n"
        + "  }\n"
        + "  var expectedStr = arrayToString(expected);\n"
        + "  var actualStr = arrayToString(actual);\n"
        + "  if (expectedStr != actualStr)\n"
        + "    throw 'Expected >' + expectedStr + '< got >' + actualStr + '<';\n"
        + "}\n"
        + "assertArrEquals('ab'.match(), null);\n"
        + "assertArrEquals('ab'.match('foo'), null);\n"
        + "assertArrEquals('ab'.match('a'), ['a']);\n"
        + "assertArrEquals('abab'.match('a'), ['a']);\n"
        + "assertArrEquals('abab'.match('.a'), ['ba']);\n"
        + "assertArrEquals('abab'.match(/.a/), ['ba']);\n"
        + "assertArrEquals('li'.match(/^([^a-z0-9_-])?([a-z0-9_-]+)(.*)/i), ['li', undefined, 'li', '']);\n"
        + "assertArrEquals('abab'.match(new RegExp('.a')), ['ba']);\n"
        + "var s = '<script>var a = 1;</' + 'script>';\n"
        + "var re = '(?:<script.*?>)((\\n|\\r|.)*?)(?:<\\/script>)';\n"
        + "assertArrEquals(s.match(re), [s, 'var a = 1;', ';']);\n";

    /**
     * Test that string.replace works correctly (?) in HtmlUnit.
     * @throws Exception if the test fails
     */
    @Test
    @Browsers(Browser.NONE)
    public void fixedInHtmlUnit() throws Exception {
        final String html = "<html></html>";
        final HtmlPage page = loadPage(html);
        final Window topScope = ((Window) page.getEnclosingWindow().getScriptObject());
        topScope.put("str", topScope, str_);
        topScope.put("text", topScope, text_);
        topScope.put("expected", topScope, expected_);
        page.executeJavaScript(src_);
    }

    /**
     * Tests if custom patch is still needed.
     */
    @Test
    @Browsers(Browser.NONE)
    public void needCustomFix() {
        final WebClient client = new WebClient();
        final ContextFactory cf = client.getJavaScriptEngine().getContextFactory();
        final Context ctx = cf.enterContext();
        try {
            final ScriptableObject topScope = ctx.initStandardObjects();
            topScope.put("str", topScope, str_);
            topScope.put("text", topScope, text_);
            topScope.put("expected", topScope, expected_);
            assertEquals(begin_ + end_, text_.replaceAll(str_, ""));
            try {
                ctx.evaluateString(topScope, src_, "test script", 0, null);
            }
            catch (final JavaScriptException e) {
                assertTrue(e.getMessage().indexOf("Expected >") == 0);
            }
        }
        finally {
            Context.exit();
        }
    }

    /**
     * Test for bug http://sf.net/tracker/index.php?func=detail&aid=1780089&group_id=47038&atid=448266.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("123456")
    public void replaceNormalStringWithRegexpChars() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    alert('123456'.replace('/{\\d+}', ''));\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("123456")
    public void replaceWithUndefinedPattern() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var pattern;\n"
            + "    alert('123456'.replace(pattern, ''));\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("123456")
    public void replace() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var pattern = /{\\d+}/g;\n"
            + "    alert('123456'.replace(pattern, ''));\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void match() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + scriptTestMatch_
            + "</script></head><body>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Test if the custom fix is needed or not. When this test fails, then it means that the problem is solved in
     * Rhino and that custom fix for String.match in {@link HtmlUnitRegExpProxy} is not needed anymore (and that
     * this test can be removed, or turned positive).
     * @throws Exception if the test fails
     */
    @Test
    public void matchFixNeeded() throws Exception {
        final WebClient client = getWebClient();
        final ContextFactory cf = client.getJavaScriptEngine().getContextFactory();
        final Context cx = cf.enterContext();
        try {
            final ScriptableObject topScope = cx.initStandardObjects();
            cx.evaluateString(topScope, scriptTestMatch_, "test script String.match", 0, null);
            try {
                cx.evaluateString(topScope, scriptTestMatch_, "test script String.match", 0, null);
            }
            catch (final JavaScriptException e) {
                assertTrue(e.getMessage().indexOf("Expected >") == 0);
            }
        }
        finally {
            Context.exit();
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("0")
    public void index() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var match = '#{tests} tests'.match(/(^|.|\\r|\\n)(#\\{(.*?)\\})/);\n"
            + "    alert(match.index);\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("ab,a")
    public void match_NotFirstCharacter() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    alert(\"ab\".match(/^(.)[^\\1]$/))\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @NotYetImplemented
    @Alerts("boo();")
    public void regExp_exec() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var re = new RegExp('(?:<s' + 'cript.*?>)(.*)<\\/script>');\n"
            + "    var t = 'foo <scr' + 'ipt>boo();</' + 'script>bar';\n"
            + "    var r = re.exec(t);\n"
            + "    alert(r[1]);\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("<script>boo();</script>")
    public void flag_global() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var str = 'foo <script>boo();<'+'/script>bar';\n"
            + "    var regExp = new RegExp('<script[^>]*>([\\\\S\\\\s]*?)<\\/script>', 'img');\n"
            + "    alert(str.match(regExp));\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Minimal case exists in {@link #test_minimal()}.  Once successful, either one to be removed.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "true", "false", "true" })
    @NotYetImplemented
    public void test_prototype() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var regexp = /^(?:(?:(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|"
            + "(?:(?:16|[2468][048]|[3579][26])00)))(\\/|-|\\.)(?:0?2\\1(?:29)))|(?:(?:(?:1[6-9]|[2-9]\\d)?\\d{2})"
            + "(\\/|-|\\.)(?:(?:(?:0?[13578]|1[02])\\2(?:31))|(?:(?:0?[1,3-9]|1[0-2])\\2(29|30))|(?:(?:0?[1-9])|"
            + "(?:1[0-2]))\\2(?:0?[1-9]|1\\d|2[0-8]))))$/;\n"
            + "    var str = '2001-06-16';\n"
            + "    alert(regexp.test(str))\n"
            + "    alert(regexp.test('hello'))\n"
            + "    alert(regexp.exec(str) != null)\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void test() throws Exception {
        test("/foo/", "hello foo", true, true, false);
        test("/foo/gi", "hello foo", true, false, false);
        test("/foo/gi", "hello Foo", true, false, false);
    }

    private void test(final String regexp, final String testString, final boolean... expectedResults) throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var regexp = " + regexp + ";\n"
            + "    var str = '" + testString + "';\n"
            + "    alert(regexp.test(str));\n"
            + "    alert(regexp.exec(str) != null);\n"
            + "    alert(regexp.test('blabla'));\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        setExpectedAlerts(String.valueOf(expectedResults[0]), String.valueOf(expectedResults[1]),
            String.valueOf(expectedResults[2]));

        loadPageWithAlerts2(html);
    }

    /**
     * Minimized version of {@link #test()}.  Once successful, either one to be removed.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("true")
    @NotYetImplemented
    public void test_minimal() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var regexp = /((?:2001)-)/;\n"
            + "    var str = '2001-';\n"
            + "    alert(regexp.test(str))\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Verifies that curly braces can be used non escaped in JS regexp.
     */
    @Test
    @Browsers(Browser.NONE)
    public void testEscapeCurlyBraces() {
        assertEquals("\\{", HtmlUnitRegExpProxy.escapeJSCurly("{"));
        assertEquals("\\{", HtmlUnitRegExpProxy.escapeJSCurly("\\{"));
        assertEquals("\\}", HtmlUnitRegExpProxy.escapeJSCurly("}"));
        assertEquals("\\}", HtmlUnitRegExpProxy.escapeJSCurly("\\}"));
        assertEquals("(^|\\{)#([^\\}]+)(\\}|$)", HtmlUnitRegExpProxy.escapeJSCurly("(^|{)#([^}]+)(}|$)"));

        assertEquals("a{5}", HtmlUnitRegExpProxy.escapeJSCurly("a{5}"));
        assertEquals("a{5,}", HtmlUnitRegExpProxy.escapeJSCurly("a{5,}"));
        assertEquals("a{5,10}", HtmlUnitRegExpProxy.escapeJSCurly("a{5,10}"));
    }

    /**
     * Verifies that curly braces can be used non escaped in JS regexp.
     */
    @Test
    @Browsers(Browser.NONE)
    public void escapeOpeningSquareBracketInCharacterClass() {
        assertEquals("[ab\\[]", HtmlUnitRegExpProxy.jsRegExpToJavaRegExp("[ab[]"));
        assertEquals("[\\[]", HtmlUnitRegExpProxy.jsRegExpToJavaRegExp("[[]"));
        assertEquals("[a\\[b]", HtmlUnitRegExpProxy.jsRegExpToJavaRegExp("[a[b]"));
        assertEquals("[ab][a\\[b][ab]", HtmlUnitRegExpProxy.jsRegExpToJavaRegExp("[ab][a[b][ab]"));

        // with already escaped [
        assertEquals("[ab\\[]", HtmlUnitRegExpProxy.jsRegExpToJavaRegExp("[ab\\[]"));
        assertEquals("[\\[]", HtmlUnitRegExpProxy.jsRegExpToJavaRegExp("[\\[]"));
        assertEquals("[a\\[b]", HtmlUnitRegExpProxy.jsRegExpToJavaRegExp("[a\\[b]"));
        assertEquals("[ab][a\\[b][ab]", HtmlUnitRegExpProxy.jsRegExpToJavaRegExp("[ab][a\\[b][ab]"));
    }

    /**
     * Verifies that back references in character classes are removed.
     * @see #jqueryPseudo()
     * @see #ignoreBackReferenceInCharacterClass()
     */
    @Test
    @Browsers(Browser.NONE)
    public void removeBackReferencesInCharacterClasses() {
        assertEquals("(a)(b)[^c]", HtmlUnitRegExpProxy.jsRegExpToJavaRegExp("(a)(b)[^\\2c]"));
        assertEquals("(a)(b)[c]", HtmlUnitRegExpProxy.jsRegExpToJavaRegExp("(a)(b)[\\2c]"));
        assertEquals("(a)(b)[\\\\2c]", HtmlUnitRegExpProxy.jsRegExpToJavaRegExp("(a)(b)[\\\\2c]"));
    }

    /**
     * Tests usage of regex with non escaped curly braces, such as is used by dhtmlGrid.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "{#abcd},{,abcd,}" })
    public void testRegexWithNonEscapedCurlyBraces() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var regexp = /(^|{)#([^}]+)(}|$)/;\n"
            + "    var str = '|{#abcd}|';\n"
            + "    alert(str.match(regexp))\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Tests usage of regex with non escaped curly braces, such as is used by dhtmlGrid.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("aa-b-b-")
    public void testBackSpace() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var regexp = /[p\\bz]/g;\n"
            + "    var str = 'aapbzb' + String.fromCharCode(8);\n"
            + "    alert(str.replace(regexp, '-'))\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("")
    public void dollarSignAndCurlyBracket() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var value = ''.replace(/\\${/g, '');\n"
            + "    alert(value)\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "null", "[" })
    public void openingSquareBracketInCharacterClass() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function test() {\n"
            + "  var re = /[[]/;\n"
            + "  alert('div'.match(re));\n"
            + "  alert('['.match(re));\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * RegExp used in JQuery 1.3.1 that wasn't correctly transformed to Java RegExp in HtmlUnit-2.4.
     * @see #escapeOpeningSquareBracketInCharacterClass()
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "div" })
    public void jquerySizzleChunker() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + " var re = /((?:\\((?:\\([^()]+\\)|[^()]+)+\\)|\\[(?:\\[[^[\\]]*\\]|['\"][^'\"]+['\"]|[^[\\]'\"]+)+\\]"
            + "|\\\\.|[^ >+~,(\\[]+)+|[>+~])(\\s*,\\s*)?/g\n"
            + "  function test() {\n"
            + "    alert('div'.match(re))\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * RegExp used in JQuery 1.3.1 that wasn't correctly transformed to Java RegExp in HtmlUnit-2.4.
     * @see #escapeOpeningSquareBracketInCharacterClass()
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ ":toto,toto,,", "null" })
    public void jqueryPseudo() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + " var re = /:((?:[\\w\\u00c0-\\uFFFF_-]|\\\\.)+)(?:\\((['\"]*)((?:\\([^\\)]+\\)"
            + "|[^\\2\\(\\)]*)+)\\2\\))?/;\n"
            + "  function test() {\n"
            + "    alert(':toto'.match(re));\n"
            + "    alert('foo'.match(re));\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Browsers ignore back references in character classes.
     * This is a simplified case of {@link #jqueryPseudo()}
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "null", "abb,a,b", "abd,a,b" })
    public void ignoreBackReferenceInCharacterClass() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function test() {\n"
            + " var re = /(a)(b)[^\\2c]/;\n"
            + " alert('abc'.match(re));\n"
            + " alert('abb'.match(re));\n"
            + " alert('abd'.match(re));\n"
            + "}\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Tests basic usage of back references in a <tt>string.replace(...)</tt> invocation.
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("afood$0$7b")
    public void replace_backReferences() throws Exception {
        final String html = "<script>alert('afoob'.replace(/(foo)/g, '$1d$0$7'));</script>";
        loadPageWithAlerts2(html);
    }

    /**
     * Regression test for bug 2638813 (dollar signs with no index are not back references).
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("I$want$these$periods$to$be$$s")
    public void replace_backReferences_2() throws Exception {
        final String html = buildHtml("alert('I.want.these.periods.to.be.$s'.replace(/\\./g, '$'));");
        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("kid\\'s toys")
    public void escapeQuote() throws Exception {
        final String script = "alert(\"kid's toys\".replace(/'/g, \"\\\\'\"))";
        final String html = buildHtml(script);
        loadPageWithAlerts2(html);
    }

    private String buildHtml(final String script) {
        return "<html><head><script>function test() {"
            + script
            + "\n}</script>"
            + "</head><body onload='test()'></body></html>";
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("INPUT")
    @NotYetImplemented
    public void test2() throws Exception {
        final String html = buildHtml("var description = 'INPUT#BasisRenameInput';\n"
                + "if(description.match(/^\\s*([a-z0-9\\_\\-]+)/i)) {\n"
                + "  alert(RegExp.$1);\n"
                + "}");
        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("a")
    public void stackOverflow() throws Exception {
        final String s = IOUtils.toString(getClass().getResourceAsStream("stackOverflow.txt"));
        final String html = buildHtml(
                  "var s = '" + s + "';\n"
                + "s = s.replace(/(\\s*\\S+)*/, 'a');\n"
                + "alert(s);\n");
        loadPageWithAlerts2(html);
    }


    /**
     * Regression test for bug 2890953.
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("$$x$")
    public void replaceDollarDollar() throws Exception {
        final String script = "alert('x'.replace(/(x)/g, '$$$$x$$'));";
        final String html = buildHtml(script);

        loadPageWithAlerts2(html);
    }

    /**
     * Original test resides in
     * <a href="http://code.google.com/p/google-web-toolkit/source/browse/trunk/user/test/com/google/gwt/emultest/java/lang/StringTest.java">StringTest</a>.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({ "\\*\\[", "\\\\", "+1", "abcdef", "1\\1abc123\\123de1234\\1234f", "\n  \n", "x  x", "x\"\\", "$$x$" })
    public void testReplaceAll() throws Exception {
        final String html = "<html>\n"
            + "<head>\n"
            + "  <script>\n"
            + "    function test() {\n"
            + "      var regex, replacement, x1, x2, x3, x4, x5;\n"
            + "      regex = $replaceAll('*[', "
            + "'([/\\\\\\\\\\\\.\\\\*\\\\+\\\\?\\\\|\\\\(\\\\)\\\\[\\\\]\\\\{\\\\}])', '\\\\\\\\$1');\n"
            + "      alert(regex);\n"
            + "      replacement = "
            + "$replaceAll($replaceAll('\\\\', '\\\\\\\\', '\\\\\\\\\\\\\\\\'), '\\\\$', '\\\\\\\\$');\n"
            + "      alert(replacement);\n"
            + "      alert($replaceAll('*[1', regex, '+'));\n"
            + "      x1 = 'xxxabcxxdexf';\n"
            + "      alert($replaceAll(x1, 'x*', ''));\n"
            + "      x2 = '1abc123de1234f';\n"
            + "      alert($replaceAll(x2, '([1234]+)', '$1\\\\\\\\$1'));\n"
            + "      x3 = 'x  x';\n"
            + "      alert($replaceAll(x3, 'x', '\\n'));\n"
            + "      x4 = 'x  \\n';\n"
            + "      alert($replaceAll(x4, '\\\\\\n', 'x'));\n"
            + "      x5 = 'x';\n"
            + "      alert($replaceAll(x5, 'x', '\\\\x\\\\\"\\\\\\\\'));\n"
            + "      alert($replaceAll(x5, '(x)', '\\\\$\\\\$$1\\\\$'));\n"
            + "    }\n"
            + "    function $replaceAll(this$static, regex, replace){\n"
            + "      replace = __translateReplaceString(replace);\n"
            + "      return this$static.replace(RegExp(regex, 'g'), replace);\n"
            + "    }\n"
            + "    function __translateReplaceString(replaceStr){\n"
            + "      var pos = 0;\n"
            + "      while (0 <= (pos = replaceStr.indexOf('\\\\', pos))) {\n"
            + "        if (replaceStr.charCodeAt(pos + 1) == 36) {\n"
            + "          replaceStr = replaceStr.substr(0, pos - 0) + '$' + $substring(replaceStr, ++pos);\n"
            + "        }\n"
            + "        else {\n"
            + "          replaceStr = replaceStr.substr(0, pos - 0) + $substring(replaceStr, ++pos);\n"
            + "        }\n"
            + "      }\n"
            + "      return replaceStr;\n"
            + "    }\n"
            + "    function $substring(this$static, beginIndex){\n"
            + "      return this$static.substr(beginIndex, this$static.length - beginIndex);\n"
            + "    }\n"
            + "  </script>\n"
            + "</head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }
}
