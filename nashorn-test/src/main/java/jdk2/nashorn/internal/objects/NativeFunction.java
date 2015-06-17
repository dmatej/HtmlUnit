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

package jdk2.nashorn.internal.objects;

import static jdk2.nashorn.internal.runtime.ECMAErrors.rangeError;
import static jdk2.nashorn.internal.runtime.ECMAErrors.typeError;
import static jdk2.nashorn.internal.runtime.ScriptRuntime.UNDEFINED;
import static jdk2.nashorn.internal.runtime.Source.sourceFor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import jdk2.internal.dynalink.support.Lookup;
import jdk2.nashorn.api.scripting.JSObject;
import jdk2.nashorn.internal.parser.Parser;
import jdk2.nashorn.internal.runtime.AccessorProperty;
import jdk2.nashorn.internal.runtime.Context;
import jdk2.nashorn.internal.runtime.JSType;
import jdk2.nashorn.internal.runtime.ParserException;
import jdk2.nashorn.internal.runtime.Property;
import jdk2.nashorn.internal.runtime.PropertyMap;
import jdk2.nashorn.internal.runtime.ScriptEnvironment;
import jdk2.nashorn.internal.runtime.ScriptFunction;
import jdk2.nashorn.internal.runtime.ScriptObject;
import jdk2.nashorn.internal.runtime.ScriptRuntime;
import jdk2.nashorn.internal.runtime.Specialization;
import jdk2.nashorn.internal.runtime.linker.Bootstrap;

/**
 * ECMA 15.3 Function Objects
 *
 * Note: instances of this class are never created. This class is not even a
 * subclass of ScriptObject. But, we use this class to generate prototype and
 * constructor for "Function".
 */
public final class NativeFunction {

    /** apply arg converter handle */
    public static final MethodHandle TO_APPLY_ARGS = Lookup.findOwnStatic(MethodHandles.lookup(), "toApplyArgs", Object[].class, Object.class);

    // do *not* create me!
    private NativeFunction() {
        throw new UnsupportedOperationException();
    }

    /**
     * ECMA 15.3.4.2 Function.prototype.toString ( )
     *
     * @param self self reference
     * @return string representation of Function
     */
    public static String toString(final Object self) {
        if (!(self instanceof ScriptFunction)) {
            throw typeError("not.a.function", ScriptRuntime.safeToString(self));
        }
        return ((ScriptFunction)self).toSource();
    }

    /**
     * ECMA 15.3.4.3 Function.prototype.apply (thisArg, argArray)
     *
     * @param self   self reference
     * @param thiz   {@code this} arg for apply
     * @param array  array of argument for apply
     * @return result of apply
     */
    public static Object apply(final Object self, final Object thiz, final Object array) {
        checkCallable(self);

        final Object[] args = toApplyArgs(array);

        if (self instanceof ScriptFunction) {
            return ScriptRuntime.apply((ScriptFunction)self, thiz, args);
        } else if (self instanceof JSObject) {
            return ((JSObject)self).call(thiz, args);
        }
        throw new AssertionError("Should not reach here");
    }

    /**
     * Given an array-like object, converts it into a Java object array suitable for invocation of ScriptRuntime.apply
     * or for direct invocation of the applied function.
     * @param array the array-like object. Can be null in which case a zero-length array is created.
     * @return the Java array
     */
    public static Object[] toApplyArgs(final Object array) {
        if (array instanceof NativeArguments) {
            return ((NativeArguments)array).getArray().asObjectArray();
        } else if (array instanceof ScriptObject) {
            // look for array-like object
            final ScriptObject sobj = (ScriptObject)array;
            final int n = lengthToInt(sobj.getLength());

            final Object[] args = new Object[n];
            for (int i = 0; i < args.length; i++) {
                args[i] = sobj.get(i);
            }
            return args;
        } else if (array instanceof Object[]) {
            return (Object[])array;
        } else if (array instanceof List) {
            final List<?> list = (List<?>)array;
            return list.toArray(new Object[list.size()]);
        } else if (array == null || array == UNDEFINED) {
            return ScriptRuntime.EMPTY_ARRAY;
        } else if (array instanceof JSObject) {
            // look for array-like JSObject object
            final JSObject jsObj = (JSObject)array;
            final Object   len  = jsObj.hasMember("length")? jsObj.getMember("length") : Integer.valueOf(0);
            final int n = lengthToInt(len);

            final Object[] args = new Object[n];
            for (int i = 0; i < args.length; i++) {
                args[i] = jsObj.hasSlot(i)? jsObj.getSlot(i) : UNDEFINED;
            }
            return args;
        } else {
            throw typeError("function.apply.expects.array");
        }
    }

