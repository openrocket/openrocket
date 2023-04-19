/*
 * This is a replacement for the ScriptEngineManager which gets around and issue with the sun.misc.ServiceConfigurationError
 * which has been removed in Java 9+.  If using the ScriptEngineManager from the script-api*.jar then the sun.misc throws
 * a ClassNotFoundException.
 */

/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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

package net.sf.openrocket.scripting;

import javax.script.*;
import java.util.*;
import java.security.*;
import java.util.ServiceLoader;
import java.util.ServiceConfigurationError;

/**
 * The <code>ScriptEngineManager</code> implements a discovery and instantiation
 * mechanism for <code>ScriptEngine</code> classes and also maintains a
 * collection of key/value pairs storing state shared by all engines created
 * by the Manager. This class uses the service provider mechanism described in the
 * {@link java.util.ServiceLoader} class to enumerate all the
 * implementations of <code>ScriptEngineFactory</code>. <br><br>
 * The <code>ScriptEngineManager</code> provides a method to return a list of all these factories
 * as well as utility methods which look up factories on the basis of language name, file extension
 * and mime type.
 * <p>
 * The <code>Bindings</code> of key/value pairs, referred to as the "Global Scope"  maintained
 * by the manager is available to all instances of <code>ScriptEngine</code> created
 * by the <code>ScriptEngineManager</code>.  The values in the <code>Bindings</code> are
 * generally exposed in all scripts.
 *
 * @author Mike Grogan
 * @author A. Sundararajan
 * @since 1.6
 */
public class ScriptEngineManagerRedux  {
    private static final boolean DEBUG = false;
    /**
     * The effect of calling this constructor is the same as calling
     * <code>ScriptEngineManager(Thread.currentThread().getContextClassLoader())</code>.
     *
     * @see java.lang.Thread#getContextClassLoader
     */
    public ScriptEngineManagerRedux() {
        init(Thread.currentThread().getContextClassLoader());
    }

    /**
     * This constructor loads the implementations of
     * <code>ScriptEngineFactory</code> visible to the given
     * <code>ClassLoader</code> using the service provider mechanism.<br><br>
     * If loader is <code>null</code>, the script engine factories that are
     * bundled with the platform are loaded. <br>
     *
     * @param loader ClassLoader used to discover script engine factories.
     */
    public ScriptEngineManagerRedux(ClassLoader loader) {
        init(loader);
    }

    /**
     * Gets the value for the specified key in the Global Scope
     * @param key The key whose value is to be returned.
     * @return The value for the specified key.
     */
    public Object get(String key) {
        return _globalScope.get(key);
    }

    /**
     * <code>getBindings</code> returns the value of the <code>globalScope</code> field.
     * ScriptEngineManager sets this <code>Bindings</code> as global bindings for
     * <code>ScriptEngine</code> objects created by it.
     *
     * @return The globalScope field.
     */
    public Bindings getBindings() {
        return _globalScope;
    }

