Using OpenRocket Core in External Applications
==============================================

This guide explains how to use the OpenRocket core module in your own Java applications without the Swing GUI dependencies.

Overview
--------

The OpenRocket core module provides the fundamental rocket simulation functionality, including:

- Rocket design and component models
- Flight simulation engine
- Motor and component databases
- Internationalization (i18n) support
- Preferences management

Maven/Gradle Dependencies
-------------------------

To use OpenRocket core in your project, add it as a dependency:

**Maven:**

.. code-block:: xml

   <dependency>
       <groupId>info.openrocket</groupId>
       <artifactId>openrocket-core</artifactId>
       <version>YOUR_VERSION</version>
   </dependency>

**Gradle:**

.. code-block:: groovy

   implementation 'info.openrocket:openrocket-core:YOUR_VERSION'

Basic Usage
-----------

The simplest way to initialize OpenRocket core is using the ``OpenRocketCore`` helper class:

.. code-block:: java

   import info.openrocket.core.startup.OpenRocketCore;
   import info.openrocket.core.startup.Application;

   public class MyApplication {
       public static void main(String[] args) {
           // Initialize OpenRocket core
           OpenRocketCore.initialize();
           
           // Your application logic here...
       }

      // Example: Load a rocket from a file
      private Rocket loadRocketFromFile(File file) {
         try {
            GeneralRocketLoader loader = new GeneralRocketLoader(file);
            OpenRocketDocument document = loader.load();
            return document.getRocket();
         } catch (Exception e) {
            System.err.println("Failed to load rocket from file: " + e.getMessage());
            return null;
         }
      }

      // Example: Save a rocket to a file
      private void saveRocketToFile(OpenRocketDocument document, File file) {
         try {
               GeneralRocketSaver saver = new GeneralRocketSaver(file);
               saver.save(rocket);
         } catch (Exception e) {
               System.err.println("Failed to save rocket to file: " + e.getMessage());
         }
      }

      private OpenRocketDocument createNewRocket() {
         // --- ROCKET ---
         OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
         Rocket rocket = document.getRocket();
         AxialStage stage = rocket.getStage(0);

         // --- NOSE CONE ---
         NoseCone noseCone = new NoseCone();
         noseCone.setShapeType(Transition.Shape.OGIVE);
         noseCone.setShapeParameter(1.0);
         noseCone.setLength(0.15);
         noseCone.setBaseRadius(0.025);
         noseCone.setThickness(0.002);

         stage.addChild(noseCone);

         // --- BODY TUBE ---
         BodyTube bodyTube = new BodyTube();
         bodyTube.setLength(0.2);
         bodyTube.setOuterRadius(0.025);
         bodyTube.setThickness(0.002);

         stage.addChild(bodyTube);

         // --- TRANSITION ---
         Transition transition = new Transition();
         transition.setLength(0.1);
         transition.setForeRadius(0.025);
         transition.setAftRadius(0.01);
         transition.setShapeType(Transition.Shape.OGIVE);
         transition.setShapeParameter(1.0);

         stage.addChild(transition);

         // --- FIN SET ---
         TrapezoidFinSet f = new TrapezoidFinSet();
         f.setHeight(0.05);
         f.setRootChord(0.08);
         f.setSweep(0.05);
         finSet.setFinCount(3);
         finSet.setThickness(0.005);
         finSet.setAxialMethod(AxialMethod.BOTTOM);
         finSet.setAxialOffset(0);

         bodyTube.addChild(finSet); // Attach fins to the parent

         return document;
      }
   }

Advanced Usage with Custom Modules
-----------------------------------

For more control over the dependency injection setup, you can initialize OpenRocket with custom Guice modules:

.. code-block:: java

   import info.openrocket.core.startup.OpenRocketCore;
   import info.openrocket.core.plugin.PluginModule;
   import com.google.inject.AbstractModule;

   public class MyApplication {
       public static void main(String[] args) {
           // Create custom module
           AbstractModule customModule = new AbstractModule() {
               @Override
               protected void configure() {
                   // Your custom bindings here
               }
           };
           
           // Initialize with custom modules
           OpenRocketCore.initialize(new PluginModule(), customModule);
           
           // Use OpenRocket functionality...
       }
   }

