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
/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.gargoylesoftware.js.nashorn.internal.runtime.regexp.joni.constants;

@SuppressWarnings("javadoc")
public interface NodeStatus {
    /* status bits */
    final int NST_MIN_FIXED            = (1<<0);
    final int NST_MAX_FIXED            = (1<<1);
    final int NST_CLEN_FIXED           = (1<<2);
    final int NST_MARK1                = (1<<3);
    final int NST_MARK2                = (1<<4);
    final int NST_MEM_BACKREFED        = (1<<5);
    final int NST_STOP_BT_SIMPLE_REPEAT= (1<<6);
    final int NST_RECURSION            = (1<<7);
    final int NST_CALLED               = (1<<8);
    final int NST_ADDR_FIXED           = (1<<9);
    final int NST_NAMED_GROUP          = (1<<10);
    final int NST_NAME_REF             = (1<<11);
    final int NST_IN_REPEAT            = (1<<12);   /* STK_REPEAT is nested in stack. */
    final int NST_NEST_LEVEL           = (1<<13);
    final int NST_BY_NUMBER            = (1<<14);   /* {n,m} */
}
