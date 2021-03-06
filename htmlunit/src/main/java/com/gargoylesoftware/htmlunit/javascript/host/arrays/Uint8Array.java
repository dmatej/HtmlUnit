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
package com.gargoylesoftware.htmlunit.javascript.host.arrays;

import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstant;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;

import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

/**
 * Represents an array of unsigned 16-bit integers.
 *
 * @author Ahmed Ashour
 * @author Frank Danek
 */
@JsxClass
public class Uint8Array extends ArrayBufferViewBase {

    /** The size, in bytes, of each array element. */
    @JsxConstant
    public static final int BYTES_PER_ELEMENT = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    @JsxConstructor
    public void constructor(final Object object, final Object byteOffset, final Object length) {
        super.constructor(object, byteOffset, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte[] toArray(final Number number) {
        return new byte[] {number.byteValue()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object fromArray(final byte[] array, final int offset) {
        if (offset >= array.length) {
            return Scriptable.NOT_FOUND;
        }
        return array[offset] & 0xFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getBytesPerElement() {
        return BYTES_PER_ELEMENT;
    }

}
