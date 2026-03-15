*****
Icons
*****

.. contents:: Table of Contents
   :depth: 2
   :local:
   :backlinks: none

----

This guide explains how to add new icons to OpenRocket and how the icon theming system works.

Icon System Overview
====================

OpenRocket uses a dynamic icon theming system that allows icons to automatically adapt their colors when the user
switches themes (Light, Dark, Dark Contrast). The system is built on top of FlatLaf's ``FlatSVGIcon`` and uses
color filters to map placeholder colors in SVG files to theme-specific colors defined in property files.

Key files:

* :file:`swing/src/main/java/info/openrocket/swing/gui/util/Icons.java` - Icon definitions and loading logic
* :file:`swing/src/main/resources/themes/FlatLightLaf.properties` - Light theme colors
* :file:`swing/src/main/resources/themes/FlatDarculaLaf.properties` - Dark theme colors
* :file:`swing/src/main/resources/themes/FlatOneDarkIJTheme.properties` - Dark Contrast theme colors

Adding a New SVG Icon
=====================

Step 1: Download the SVG
------------------------

We primarily use `Lucide Icons <https://lucide.dev/icons>`_ for SVG icons. To download an icon:

1. Search for the desired icon on https://lucide.dev/icons
2. Configure the export settings:

   * **Color:** ``#000000`` (black - this is the default theme color placeholder)
   * **Stroke width:** ``1.25px``
   * **Size:** ``16px``
   * **Absolute stroke width:** Disabled

3. Download the SVG file
4. Place the SVG in :file:`swing/src/main/resources/pix/icons/lucide/`

.. note::

   Verify that the SVG uses ``stroke="#000000"`` for strokes you want themed. Sometimes the export doesn't
   work correctly and you may need to manually edit the SVG file.

Step 2: Register the Icon
-------------------------

Add the icon definition to :file:`Icons.java`. There are several methods available depending on your needs:

**Simple monochrome icon (most common):**

.. code-block:: java

   public static final Icon MY_ICON = loadIcon(
           "pix/icons/lucide/my-icon.svg",
           "pix/icons/my-icon-fallback.png",  // PNG fallback (optional, can be same as SVG path)
           "My Icon Description");

This uses the default color (``OR.icons.default``) which is black in light theme and white in dark theme.

**Icon with custom theme color:**

.. code-block:: java

   public static final Icon MY_ICON = loadIcon(
           "pix/icons/lucide/my-icon.svg",
           "pix/icons/my-icon-fallback.png",
           "My Icon Description",
           "OR.icons.myColor");  // UIManager color key

**Icon with multiple colors (see Multi-Color Icons):**

.. code-block:: java

   public static final Icon MY_ICON = loadIcon(
           "pix/icons/lucide/my-icon.svg",
           "pix/icons/my-icon-fallback.png",
           "My Icon Description",
           Map.of(
                   SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
                   0x499C54, "OR.icons.plus"
           ));

**SVG-only icon (no PNG fallback):**

.. code-block:: java

   public static final Icon MY_ICON = loadSvgIcon(
           "pix/icons/lucide/my-icon.svg",
           "My Icon Description");

**Scaled icon (for sub-icons or smaller variations):**

.. code-block:: java

   public static final Icon MY_SMALL_ICON = loadIcon(
           "pix/icons/lucide/my-icon.svg",
           "pix/icons/my-icon-fallback.png",
           "My Small Icon",
           "OR.icons.myColor",
           0.75);  // 75% of normal size

How the Color Filter Works
==========================

The icon color filter is what makes icons adapt to theme changes dynamically. Here's how it works:

Color Mapping Concept
---------------------

When you load an SVG icon with color mappings, you're telling the system:

   *"When you encounter this specific RGB color in the SVG, replace it with the color from this UIManager key."*

The mapping is defined as:

.. code-block:: java

   Map.of(
       0x000000, "OR.icons.default",    // Black in SVG -> OR.icons.default color
       0x499C54, "OR.icons.plus"        // Green (#499C54) in SVG -> OR.icons.plus color
   )