Manual Initialization (Advanced)
---------------------------------

If you need full control over the initialization process, you can manually set up the dependency injection:

.. code-block:: java

   import info.openrocket.core.startup.Application;
   import info.openrocket.core.startup.CoreModule;
   import info.openrocket.core.plugin.PluginModule;
   import com.google.inject.Guice;
   import com.google.inject.Injector;

   public class MyApplication {
       public static void main(String[] args) {
           // Create the core module
           CoreModule coreModule = new CoreModule();
           
           // Create injector with required modules
           Injector injector = Guice.createInjector(
               coreModule, 
               new PluginModule()
           );
           
           // Set the injector in the Application class
           Application.setInjector(injector);
           
           // Start loading databases
           coreModule.startLoader();
           
           // Now you can use OpenRocket functionality...
       }
   }

Working with Rockets
--------------------

Here's an example of creating and simulating a simple rocket:

.. code-block:: java

   import info.openrocket.core.rocketcomponent.*;
   import info.openrocket.core.simulation.*;
   import info.openrocket.core.startup.OpenRocketCore;

   public class RocketSimulationExample {
       public static void main(String[] args) {
           // Initialize OpenRocket
           OpenRocketCore.initialize();
           
           // Create a simple rocket
           Rocket rocket = new Rocket();
           Stage stage = new Stage();
           rocket.addChild(stage);
           
           // Add nose cone
           NoseCone noseCone = new NoseCone();
           noseCone.setLength(0.1); // 10cm
           stage.addChild(noseCone);
           
           // Add body tube
           BodyTube bodyTube = new BodyTube();
           bodyTube.setLength(0.3); // 30cm
           bodyTube.setOuterRadius(0.025); // 2.5cm radius
           stage.addChild(bodyTube);
           
           // Add motor mount
           InnerTube motorMount = new InnerTube();
           motorMount.setLength(0.07); // 7cm
           motorMount.setOuterRadius(0.012); // 1.2cm radius
           bodyTube.addChild(motorMount);
           
           // Create simulation
           Simulation simulation = new Simulation(rocket);
           
           // Configure simulation options
           SimulationOptions options = simulation.getOptions();
           // Set motor, recovery, etc...
           
           // Run simulation
           // simulation.simulate();
           
           System.out.println("Rocket created successfully!");
       }
   }

Configuration Options
---------------------

You can configure OpenRocket behavior using system properties:

.. code-block:: java

   // Skip loading component presets (faster startup)
   System.setProperty("openrocket.bypass.presets", "true");
   
   // Skip loading motor database (faster startup)
   System.setProperty("openrocket.bypass.motors", "true");
   
   // Set custom locale
   System.setProperty("openrocket.locale", "en_US");
   
   // Enable debug mode
   System.setProperty("openrocket.debug", "true");

Thread Safety
-------------

OpenRocket core components are generally not thread-safe. If you need to use OpenRocket from multiple threads:

1. Initialize OpenRocket from the main thread before creating other threads
2. Use proper synchronization when accessing shared OpenRocket objects
3. Consider creating separate instances for different threads where possible

Best Practices
--------------

1. **Initialize once**: Call ``OpenRocketCore.initialize()`` only once at application startup
2. **Handle exceptions**: Database loading can fail; handle exceptions appropriately
3. **Use dependency injection**: Access services through ``Application.getInjector()`` when possible
4. **Memory management**: OpenRocket loads significant data; monitor memory usage in long-running applications

Troubleshooting
---------------

**"Application.injector is null" error:**
  Make sure you've called ``OpenRocketCore.initialize()`` before using any OpenRocket functionality.

**ClassNotFoundException or NoClassDefFoundError:**
  Ensure all required dependencies are on the classpath. The core module has dependencies on Google Guice and other libraries.

**Database loading issues:**
  Check that component and motor database files are accessible. Use bypass properties for faster startup during development.