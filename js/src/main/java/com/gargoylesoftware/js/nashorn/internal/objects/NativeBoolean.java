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
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.gargoylesoftware.js.nashorn.internal.objects;

import static com.gargoylesoftware.js.nashorn.internal.lookup.Lookup.MH;
import static com.gargoylesoftware.js.nashorn.internal.runtime.ECMAErrors.typeError;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gargoylesoftware.js.internal.dynalink.linker.GuardedInvocation;
import com.gargoylesoftware.js.internal.dynalink.linker.LinkRequest;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Attribute;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Function;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ScriptClass;
import com.gargoylesoftware.js.nashorn.internal.runtime.AccessorProperty;
import com.gargoylesoftware.js.nashorn.internal.runtime.JSType;
import com.gargoylesoftware.js.nashorn.internal.runtime.Property;
import com.gargoylesoftware.js.nashorn.internal.runtime.PropertyMap;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptRuntime;
import com.gargoylesoftware.js.nashorn.internal.runtime.linker.PrimitiveLookup;

/**
 * ECMA 15.6 Boolean Objects.
 */

@ScriptClass("Boolean")
public final class NativeBoolean extends ScriptObject {
    private final boolean value;

    /** Method handle to create an object wrapper for a primitive boolean. */
    static final MethodHandle WRAPFILTER = findOwnMH("wrapFilter", MH.type(NativeBoolean.class, Object.class));
    /** Method handle to retrieve the Boolean prototype object. */
    private static final MethodHandle PROTOFILTER = findOwnMH("protoFilter", MH.type(Object.class, Object.class));

    // initialized by nasgen
    private static PropertyMap $nasgenmap$;

    private NativeBoolean(final boolean value, final ScriptObject proto, final PropertyMap map) {
        super(proto, map);
        this.value = value;
    }

    NativeBoolean(final boolean flag, final Global global) {
        this(flag, global.getBooleanPrototype(), $nasgenmap$);
    }

    NativeBoolean(final boolean flag) {
        this(flag, Global.instance());
    }

    @Override
    public String safeToString() {
        return "[Boolean " + toString() + "]";
    }

    @Override
    public String toString() {
        return Boolean.toString(getValue());
    }

    /**
     * Get the value for this NativeBoolean
     * @return true or false
     */
    public boolean getValue() {
        return booleanValue();
    }

    /**
     * Get the value for this NativeBoolean
     * @return true or false
     */
    public boolean booleanValue() {
        return value;
    }

    @Override
    public String getClassName() {
        return "Boolean";
    }

    /**
     * ECMA 15.6.4.2 Boolean.prototype.toString ( )
     *
     * @param self self reference
     * @return string representation of this boolean
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE)
    public static String toString(final Object self) {
        return getBoolean(self).toString();
    }

    /**
     * ECMA 15.6.4.3 Boolean.prototype.valueOf ( )
     *
     * @param self self reference
     * @return value of this boolean
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE)
    public static boolean valueOf(final Object self) {
        return getBoolean(self);
    }

    /**
     * ECMA 15.6.2.1 new Boolean (value)
     *
     * @param newObj is the new operator used to instantiate this NativeBoolean
     * @param self   self reference
     * @param value  value of boolean
     * @return the new NativeBoolean
     */
    @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Constructor(arity = 1)
    public static Object constructor(final boolean newObj, final Object self, final Object value) {
        final boolean flag = JSType.toBoolean(value);

        if (newObj) {
            return new NativeBoolean(flag);
        }

        return flag;
    }

    private static Boolean getBoolean(final Object self) {
        if (self instanceof Boolean) {
            return ((Boolean)self);
        } else if (self instanceof NativeBoolean) {
            return ((NativeBoolean)self).getValue();
        } else if (self != null && self == Global.instance().getBooleanPrototype()) {
            return false;
        } else {
            throw typeError("not.a.boolean", ScriptRuntime.safeToString(self));
        }
    }

    /**
     * Lookup the appropriate method for an invoke dynamic call.
     *
     * @param request  The link request
     * @param receiver The receiver for the call
     * @return Link to be invoked at call site.
     */
    public static GuardedInvocation lookupPrimitive(final LinkRequest request, final Object receiver) {
        return PrimitiveLookup.lookupPrimitive(request, Boolean.class, new NativeBoolean((Boolean)receiver), WRAPFILTER, PROTOFILTER);
    }

    /**
     * Wrap a native string in a NativeString object.
     *
     * @param receiver Native string.
     * @return Wrapped object.
     */
    @SuppressWarnings("unused")
    private static NativeBoolean wrapFilter(final Object receiver) {
        return new NativeBoolean((Boolean)receiver);
    }

    @SuppressWarnings("unused")
    private static Object protoFilter(final Object object) {
        return Global.instance().getBooleanPrototype();
    }

    private static MethodHandle findOwnMH(final String name, final MethodType type) {
        return MH.findStatic(MethodHandles.lookup(), NativeBoolean.class, name, type);
    }

    static {
            final List<Property> list = Collections.emptyList();
            $nasgenmap$ = PropertyMap.newMap(list);
    }

    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(NativeBoolean.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static final class Constructor extends ScriptFunction {
        Constructor() {
            super("Boolean", 
                    staticHandle("constructor", Object.class, boolean.class, Object.class, Object.class),
                    null);
            final Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
        }
    }

    static final class Prototype extends PrototypeObject {
        private ScriptFunction toString;
        private ScriptFunction valueOf;
        private static final PropertyMap $nasgenmap$;

        public ScriptFunction G$toString() {
            return this.toString;
        }

        public void S$toString(final ScriptFunction function) {
            this.toString = function;
        }

        public ScriptFunction G$valueOf() {
            return this.valueOf;
        }

        public void S$valueOf(final ScriptFunction function) {
            this.valueOf = function;
        }

        static {
            final List<Property> list = new ArrayList<>(2);
            list.add(AccessorProperty.create("toString", Property.NOT_ENUMERABLE, 
                    virtualHandle("G$toString", ScriptFunction.class),
                    virtualHandle("S$toString", void.class, ScriptFunction.class)));
            list.add(AccessorProperty.create("valueOf", Property.NOT_ENUMERABLE, 
                    virtualHandle("G$valueOf", ScriptFunction.class),
                    virtualHandle("S$valueOf", void.class, ScriptFunction.class)));
            $nasgenmap$ = PropertyMap.newMap(list);
        }

        Prototype() {
            super($nasgenmap$);
            toString = ScriptFunction.createBuiltin("toString",
                    staticHandle("toString", String.class, Object.class));
            valueOf = ScriptFunction.createBuiltin("valueOf",
                    staticHandle("valueOf", boolean.class, Object.class));
        }

        public String getClassName() {
            return "Boolean";
        }

        private static MethodHandle virtualHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
            try {
                return MethodHandles.lookup().findVirtual(Prototype.class, name,
                        MethodType.methodType(rtype, ptypes));
            }
            catch (final ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