* **First value (hex integer):** The RGB color value stored in the SVG file (e.g., ``0x000000`` for black)
* **Second value (String):** The UIManager color key from the theme property files

Dynamic Resolution
------------------

The color filter resolves colors **at paint time**, not at icon creation time. This means:

* When the theme changes, ``UIManager`` colors are updated
* The next time the icon paints, it queries ``UIManager`` for the current color
* Icons automatically reflect the new theme without needing to be recreated

This is implemented using a mapper function in ``createSvgColorFilter()``:

.. code-block:: java

   Function<Color, Color> colorMapper = originalColor -> {
       int rgb = originalColor.getRGB() & 0x00ffffff;
       String colorKey = rgbToKeyMap.get(rgb);
       if (colorKey == null) {
           return originalColor;  // No mapping, keep original
       }
       Color themedColor = UIManager.getColor(colorKey);
       // ... fallback logic ...
       return themedColor;
   };

Multi-Color SVG Icons
=====================

For icons with multiple distinct colors (e.g., a file icon with a green plus sign), you can map each color
to a different theme key.

Creating a Multi-Color SVG
--------------------------

1. Design your SVG with specific placeholder colors for each element:

   * Use ``#000000`` (black) for the main icon shape (maps to ``OR.icons.default``)
   * Use other colors like ``#499C54`` (green), ``#2D2DBD`` (blue), ``#FAC132`` (yellow) for accents

2. Register the icon with a color map:

.. code-block:: java

   public static final Icon FILE_NEW = loadIcon(
           "pix/icons/lucide/file-plus-corner.svg",
           "pix/icons/document-new.png",
           "New document",
           Map.of(
                   SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,  // 0x000000 -> OR.icons.default
                   0x499C54, "OR.icons.plus"                     // Green accent -> OR.icons.plus
           ));

Common Color Placeholders
-------------------------

When creating multi-color SVGs, use these standardized placeholder colors:

.. list-table::
   :header-rows: 1
   :widths: 20 20 60

   * - Hex Color
     - Purpose
     - Typical Theme Key
   * - ``#000000``
     - Main icon color
     - ``OR.icons.default``
   * - ``#499C54``
     - Green accents (plus, add, success)
     - ``OR.icons.plus``, ``OR.icons.export``
   * - ``#2D2DBD``
     - Blue accents (info, tint)
     - ``OR.icons.tintColor``, ``OR.icons.redo``
   * - ``#FAC132``
     - Yellow accents (edit, pencil)
     - ``OR.icons.pencil``
   * - ``#C80000``
     - Red accents (delete, remove, error)
     - ``OR.icons.delete``, ``OR.icons.import``
   * - ``#F1C066``
     - Gold accents (lock, favorite)
     - ``OR.icons.locked``

Theme Property Files
====================

Icon colors are defined in the theme property files under :file:`swing/src/main/resources/themes/`.

Adding a New Icon Color
-----------------------

1. Define the color in **all three** theme files:

**FlatLightLaf.properties** (Light theme):

.. code-block:: properties

   OR.icons.myNewColor = #2D7FF9

**FlatDarculaLaf.properties** (Dark theme):

.. code-block:: properties

   OR.icons.myNewColor = #6EA8FE

**FlatOneDarkIJTheme.properties** (Dark Contrast theme):

.. code-block:: properties

   OR.icons.myNewColor = #7EAAFF

2. Use the key in your icon definition:

.. code-block:: java

   public static final Icon MY_ICON = loadIcon(
           "pix/icons/lucide/my-icon.svg",
           "pix/icons/my-icon.png",
           "My Icon",
           "OR.icons.myNewColor");

Available Icon Color Keys
-------------------------

The following ``OR.icons.*`` keys are predefined:

