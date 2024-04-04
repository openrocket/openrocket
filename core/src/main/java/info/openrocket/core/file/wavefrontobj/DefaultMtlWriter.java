/*
 * www.javagl.de - Obj
 *
 * Copyright (c) 2008-2015 Marco Hutter - http://www.javagl.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package info.openrocket.core.file.wavefrontobj;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.FloatTuples;
import de.javagl.obj.Mtl;
import de.javagl.obj.TextureOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * A class that may write {@link Mtl} objects into an MTL file
 */
public class DefaultMtlWriter {
    /**
     * Write the given {@link Mtl} objects to the given stream. The caller
     * is responsible for closing the stream.
     *
     * @param mtls         The {@link Mtl} objects
     * @param outputStream The stream to write to
     * @throws IOException If an IO error occurs
     */
    public static void write(
            Iterable<? extends Mtl> mtls, OutputStream outputStream)
            throws IOException {
        OutputStreamWriter outputStreamWriter =
                new OutputStreamWriter(outputStream);
        write(mtls, outputStreamWriter);
    }

    /**
     * Write the given {@link Mtl} objects to the given writer. The caller
     * is responsible for closing the writer.
     *
     * @param mtls   The {@link Mtl} objects
     * @param writer The writer to write to
     * @throws IOException If an IO error occurs
     */
    public static void write(
            Iterable<? extends Mtl> mtls, Writer writer)
            throws IOException {
        for (Mtl mtl : mtls) {
            write(mtl, writer);
        }
    }

    /**
     * Write the given {@link Mtl} to the given writer
     *
     * @param mtl    The {@link Mtl}
     * @param writer The writer
     * @throws IOExcept
     *
     * ion If an IO error occurs
     */
    public static void write(Mtl mtl, Writer writer)
            throws IOException {
        writer.write(createString(mtl));
        writer.flush();
    }

    /**
     * Create the string representation of the given {@link Mtl}, as it
     * is written into an MTL file
     *
     * @param mtl The {@link Mtl}
     * @return The string representation
     */
    public static String createString(Mtl mtl) {
        StringBuilder sb = new StringBuilder("newmtl ");
        sb.append(mtl.getName()).append("\n");

        append(sb, "illum", mtl.getIllum(), "\n");
        append(sb, "Ns", mtl.getNs(), "\n");
        append(sb, "Ni", mtl.getNi(), "\n");

        Float opacity = mtl.getD();
        if (opacity != null) {
            sb.append("d").append(" ");
            if (Boolean.TRUE.equals(mtl.isHalo())) {
                sb.append("-halo").append(" ");
            }
            sb.append(opacity);
            sb.append("\n");
        }

        appendTuple(sb, "Ka", mtl.getKa(), "\n");
        appendTuple(sb, "Kd", mtl.getKd(), "\n");
        appendTuple(sb, "Ks", mtl.getKs(), "\n");
        appendTuple(sb, "Tf", mtl.getTf(), "\n");
        append(sb, "sharpness", mtl.getSharpness(), "\n");
        appendTextureOptions(sb, "map_Ka", mtl.getMapKaOptions());
        appendTextureOptions(sb, "map_Kd", mtl.getMapKdOptions());
        appendTextureOptions(sb, "map_Ks", mtl.getMapKsOptions());
        appendTextureOptions(sb, "map_Ns", mtl.getMapNsOptions());
        appendTextureOptions(sb, "map_d", mtl.getMapDOptions());
        appendTextureOptions(sb, "bump", mtl.getBumpOptions());
        appendTextureOptions(sb, "disp", mtl.getDispOptions());
        appendTextureOptions(sb, "decal", mtl.getDecalOptions());
        List<TextureOptions> refls = mtl.getReflOptions();
        for (TextureOptions refl : refls) {
            appendTextureOptions(sb, "refl", refl);
        }

        // PBR parameters
        append(sb, "Pr", mtl.getPr(), "\n");
        appendTextureOptions(sb, "map_Pr", mtl.getMapPrOptions());
        append(sb, "Pm", mtl.getPm(), "\n");
        appendTextureOptions(sb, "map_Pm", mtl.getMapPmOptions());
        append(sb, "Ps", mtl.getPs(), "\n");
        appendTextureOptions(sb, "map_Ps", mtl.getMapPsOptions());
        append(sb, "Pc", mtl.getPc(), "\n");
        append(sb, "Pcr", mtl.getPcr(), "\n");
        appendTuple(sb, "Ke", mtl.getKe(), "\n");
        appendTextureOptions(sb, "map_Ke", mtl.getMapKeOptions());
        append(sb, "aniso", mtl.getAniso(), "\n");
        append(sb, "anisor", mtl.getAnisor(), "\n");
        appendTextureOptions(sb, "norm", mtl.getNormOptions());

        return sb.toString();
    }

