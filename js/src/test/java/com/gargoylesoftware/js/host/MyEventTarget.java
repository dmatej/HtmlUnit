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
package com.gargoylesoftware.js.host;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Browser;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.Function;
import com.gargoylesoftware.js.nashorn.internal.objects.annotations.ScriptClass;
import com.gargoylesoftware.js.nashorn.internal.runtime.AccessorProperty;
import com.gargoylesoftware.js.nashorn.internal.runtime.Context;
import com.gargoylesoftware.js.nashorn.internal.runtime.Property;
import com.gargoylesoftware.js.nashorn.internal.runtime.PropertyMap;
import com.gargoylesoftware.js.nashorn.internal.runtime.PrototypeObject;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptFunction;
import com.gargoylesoftware.js.nashorn.internal.runtime.ScriptObject;

@ScriptClass("EventTarget")
public class MyEventTarget extends ScriptObject {

    @com.gargoylesoftware.js.nashorn.internal.objects.annotations.Constructor
    public static MyEventTarget constructor(final boolean newObj, final Object self) {
        final MyEventTarget host = new MyEventTarget();
        host.setProto(Context.getGlobal().getPrototype(host.getClass()));
        return host;
    }

    @Function
    public static String addEventListener(final Object self) {
        return Browser.getCurrent().getFamily().name();
    }

    {
        final List<Property> list = Collections.emptyList();
        setMap(PropertyMap.newMap(list));
    }

    private static MethodHandle staticHandle(final String name, final Class<?> rtype, final Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(MyEventTarget.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class Constructor extends ScriptFunction {
        public Constructor() {
            super("EventTarget", 
                    staticHandle("constructor", MyEventTarget.class, boolean.class, Object.class),
                    null);
            final Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
        }
    }

    static final class Prototype extends PrototypeObject {
        private ScriptFunction addEventListener;

        public ScriptFunction G$addEventListener() {
            return this.addEventListener;
        }

        public void S$addEventListener(final ScriptFunction function) {
            this.addEventListener = function;
        }

        {
            final List<Property> list = new ArrayList<>(1);
            list.add(AccessorProperty.create("addEventListener", Property.WRITABLE_ENUMERABLE_CONFIGURABLE, 
                    virtualHandle("G$addEventListener", ScriptFunction.class),
                    virtualHandle("S$addEventListener", void.class, ScriptFunction.class)));
            setMap(PropertyMap.newMap(list));
        }

        Prototype() {
            addEventListener = ScriptFunction.createBuiltin("addEventListener",
                    staticHandle("addEventListener", String.class, Object.class));
        }

        public String getClassName() {
            return "EventTarget";
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