    private static int lengthToInt(final Object len) {
        final long ln = JSType.toUint32(len);
        // NOTE: ECMASCript 5.1 section 15.3.4.3 says length should be treated as Uint32, but we wouldn't be able to
        // allocate a Java array of more than MAX_VALUE elements anyway, so at this point we have to throw an error.
        // People applying a function to more than 2^31 arguments will unfortunately be out of luck.
        if (ln > Integer.MAX_VALUE) {
            throw rangeError("range.error.inappropriate.array.length", JSType.toString(len));
        }
        return (int)ln;
    }

    private static void checkCallable(final Object self) {
        if (!(self instanceof ScriptFunction || (self instanceof JSObject && ((JSObject)self).isFunction()))) {
            throw typeError("not.a.function", ScriptRuntime.safeToString(self));
        }
    }

    /**
     * ECMA 15.3.4.4 Function.prototype.call (thisArg [ , arg1 [ , arg2, ... ] ] )
     *
     * @param self self reference
     * @param args arguments for call
     * @return result of call
     */
    public static Object call(final Object self, final Object... args) {
        checkCallable(self);

        final Object thiz = (args.length == 0) ? UNDEFINED : args[0];
        Object[] arguments;

        if (args.length > 1) {
            arguments = new Object[args.length - 1];
            System.arraycopy(args, 1, arguments, 0, arguments.length);
        } else {
            arguments = ScriptRuntime.EMPTY_ARRAY;
        }

        if (self instanceof ScriptFunction) {
            return ScriptRuntime.apply((ScriptFunction)self, thiz, arguments);
        } else if (self instanceof JSObject) {
            return ((JSObject)self).call(thiz, arguments);
        }

        throw new AssertionError("should not reach here");
    }

    /**
     * ECMA 15.3.4.5 Function.prototype.bind (thisArg [, arg1 [, arg2, ...]])
     *
     * @param self self reference
     * @param args arguments for bind
     * @return function with bound arguments
     */
    public static Object bind(final Object self, final Object... args) {
        final Object thiz = (args.length == 0) ? UNDEFINED : args[0];

        Object[] arguments;
        if (args.length > 1) {
            arguments = new Object[args.length - 1];
            System.arraycopy(args, 1, arguments, 0, arguments.length);
        } else {
            arguments = ScriptRuntime.EMPTY_ARRAY;
        }

        return Bootstrap.bindCallable(self, thiz, arguments);
    }

    /**
     * Nashorn extension: Function.prototype.toSource
     *
     * @param self self reference
     * @return source for function
     */
    public static String toSource(final Object self) {
        if (!(self instanceof ScriptFunction)) {
            throw typeError("not.a.function", ScriptRuntime.safeToString(self));
        }
        return ((ScriptFunction)self).toSource();
    }

    /**
     * ECMA 15.3.2.1 new Function (p1, p2, ... , pn, body)
     *
     * Constructor
     *
     * @param newObj is the new operator used for constructing this function
     * @param self   self reference
     * @param args   arguments
     * @return new NativeFunction
     */
    public static ScriptFunction function(final boolean newObj, final Object self, final Object... args) {
        final StringBuilder sb = new StringBuilder();

        sb.append("(function (");
        final String funcBody;
        if (args.length > 0) {
            final StringBuilder paramListBuf = new StringBuilder();
            for (int i = 0; i < args.length - 1; i++) {
                paramListBuf.append(JSType.toString(args[i]));
                if (i < args.length - 2) {
                    paramListBuf.append(",");
                }
            }

            // now convert function body to a string
            funcBody = JSType.toString(args[args.length - 1]);

            final String paramList = paramListBuf.toString();
            if (!paramList.isEmpty()) {
                checkFunctionParameters(paramList);
                sb.append(paramList);
            }
        } else {
            funcBody = null;
        }

        sb.append(") {\n");
        if (args.length > 0) {
            checkFunctionBody(funcBody);
            sb.append(funcBody);
            sb.append('\n');
        }
        sb.append("})");

        final Global global = Global.instance();

        return (ScriptFunction)Global.directEval(global, sb.toString(), global, "<function>", global.isStrictContext());
    }

    private static void checkFunctionParameters(final String params) {
        final Parser parser = getParser(params);
        try {
            parser.parseFormalParameterList();
        } catch (final ParserException pe) {
            pe.throwAsEcmaException();
        }
    }

