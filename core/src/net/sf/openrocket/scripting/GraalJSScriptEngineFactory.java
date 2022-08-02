package net.sf.openrocket.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.*;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;

public class GraalJSScriptEngineFactory implements ScriptEngineFactory {
    private static final String ENGINE_NAME = "Graal.js";
    private static final String LANGUAGE = "ECMAScript";
    private static final String LANGUAGE_VERSION = "ECMAScript 262 Edition 11";
    private static final String[] NAMES = new String[]{"js", "JS", "JavaScript", "javascript", LANGUAGE, LANGUAGE.toLowerCase(), ENGINE_NAME, ENGINE_NAME.toLowerCase(), "Graal-js", "graal-js", "Graal.JS", "Graal-JS", "GraalJS", "GraalJSPolyglot"};
    private static final String[] MIME_TYPES = new String[]{"application/javascript", "application/ecmascript", "text/javascript", "text/ecmascript"};
    private static final String[] EXTENSIONS = new String[]{"js", "mjs"};
    private static final List names;
    private static final List mimeTypes;
    private static final List extensions;

    public GraalJSScriptEngineFactory() {
    }

    public ScriptEngine getScriptEngine() {
        // https://github.com/oracle/graaljs/blob/master/docs/user/RunOnJDK.md
        // https://github.com/oracle/graaljs/blob/master/docs/user/ScriptEngine.md#setting-options-via-bindings
        return GraalJSScriptEngine.create(null,
                Context.newBuilder("js")
                        .allowHostAccess(HostAccess.ALL)
                        .allowHostClassLookup(s -> true)
                        .option("js.ecmascript-version", "2021"));
    }

    public String getEngineName() {
        return ENGINE_NAME;
    }

    public String getEngineVersion() {
        return ((GraalJSScriptEngine)getScriptEngine()).getPolyglotEngine().getVersion();
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public String getLanguageVersion() {
        return LANGUAGE_VERSION;
    }

    public String getLanguageName() {
        return LANGUAGE;
    }

    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    public List<String> getNames() {
        return names;
    }

    public String getMethodCallSyntax(final String obj, final String method, final String... args) {
        return null;
    }

    public String getOutputStatement(final String toDisplay) {
        return "print(" + toDisplay + ")";
    }

    public Object getParameter(String key) {
        switch (key) {
            case "javax.script.name":
                return "javascript";
            case "javax.script.engine":
                return this.getEngineName();
            case "javax.script.engine_version":
                return this.getEngineVersion();
            case "javax.script.language":
                return this.getLanguageName();
            case "javax.script.language_version":
                return this.getLanguageVersion();
            default:
                return null;
        }
    }

    public String getProgram(final String... statements) {
       return null;
    }

    static {
        List<String> nameList = Arrays.asList(NAMES);
        List<String> mimeTypeList = Arrays.asList(MIME_TYPES);
        List<String> extensionList = Arrays.asList(EXTENSIONS);
        names = Collections.unmodifiableList(nameList);
        mimeTypes = Collections.unmodifiableList(mimeTypeList);
        extensions = Collections.unmodifiableList(extensionList);
    }
}
