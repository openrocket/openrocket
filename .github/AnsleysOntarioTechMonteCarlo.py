import sys
import os
import logging
import orhelper as orh
import jpype
import numpy as np
import math
from random import gauss
import matplotlib.pyplot as plt
from jpype.types import *

num=20
# Configure logging
logging.basicConfig(level=logging.DEBUG)

# Set the rocket file path
rocket_file_path = r"E:/Dispersion Simulations/Dispersion Undeleted/Average Wind Speed.ork"  # Use a raw string for Windows paths
if not os.path.exists(rocket_file_path):
    logging.error(f"Rocket file not found at {rocket_file_path}")
    sys.exit(1)

# Ensure JAVA_HOME is correctly set
os.environ['JAVA_HOME'] = r"C:/Program Files/Java/jdk1.8.0_202"

try:
    with orh.OpenRocketInstance() as instance:
        import java.awt
        logging.info("OpenRocket instance started.")

        # Initialize the helper
        orh_helper = orh.Helper(instance)

        # Try loading the rocket file
        try:
            logging.info("Loading rocket file...")
            doc = orh_helper.load_doc(rocket_file_path)
            logging.info("Rocket file loaded successfully.")
        except Exception as e:
            logging.error(f"Failed to load rocket file: {e}")
            raise

        # Perform operations with the loaded document
        try:
            sim = doc.getSimulation(0)
            opts = sim.getOptions()
            rocket = opts.getRocket()
            logging.info("Rocket and simulation loaded successfully.")

            if __name__ == '__main__':
                def list_simulations_and_motor_configs(doc):
                    num_sims = doc.getSimulationCount()
                    print(f"Total Simulations: {num_sims}")

                    for i in range(num_sims):
                        sim = doc.getSimulation(i)
                        motor_config = sim.getOptions().getMotorConfigurationID()
                        print(f"Simulation {i}: Motor Configuration = {motor_config}")

               # Call this function after loading the document
                list_simulations_and_motor_configs(doc)

                def get_motor_configuration_name(simulation):
                    config = simulation.getOptions().getMotorConfigurationID()  # Get motor configuration ID
                    return config if config else "No motor configuration set"

                    for stage_index, stage in enumerate(stages):
                        motors = stage.getMotors()
                        motor_counts = {}

                        # Count occurrences of each motor type
                        for motor in motors:
                            motor_designation = motor.getMotorDesignation()
                            if motor_designation in motor_counts:
                                motor_counts[motor_designation] += 1
                            else:
                                motor_counts[motor_designation] = 1

                        # Build stage motor name
                        stage_name = " + ".join(
                            [f"{count}x{motor}" if count > 1 else motor for motor, count in motor_counts.items()]
                        )

                        config_names.append(stage_name)

                    return " + ".join(config_names)

                # Open OpenRocket and load a .ork file
                sim = doc.getSimulation(0)  # Get first simulation

                motor_config_name = get_motor_configuration_name(sim)
                print("Motor Configuration:", motor_config_name)

                # Example simulation listener
                class LandingPoints(orh.AbstractSimulationListener):
                    def __init__(self):
                        self.ranges = []
                        self.bearings = []

                    def startSimulation(self, status):
                        position = status.getRocketPosition()
                        self.ranges.append(position.getRange())
                        self.bearings.append(position.getBearing())

                    def print_stats(self):
                        if not self.ranges or not self.bearings:
                            print("No landing points to analyze.")
                            return
                             
                        print(f'Rocket landing zone: {np.mean(self.ranges):.2f} m ± {np.std(self.ranges):.2f} m bearing '
                              f'{np.degrees(np.mean(self.bearings)):.2f} deg ± {np.degrees(np.std(self.bearings)):.4f} deg '
                              f'from launch site. Based on {len(self.ranges)} simulations.')

                    def plot_landing_zones(self):
                        if not self.ranges or not self.bearings:
                            print("No data to plot.")
                            return

                        plt.figure(figsize=(10, 6))
                        plt.scatter(self.ranges, np.degrees(self.bearings), c='blue', marker='o')
                        plt.title('Landing Zones')
                        plt.xlabel('Range (m)')
                        plt.ylabel('Bearing (degrees)')
                        plt.grid(True)
                        plt.show()

                # Randomize various parameters
            opts = sim.getOptions()
            rocket = opts.getRocket()

            # Run num simulations and add to self
            for p in range(num):
                print('Running simulation ', p)

                opts.setLaunchRodAngle(math.radians(gauss(45, 5)))  # 45 +- 5 deg in direction
                opts.setLaunchRodDirection(math.radians(gauss(0, 5)))  # 0 +- 5 deg in direction
                opts.setWindSpeedAverage(gauss(15, 5))  # 15 +- 5 m/s in wind
                #for component_name in ('Nose cone', 'Body tube'):  # 5% in the mass of various components
                    #component = shape
                    #mass = component.getMass()
                    #component.setMassOverridden(True)
                    #component.setOverrideMass(mass * gauss(1.0, 0.05))

                #airstarter = AirStart(gauss(1000, 50))  # simulation listener to drop from 1000 m +- 50
                #lp = LandingPoint(self.ranges, self.bearings)
                #orh.run_simulation(sim, listeners=(airstarter, lp))
                #self.append(lp)

            def print_stats(self):
                print(
                    'Rocket landing zone %3.2f m +- %3.2f m bearing %3.2f deg +- %3.4f deg from launch site. Based on %i simulations.' % \
                    (np.mean(self.ranges), np.std(self.ranges), np.degrees(np.mean(self.bearings)),
                     np.degrees(np.std(self.bearings)), len(self)))


            class LandingPoint(orh.AbstractSimulationListener):
                def __init__(self, ranges, bearings):
                    self.ranges = ranges
                    self.bearings = bearings

                def endSimulation(self, status, simulation_exception):
                    worldpos = status.getRocketWorldPosition()
                    conditions = status.getSimulationConditions()
                    launchpos = conditions.getLaunchSite()
                    geodetic_computation = conditions.getGeodeticComputation()

                    if geodetic_computation != geodetic_computation.FLAT:
                        raise Exception("GeodeticComputationStrategy type not supported")

                    self.ranges.append(range_flat(launchpos, worldpos))
                    self.bearings.append(bearing_flat(launchpos, worldpos))


            class AirStart(orh.AbstractSimulationListener):

                def __init__(self, altitude):
                    self.start_altitude = altitude

                def startSimulation(self, status):
                    position = status.getRocketPosition()
                    position = position.add(0.0, 0.0, self.start_altitude)
                    status.setRocketPosition(position)


            METERS_PER_DEGREE_LATITUDE = 111325
            METERS_PER_DEGREE_LONGITUDE_EQUATOR = 111050


            def range_flat(start, end):
                dy = (end.getLatitudeDeg() - start.getLatitudeDeg()) * METERS_PER_DEGREE_LATITUDE
                dx = (end.getLongitudeDeg() - start.getLongitudeDeg()) * METERS_PER_DEGREE_LONGITUDE_EQUATOR
                return math.sqrt(dy * dy + dx * dx)


            def bearing_flat(start, end):
                dy = (end.getLatitudeDeg() - start.getLatitudeDeg()) * METERS_PER_DEGREE_LATITUDE
                dx = (end.getLongitudeDeg() - start.getLongitudeDeg()) * METERS_PER_DEGREE_LONGITUDE_EQUATOR
                return math.pi / 2 - math.atan(dy / dx)


            if __name__ == '__main__':
                points = LandingPoints()
                #points.add_simulations(20)
                points.print_stats()

        except Exception as e:
            logging.error(f"An error occurred: {e}", exc_info=True)

        finally:
            if jpype.isJVMStarted():
                logging.info("JVM is still running.")
 
except Exception as e:
    logging.error(f"Error processing rocket file: {e}")
    raise
