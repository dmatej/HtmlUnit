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
package com.gargoylesoftware.js.nashorn.internal.runtime.arrays;

import static com.gargoylesoftware.js.nashorn.internal.runtime.ECMAErrors.typeError;

import com.gargoylesoftware.js.nashorn.internal.objects.Global;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptRuntime;

/**
 * Filter class that wrap arrays that have been tagged non extensible
 */
final class NonExtensibleArrayFilter extends ArrayFilter {

    /**
     * Constructor
     * @param underlying array
     */
    NonExtensibleArrayFilter(final ArrayData underlying) {
        super(underlying);
    }

    @Override
    public ArrayData copy() {
        return new NonExtensibleArrayFilter(underlying.copy());
    }

    @Override
    public ArrayData slice(final long from, final long to) {
        return new NonExtensibleArrayFilter(underlying.slice(from, to));
    }

    private ArrayData extensionCheck(final boolean strict, final int index) {
        if (!strict) {
            return this;
        }
        throw typeError(Global.instance(), "object.non.extensible", String.valueOf(index), ScriptRuntime.safeToString(this));
    }

    @Override
    public ArrayData set(final int index, final Object value, final boolean strict) {
        if (has(index)) {
            return underlying.set(index, value, strict);
        }
        return extensionCheck(strict, index);
    }

    @Override
    public ArrayData set(final int index, final int value, final boolean strict) {
        if (has(index)) {
            return underlying.set(index, value, strict);
        }
        return extensionCheck(strict, index);
    }

    @Override
    public ArrayData set(final int index, final long value, final boolean strict) {
        if (has(index)) {
            return underlying.set(index, value, strict);
        }
        return extensionCheck(strict, index);
    }

    @Override
    public ArrayData set(final int index, final double value, final boolean strict) {
        if (has(index)) {
            return underlying.set(index, value, strict);
        }
        return extensionCheck(strict, index);
    }
}