    /**
     * Append the given {@link TextureOptions} to the given string builder,
     * if they are not <code>null</code>
     *
     * @param sb      The string builder
     * @param key     The key
     * @param options The {@link TextureOptions}
     */
    private static void appendTextureOptions(
            StringBuilder sb, String key, TextureOptions options) {
        if (options != null) {
            sb.append(key).append(" ");
            sb.append(createString(options)).append("\n");
        }
    }

    /**
     * Create the string representation for the given {@link TextureOptions},
     * as a single line that may be written to the MTL file
     *
     * @param options The {@link TextureOptions}
     * @return The string representation
     */
    static String createString(TextureOptions options) {
        StringBuilder sb = new StringBuilder();
        append(sb, "-blendu", options.isBlendu(), " ");
        append(sb, "-blendv", options.isBlendv(), " ");
        append(sb, "-boost", options.getBoost(), " ");
        appendTuple(sb, "-mm", options.getMm(), " ");
        appendTuple(sb, "-o", options.getO(), " ");
        appendTuple(sb, "-s", options.getS(), " ");
        appendTuple(sb, "-t", options.getT(), " ");
        append(sb, "-texres", options.getTexres(), " ");
        append(sb, "-clamp", options.isClamp(), " ");
        append(sb, "-bm", options.getBm(), " ");
        append(sb, "-imfchan", options.getImfchan(), " ");
        append(sb, "-type", options.getType(), " ");
        sb.append(options.getFileName());
        return sb.toString();
    }

    /**
     * Append the given key-value mapping to the given string builder, if
     * the given value is not <code>null</code>
     *
     * @param sb        The string builder
     * @param key       The key
     * @param value     The value
     * @param separator The separator to append after the value
     */
    private static void append(
            StringBuilder sb, String key, Object value, String separator) {
        if (value != null) {
            sb.append(key).append(" ");
            sb.append(value);
            sb.append(separator);
        }
    }

    /**
     * Append the given key-value mapping to the given string builder, if
     * the given value is not <code>null</code>
     *
     * @param sb        The string builder
     * @param key       The key
     * @param value     The value
     * @param separator The separator to append after the value
     */
    private static void append(
            StringBuilder sb, String key, Boolean value, String separator) {
        if (value != null) {
            sb.append(key).append(" ");
            if (value) {
                sb.append("on");
            } else {
                sb.append("off");
            }
            sb.append(separator);
        }
    }

    /**
     * Append the given key-value mapping to the given string builder, if
     * the given value is not <code>null</code>
     *
     * @param sb        The string builder
     * @param key       The key
     * @param value     The value
     * @param separator The separator to append after the value
     */
    private static void appendTuple(
            StringBuilder sb, String key, FloatTuple value, String separator) {
        if (value != null) {
            sb.append(key).append(" ");
            sb.append(FloatTuples.createString(value));
            sb.append(separator);
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    private DefaultMtlWriter() {
        // Private constructor to prevent instantiation
    }


}