.. list-table::
   :header-rows: 1
   :widths: 30 70

   * - Key
     - Purpose
   * - ``OR.icons.default``
     - Default icon color (black/white depending on theme)
   * - ``OR.icons.tintColor``
     - General accent color (blue)
   * - ``OR.icons.play``
     - Play/run buttons (green)
   * - ``OR.icons.help``
     - Help/info icons (blue)
   * - ``OR.icons.warning.low``
     - Low priority warnings (info color)
   * - ``OR.icons.warning.normal``
     - Normal warnings (warning color)
   * - ``OR.icons.warning.high``
     - Critical warnings (error color)
   * - ``OR.icons.override``
     - Override indicators
   * - ``OR.icons.override.subcomponent``
     - Subcomponent override indicators (dimmed)
   * - ``OR.icons.locked``
     - Lock icon accent (gold)
   * - ``OR.icons.delete``
     - Delete/trash icons (red)
   * - ``OR.icons.plot``
     - Plot/chart icons (blue)
   * - ``OR.icons.plus``
     - Add/plus icons (green)
   * - ``OR.icons.duplicate``
     - Duplicate icons (tint color)
   * - ``OR.icons.pencil``
     - Edit/pencil icons (yellow)
   * - ``OR.icons.zoomIn``
     - Zoom in (green)
   * - ``OR.icons.zoomOut``
     - Zoom out (red)
   * - ``OR.icons.zoomFit``
     - Zoom to fit (tint color)
   * - ``OR.icons.import``
     - Import icons (red)
   * - ``OR.icons.export``
     - Export icons (green)
   * - ``OR.icons.moveUp``
     - Move up arrows (blue)
   * - ``OR.icons.moveDown``
     - Move down arrows (purple)
   * - ``OR.icons.undo``
     - Undo icons (red)
   * - ``OR.icons.redo``
     - Redo icons (blue)

You can also reference other color keys using FlatLaf's ``$`` syntax:

.. code-block:: properties

   OR.icons.myColor = $OR.colors.blue

macOS Menu Icons
================

On macOS, menu bar icons have special handling to match the native appearance. The ``MACOS_MENU_COLOR_FILTER``
converts **all** icon colors to monochrome (white for dark system theme, black for light system theme).

When using an icon in a menu, wrap it with ``deriveMenuIcon()``:

.. code-block:: java

   JMenuItem item = new JMenuItem("My Action");
   item.setIcon(Icons.deriveMenuIcon(Icons.MY_ICON));

This ensures the icon displays correctly in native macOS menus regardless of how many colors the original SVG has.

Best Practices
==============

1. **Always use black (``#000000``) as the primary color** - This is the default theme color and ensures
   icons work correctly even without explicit color mappings.

2. **Keep SVGs simple** - Use stroke-based icons with minimal complexity for best scaling and theming.

3. **Test in all themes** - After adding an icon, switch between Light, Dark, and Dark Contrast themes to
   verify colors look correct.

4. **Maintain consistency** - Use the predefined ``OR.icons.*`` color keys when possible to maintain visual
   consistency across the application.

5. **Update all theme files** - When adding a new color key, define it in all three theme property files.

6. **Provide PNG fallbacks** - While SVG is preferred, provide a PNG fallback for compatibility.

7. **Use appropriate sizes** - Icons are automatically scaled based on font size. Use the ``scaleMultiplier``
   parameter only when you need an icon that's intentionally smaller or larger than the standard size.

Troubleshooting
===============

Icon colors don't update when switching themes
----------------------------------------------

Ensure you're using the ``Map.of()`` syntax for color mappings, not hardcoded colors. The color filter must
resolve colors dynamically from ``UIManager``.

Icon appears with wrong colors
------------------------------

1. Verify the hex color in the SVG matches exactly what you specified in the color map
2. Check that the color key exists in all theme property files
3. Open the SVG in a text editor and verify the stroke/fill colors are correct

Icon is too large or too small
------------------------------

Icons are scaled based on the user's font size setting. If you need a specifically sized icon, use the
``scaleMultiplier`` parameter:

.. code-block:: java

   loadIcon(svgFile, pngFile, name, colorKey, 0.75);  // 75% of normal size

SVG stroke colors not theming correctly
---------------------------------------

Make sure the SVG uses ``stroke="#000000"`` (or your placeholder color) and not ``stroke="black"`` or
other color formats. The color filter matches exact RGB values.