    private static void checkFunctionBody(final String funcBody) {
        final Parser parser = getParser(funcBody);
        try {
            parser.parseFunctionBody();
        } catch (final ParserException pe) {
            pe.throwAsEcmaException();
        }
    }

    private static Parser getParser(final String sourceText) {
        final ScriptEnvironment env = Global.getEnv();
        return new Parser(env, sourceFor("<function>", sourceText), new Context.ThrowErrorManager(), env._strict, null);
    }

    final static class Constructor extends ScriptFunctionImpl {
        private static final PropertyMap $nasgenmap$ = PropertyMap.newMap(new ArrayList<>(1));

        Constructor() {
            super("Function", 
                    staticHandle("function", ScriptFunction.class, boolean.class, Object.class, Object[].class),
                    $nasgenmap$, (Specialization[]) null);
            Prototype prototype = new Prototype();
            PrototypeObject.setConstructor(prototype, this);
            setPrototype(prototype);
            setArity(1);
        }
    }

    /**
     * @param name the name of the method
     * @param rtype  the return type
     * @param ptypes the parameter types
     * @throws IllegalAccessException 
     * @throws NoSuchMethodException 
     */
    private static MethodHandle staticHandle(String name, Class<?> rtype, Class<?>... ptypes) {
        try {
            return MethodHandles.lookup().findStatic(NativeFunction.class,
                    name, MethodType.methodType(rtype, ptypes));
        }
        catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static final class Prototype extends PrototypeObject {
        private ScriptFunction toString = ScriptFunctionImpl.makeFunction("toString", 
                staticHandle("toString", String.class, Object.class));
        private ScriptFunction apply = ScriptFunctionImpl.makeFunction("apply",
                staticHandle("apply", Object.class, Object.class, Object.class, Object.class));
        private ScriptFunction call = ScriptFunctionImpl.makeFunction("call",
                staticHandle("call", Object.class, Object.class, Object[].class));
        private ScriptFunction bind = ScriptFunctionImpl.makeFunction("bind",
                staticHandle("bind", Object.class, Object.class, Object[].class));
        private ScriptFunction toSource = ScriptFunctionImpl.makeFunction("toSource",
                staticHandle("toSource", String.class, Object.class));

        private static final PropertyMap $nasgenmap$;

        public ScriptFunction G$toString() {
            return this.toString;
        }

        public void S$toString(final ScriptFunction function) {
            this.toString = function;
        }

        public ScriptFunction G$apply() {
            return this.apply;
        }

        public void S$apply(final ScriptFunction function) {
            this.apply = function;
        }

        public ScriptFunction G$call() {
            return this.call;
        }

        public void S$call(final ScriptFunction function) {
            this.call = function;
        }

        public ScriptFunction G$bind() {
            return this.bind;
        }

        public void S$bind(ScriptFunction function) {
            this.bind = function;
        }

        public ScriptFunction G$toSource() {
            return this.toSource;
        }

        public void S$toSource(final ScriptFunction function) {
            this.toSource = function;
        }

        static {
            ArrayList<Property> list = new ArrayList<>(6);
            list.add(AccessorProperty.create("toString", Property.NOT_ENUMERABLE,
                    virtualHandle("G$toString", ScriptFunction.class),
                    virtualHandle("S$toString", void.class, ScriptFunction.class)));
            list.add(AccessorProperty.create("apply", Property.NOT_ENUMERABLE,
                    virtualHandle("G$apply", ScriptFunction.class),
                    virtualHandle("S$apply", void.class, ScriptFunction.class)));
            list.add(AccessorProperty.create("call", Property.NOT_ENUMERABLE,
                    virtualHandle("G$call", ScriptFunction.class),
                    virtualHandle("S$call", void.class, ScriptFunction.class)));
            list.add(AccessorProperty.create("bind", Property.NOT_ENUMERABLE,
                    virtualHandle("G$bind", ScriptFunction.class),
                    virtualHandle("S$bind", void.class, ScriptFunction.class)));
            list.add(AccessorProperty.create("toSource", Property.NOT_ENUMERABLE,
                    virtualHandle("G$toSource", ScriptFunction.class),
                    virtualHandle("S$toSource", void.class, ScriptFunction.class)));
            $nasgenmap$ = PropertyMap.newMap(list);
        }

        Prototype() {
            super($nasgenmap$);
            call.setArity(1);
            bind.setArity(1);
        }

        public String getClassName() {
            return "Function";
        }

        /**
         * @param name the name of the method
         * @param rtype  the return type
         * @param ptypes the parameter types
         */
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
