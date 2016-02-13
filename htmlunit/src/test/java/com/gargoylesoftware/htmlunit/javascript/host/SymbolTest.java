/*
 * Copyright (c) 2002-2016 Gargoyle Software Inc.
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
package com.gargoylesoftware.htmlunit.javascript.host;

import static com.gargoylesoftware.htmlunit.BrowserRunner.Browser.CHROME;
import static com.gargoylesoftware.htmlunit.BrowserRunner.Browser.FF38;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.BrowserRunner.NotYetImplemented;
import com.gargoylesoftware.htmlunit.WebDriverTestCase;
import com.gargoylesoftware.htmlunit.html.HtmlPageTest;

/**
 * Tests for {@link Symbol}.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class SymbolTest extends WebDriverTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "symbol",
            FF31 = "not supported",
            IE = "not supported")
    public void iterator() throws Exception {
        name("iterator");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            FF31 = "not supported",
            IE = "not supported")
    public void match() throws Exception {
        name("match");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            FF31 = "not supported",
            IE = "not supported")
    public void replace() throws Exception {
        name("replace");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            FF31 = "not supported",
            IE = "not supported")
    public void search() throws Exception {
        name("search");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            FF31 = "not supported",
            IE = "not supported")
    public void split() throws Exception {
        name("split");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            FF31 = "not supported",
            IE = "not supported")
    public void hasInstance() throws Exception {
        name("hasInstance");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            CHROME = "symbol",
            FF31 = "not supported",
            IE = "not supported")
    @NotYetImplemented(CHROME)
    public void isConcatSpreadable() throws Exception {
        name("isConcatSpreadable");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "symbol",
            FF38 = "undefined",
            FF31 = "not supported",
            IE = "not supported")
    public void unscopables() throws Exception {
        name("unscopables");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            FF31 = "not supported",
            IE = "not supported")
    public void species() throws Exception {
        name("species");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            CHROME = "symbol",
            FF31 = "not supported",
            IE = "not supported")
    @NotYetImplemented(CHROME)
    public void toPrimitive() throws Exception {
        name("toPrimitive");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            FF31 = "not supported",
            IE = "not supported")
    public void toStringTag() throws Exception {
        name("toStringTag");
    }

    private void name(final String name) throws Exception {
        final String html =
            HtmlPageTest.STANDARDS_MODE_PREFIX_
            + "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    if (!window.Symbol) { alert('not supported'); return; }\n"
            + "    alert(typeof Symbol." + name + ");\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";
        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "Symbol(Symbol.iterator)",
            FF31 = "not supported",
            IE = "not supported")
    public void string() throws Exception {
        final String html =
            HtmlPageTest.STANDARDS_MODE_PREFIX_
            + "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    if (!window.Symbol) { alert('not supported'); return; }\n"
            + "    alert(Symbol.iterator.toString());\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";
        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "Symbol(Symbol.iterator)",
            FF31 = "not supported",
            IE = "not supported")
    @NotYetImplemented({FF38, CHROME})
    public void defaultValue() throws Exception {
        final String html =
            HtmlPageTest.STANDARDS_MODE_PREFIX_
            + "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    if (!window.Symbol) { alert('not supported'); return; }\n"
            + "    try {"
            + "      alert(Symbol.iterator);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";
        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = { "function", "symbol", "symbol", "symbol" },
            FF31 = "not supported",
            IE = "not supported")
    public void typeOf() throws Exception {
        final String html =
            HtmlPageTest.STANDARDS_MODE_PREFIX_
            + "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    if (!window.Symbol) { alert('not supported'); return; }\n"
            + "    try {"
            + "      alert(typeof Symbol);\n"
            + "      alert(typeof Symbol());\n"
            + "      alert(typeof Symbol('foo'));\n"
            + "      alert(typeof Symbol.iterator);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";
        loadPageWithAlerts2(html);
    }
}