    /**
     * Looks up and creates a <code>ScriptEngine</code> for a given  name.
     * The algorithm first searches for a <code>ScriptEngineFactory</code> that has been
     * registered as a handler for the specified name using the <code>registerEngineName</code>
     * method.
     * <br><br> If one is not found, it searches the set of <code>ScriptEngineFactory</code> instances
     * stored by the constructor for one with the specified name.  If a <code>ScriptEngineFactory</code>
     * is found by either method, it is used to create instance of <code>ScriptEngine</code>.
     * @param shortName The short name of the <code>ScriptEngine</code> implementation.
     * returned by the <code>getNames</code> method of its <code>ScriptEngineFactory</code>.
     * @return A <code>ScriptEngine</code> created by the factory located in the search.  Returns null
     * if no such factory was found.  The <code>ScriptEngineManager</code> sets its own <code>globalScope</code>
     * <code>Bindings</code> as the <code>GLOBAL_SCOPE</code> <code>Bindings</code> of the newly
     * created <code>ScriptEngine</code>.
     * @throws NullPointerException if shortName is null.
     */
    private Map<String, ScriptEngineFactory> _factoriesByName = new HashMap<>();
    public synchronized ScriptEngine getEngineByName(String shortName) {
        if (shortName == null) {
            throw new NullPointerException();
        }

        String key = shortName.toLowerCase();
        if (_factoriesByName.containsKey(key)) {
            return getEngineByFactory(_factoriesByName.get(key));
        }

        // Look for registered name first
        ScriptEngineFactory factoryNamed;
        if (null != (factoryNamed = _nameAssociations.get(key))) {
            try {
                _factoriesByName.put(key, factoryNamed);
                return getEngineByFactory(factoryNamed);
            } catch (Exception exp) {
                if (DEBUG) {
                    exp.printStackTrace();
                }
            }
        }

        Optional<String> factoryName;
        List<String> names;
        for (ScriptEngineFactory factory : _scriptEngineFactories) {
            try {
                factoryName = factory.getNames().stream().filter(l -> l.equalsIgnoreCase(shortName)).findFirst();
                if (factoryName.isPresent()) {
                    _factoriesByName.put(key, factory);
                   return getEngineByFactory(factory);
                }
            } catch (Exception exp) {
                if (DEBUG) {
                    exp.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * Returns a list whose elements are instances of all the <code>ScriptEngineFactory</code> classes
     * found by the discovery mechanism.
     * @return List of all discovered <code>ScriptEngineFactory</code>s.
     */
    public synchronized List<ScriptEngineFactory> getEngineFactories() {
        return List.copyOf(_scriptEngineFactories);
    }

    /**
     * Sets the specified key/value pair in the Global Scope.
     * @param key Key to set
     * @param value Value to set.
     * @throws NullPointerException if key is null.
     * @throws IllegalArgumentException if key is empty string.
     */
    public void put(String key, Object value) {
        _globalScope.put(key, value);
    }

    /**
     * Registers a <code>ScriptEngineFactory</code> to handle a language
     * name.  Overrides any such association found using the Discovery mechanism.
     * @param name The name to be associated with the <code>ScriptEngineFactory</code>.
     * @param factory The class to associate with the given name.
     * @throws NullPointerException if any of the parameters is null.
     */
    public void registerEngineName(String name, ScriptEngineFactory factory) {
        if (name == null || factory == null) {
            throw new NullPointerException();
        }

        _nameAssociations.put(name.toLowerCase(), factory);
    }

    /**
     * <code>setBindings</code> stores the specified <code>Bindings</code>
     * in the <code>globalScope</code> field. ScriptEngineManager sets this
     * <code>Bindings</code> as global bindings for <code>ScriptEngine</code>
     * objects created by it.
     *
     * @param bindings The specified <code>Bindings</code>
     * @throws IllegalArgumentException if bindings is null.
     */
    public void setBindings(Bindings bindings) {
        if (bindings == null) {
            throw new IllegalArgumentException("Global scope cannot be null.");
        }

        _globalScope = bindings;
    }

    private ScriptEngine getEngineByFactory(ScriptEngineFactory factory) {
        if (factory == null) {
            return null;
        }

        ScriptEngine engine = factory.getScriptEngine();
        engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
        return engine;
    }

    private ServiceLoader<ScriptEngineFactory> getServiceLoader(final ClassLoader loader) {
        if (loader != null) {
            return ServiceLoader.load(ScriptEngineFactory.class, loader);
        } else {
            return ServiceLoader.loadInstalled(ScriptEngineFactory.class);
        }
    }

    private void init(final ClassLoader loader) {
        _scriptEngineFactories = new TreeSet<>(Comparator.comparing(
                ScriptEngineFactory::getEngineName,
                Comparator.nullsLast(Comparator.naturalOrder()))
        );
        initEngines(loader);
    }

    private void initEngines(final ClassLoader loader) {
        Iterator<ScriptEngineFactory> itr;
        try {
            ServiceLoader<ScriptEngineFactory> loaders = AccessController.doPrivileged(
                    new PrivilegedAction<ServiceLoader<ScriptEngineFactory>>() {
                        @Override
                        public ServiceLoader<ScriptEngineFactory> run() {
                            return getServiceLoader(loader);
                        }
                    });

            itr = loaders.iterator();
        } catch (ServiceConfigurationError err) {
            // } catch (Exception err) {
            System.err.println("Can't find ScriptEngineFactory providers: " + err.getMessage());
            if (DEBUG) {
                err.printStackTrace();
            }
            // do not throw any exception here. user may want to
            // manage his/her own factories using this manager
            // by explicit registration (by registerXXX) methods.
            return;
        }

        try {
            while (itr.hasNext()) {
                try {
                    ScriptEngineFactory factory = itr.next();
                    _scriptEngineFactories.add(factory);
                } catch (ServiceConfigurationError err) {
                    //   } catch (Exception err) {
                    System.err.println("ScriptEngineManager providers.next(): " + err.getMessage());
                    if (DEBUG) {
                        err.printStackTrace();
                    }
                    // one factory failed, but check other factories...
                }
            }
        } catch (ServiceConfigurationError err) {
            //    } catch (Exception err) {
            System.err.println("ScriptEngineManager providers.hasNext(): " + err.getMessage());
            if (DEBUG) {
                err.printStackTrace();
            }
            // do not throw any exception here. user may want to
            // manage his/her own factories using this manager
            // by explicit registratation (by registerXXX) methods.
        }
    }

    /** Set of script engine factories discovered. */
    private TreeSet<ScriptEngineFactory> _scriptEngineFactories;

    /** Map of engine name to script engine factory. */
    private HashMap<String, ScriptEngineFactory> _nameAssociations = new HashMap<>();

    /** Global bindings associated with script engines created by this manager. */
    private Bindings _globalScope = new SimpleBindings();
}
