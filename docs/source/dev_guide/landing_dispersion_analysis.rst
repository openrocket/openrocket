Monte Carlo flight and landing-dispersion analysis
===================================================

Purpose
-------

The Monte Carlo analysis estimates the spread in flight metrics and the horizontal
landing cloud produced by uncertain weather, launcher, vehicle, propulsion, and
recovery inputs.  It is an ensemble analysis, not a replacement for validating
the nominal rocket model.  Every run clones the selected simulation, samples
deviations around its nominal values, records scalar outputs for every flight-data
branch, and records the east/north position of eligible bodies at their ground-hit
events.  The source simulation and document are not changed.

Research basis
--------------

The initial parameter set is based on the recurring uncertainty families in
launch-vehicle and parachute-dispersion guidance:

.. list-table:: Parameter families normally considered
   :header-rows: 1
   :widths: 18 35 47

   * - Family
     - Typical inputs
     - OpenRocket implementation status
   * - Atmosphere
     - Wind speed/direction profiles and turbulence; density, temperature, and
       pressure; forecast or historical-profile variability
     - Wind speed, direction, and relative density are implemented.  Wind
       turbulence still comes from the selected OpenRocket wind model.
   * - Launcher and initial state
     - Guide elevation/azimuth alignment, position/attitude, release conditions,
       rail effects, and launch timing
     - Guide tilt and azimuth error are implemented.
   * - Mass properties
     - Total/stage mass, center of gravity, moments of inertia, payload and
       assembly tolerances
     - Total mass/inertia scaling plus an axial CG offset are implemented.
       Independent radial CG offsets are future work.
   * - Aerodynamics and construction
     - Axial drag, normal force, center of pressure, aerodynamic moments and
       damping, fin alignment/cant, and model-form error
     - Axial-force and grouped normal-force/moment scaling are implemented.
       Explicit fin/build-error models are future work.
   * - Propulsion and staging
     - Thrust/total impulse/burn time, motor-to-motor variation, thrust
       misalignment, ignition/ejection timing, and staging timing
     - Scalar thrust and ignition-time variation are implemented.  Thrust-vector
       misalignment and curve-shape variation are future work.
   * - Recovery
     - Deployment detection/timing, inflation/fill, drag area, reefing, device
       correlations, and discrete failure modes
     - Deployment timing and a post-deployment drag multiplier are implemented.
       Per-device inflation dynamics and failure modes are future work.
   * - Guidance and control
     - Sensor, navigation, actuator, and control-law errors for guided vehicles
     - Not currently modeled by OpenRocket's unguided-flight analysis.

FAA AC 450.117-1 identifies mass, aerodynamic, propulsive, guidance, and
atmospheric input uncertainties and discusses normal, uniform, log-normal, and
empirical distributions.  It notes that 500--1,000 trajectories are typically
adequate for its normal-trajectory use case, while also requiring the analyst to
document distributions and correlations.  This range is a starting point, not a
general convergence guarantee.  NASA GSFC-STD-8009 independently calls out thrust
offset and misalignment, aerodynamic error, uncompensated wind, launcher
misalignment, weight/impulse error, guidance/control error, ignition delay, mass
variability, flight history, a statistically significant number of runs, and
one/two/three-sigma impact ellipses for every impacting body.

Recovery-system uncertainty deserves separate treatment.  NASA's CPAS work fits
drag area, inflation/fill, over-inflation, and timing parameters to normal,
log-normal, or uniform distributions based on test evidence rather than assuming
one generic distribution.  Landing studies also combine vehicle and atmospheric
uncertainties with terrain or hazard maps.  The first OpenRocket implementation
stops at pad-relative landing coordinates; geospatial and terrain-risk overlays
are a later layer.

Calculation design
------------------

``MonteCarloSettings`` stores a run count, signed 32-bit master seed, and the
distribution/spread for each active parameter.  ``MonteCarloSampler`` derives one
independent random stream per parameter, plus a stream for the per-run simulation
seeds, from the master seed over the full parameter enum.  Changing one parameter
therefore leaves every other parameter's samples and every run's simulation seed
unchanged.  Repeating the same model, settings, and master seed repeats the sampled
inputs.  The 32-bit input provides over four
billion reproducible seed choices while keeping the configuration field compact
and consistent with OpenRocket's existing simulation-seed UI.

Normal spread means one standard deviation.  Uniform spread means a symmetric
half-range.  Log-normal spread is one standard deviation of the natural logarithm
of the multiplier; it keeps a multiplier strictly positive and is offered only for
relative parameters, where a symmetric distribution can otherwise reach nonphysical
values in the tails.  Its median is the nominal value, so its mean sits slightly
above nominal.  Parameters are currently sampled independently.  Relative samples
are converted to multipliers and clipped to a minimum of 0.01 so that mass,
density, drag, or thrust cannot become zero or negative.  Wind speed is clipped
at zero.  A wind-speed sample shifts the mean speed at every selected wind level
without changing its configured standard deviation; turbulence intensity therefore
changes as the ratio of that fixed standard deviation to the varied mean.  Launch-guide
angle follows OpenRocket's existing physical limit, and an event shifted earlier than
its triggering event is placed 1 ms after the trigger.  These clips make extreme tails
truncated and must be considered when using wide normal spreads.

``MonteCarloSimulationRunner`` performs one nominal run, then the requested number
of dispersed runs on a thread pool.  Every sample is drawn before any trajectory
starts, making sampled inputs independent of thread scheduling.  Each trajectory
runs on an independent copy of the rocket so concurrent simulations do not share
mutable configuration or aerodynamic caches.  Result order remains run-number
order.  Floating-point trajectories may differ slightly with concurrency even when
their sampled inputs and simulation seeds match.  Each run adds a system simulation
listener.  Post-calculation hooks alter
atmosphere, rigid-body properties, aerodynamic forces, and thrust without editing
rocket components.  The event hook reschedules ignition and recovery deployment.
Launch-option and wind-profile deviations are applied to each clone before the
flight starts.  Simulation extensions run only when they explicitly declare
themselves safe for repeated, concurrent Monte Carlo use.  The default is unsafe;
extensions that can write files, print output, run arbitrary code, show UI, or have
other external side effects are rejected before the nominal trajectory starts.

Each ``FlightDataBranch`` that reaches the ground becomes a landing body when it
either descends under a deployed recovery device or represents an independently
simulated stage-separation branch.  This keeps separated boosters selectable even
when they tumble or impact without deploying recovery.  Bodies are correlated
between trajectories by their stable source-component ID, not by the order in
which branches happen to be created in a particular run.  A ballistic primary
body remains excluded from the landing-dispersion view, but its scalar flight
metrics remain available.
Position X is reported as east and Y as north in metres.  A branch abort excludes
only that body from the run's dispersion statistics; successful sibling bodies
remain usable.  Trajectory-wide exceptions and missing ground hits are retained
as failures and excluded from the affected statistics.

Output statistics and UI
------------------------

The simulations panel enables **Monte Carlo analysis** for exactly one non-imported
simulation.  The setup dialog exposes all implemented inputs, the distribution,
and a display-unit spread.  It starts with practical, normally distributed starter
spreads so a new analysis produces a useful cloud immediately.  These values are
explicitly labeled as starting points rather than validated tolerances, can be
restored with **Reset defaults**, and should be replaced by measured uncertainty
data when available.  **All fixed** remains available for zeroing every explicit
spread.  A ``SwingWorker`` runs the trajectories off the event-dispatch thread and
supports cancellation.

The run count, seed, distributions, and spreads are saved on each simulation when
the setup dialog closes after a valid change, or when an analysis starts.  They are
restored whenever the setup dialog is reopened and are persisted in the ``.ork``
file.  A simulation that has never had its dispersion setup changed or run has no
saved configuration.

Separately, the most recent completed result is cached in memory for each simulation
and enables **Plot cached analysis** while its settings still match.  The action is
hidden as soon as those settings differ.  A cache entry is discarded when the
rocket, active flight configuration, simulation options, or simulation-extension
configuration no longer matches the inputs used for the analysis.  Weak simulation
references allow cached results to expire with closed documents; completed runs are
not written to the ``.ork`` file.

The results dialog contains two complementary views.  **Landing dispersion**
switches among all eligible landing bodies and provides:

* a pad-relative east/north scatter plot with nominal and sample-mean markers;
* empirical nearest-rank R50, R90, and R95 radii measured about the sample mean;
* one-, two-, and three-sigma sample-covariance ellipses;
* a PNG export of the plot as displayed; and
* every run's success/failure, landing, maximum altitude, and flight time.

**Flight metrics** switches among every flight-data branch and summarizes:

* apogee altitude;
* maximum velocity and Mach number;
* maximum acceleration;
* time to apogee and total flight time; and
* landing velocity when the branch reaches the ground.

The metric table reports the nominal value, mean, median, sample standard
deviation, empirical P5 and P95, and the valid-run count.  Selecting a row shows
either a histogram or a box plot in that metric's normal OpenRocket display unit.
Histogram bins are chosen automatically from the sample count using a bounded
square-root rule, and tooltips report each bin's numeric range and run count.  The
histogram shades the P5--P95 range and identifies nominal/sample-mean values.  The
horizontal box plot uses its conventional quartile, whisker, median, and outlier
marks without binning.  Metric plots always auto-fit the complete distribution rather
than offering interactive zoom.  Keeping one metric per plot avoids combining
incompatible units or hiding engineering values behind normalization.  Plot
export follows the active results tab.

CSV export contains the master seed, settings, simulation seed, sampled deviations
in explicit SI units, stable branch ID, per-run branch index, landing coordinates
when available, and all scalar metrics for every run/branch pair.

The scatter plot uses equal east/north scale, a theme-aware colorblind-friendly
palette, drag-to-pan, uniform mouse-wheel zoom, and explicit zoom-in, fit, and
zoom-out controls.  Covariance ellipses share one color and use solid, dashed,
and dash-dotted strokes for one, two, and three sigma.

The plot is titled after the simulation and subtitled with the landing body, the
number of dispersed runs that landed, and the master seed, so an exported image
identifies the analysis that produced it without the surrounding dialog.  The
export is laid out at the on-screen size and then drawn through a scaling
transform, which keeps the fitted equal-scale axes and the on-screen proportions
while supersampling to roughly 2,400 pixels wide for report and print use.

For an ideal bivariate-normal cloud, covariance ellipses at one, two, and three
Mahalanobis sigma contain approximately 39.3%, 86.5%, and 98.9%, respectively.
They do **not** mean 68.3%, 95.4%, and 99.7%, which are one-dimensional intervals.
For skewed, multimodal, clipped, or failure-heavy clouds the ellipses are only
descriptive; the empirical radii are the reported containment measures.

Known limitations and next steps
--------------------------------

The first implementation intentionally does not persist an analysis in an ORK
file.  It supports independent inputs only; it has no
correlation matrix, empirical distributions, historical weather ensembles,
per-stage mass/propulsion errors, explicit thrust/fin misalignment, wind-shear
variation, independent radial CG offsets, per-device parachute inflation, or
discrete failure modes.  A single recovery-drag multiplier covers every deployed
device, so a drogue and a main vary together.  Extensions that do not explicitly
opt into side-effect-free concurrent execution cannot be used in the analysis.  It
also lacks convergence confidence intervals, sensitivity attribution,
latitude/longitude conversion, terrain, and range-boundary overlays.

Two parameters are narrower than their names suggest.  Ignition timing shifts only
air-started and upper-stage motors, because moving the pad ignition translates the
whole flight in time without dispersing it.  Deployment timing cannot move an event
into the past, so for an apogee trigger with no configured delay the negative half
of the spread collapses onto apogee; near apogee the vehicle is vertically
stationary, so the accuracy cost is small, but the sampled value recorded in the CSV
is then not the value the trajectory used.

Before treating an analysis as decision evidence, characterize input data and
correlations, inspect all failed trajectories, repeat with other master seeds,
and increase the run count until the decision-relevant quantiles stabilize.
Adding bootstrap confidence bounds and a convergence view should precede any UI
claim that a finite sample proves a required containment probability.

Primary references
------------------

* `FAA AC 450.117-1, Trajectory Analysis for Normal Flight
  <https://www.faa.gov/documentLibrary/media/Advisory_Circular/AC_450.117-1.pdf>`_
* `NASA GSFC-STD-8009, Wallops Flight Facility Range Safety Manual
  <https://standards.nasa.gov/sites/default/files/standards/GSFC/Baseline/0/gsfc-_std-_8009.pdf>`_
* `NASA/TP-2010-216447, Applying Monte Carlo Simulation to Launch
  Vehicle Design and Requirements Analysis
  <https://ntrs.nasa.gov/citations/20100038453>`_
* `FAA, Handling Variability and Uncertainty in Flight Safety Analysis
  <https://www.faa.gov/media/68096>`_
* `NASA, Application of Statistically Derived CPAS Parachute Parameters
  <https://ntrs.nasa.gov/citations/20130011421>`_
* `NASA, Mars Exploration Rovers Landing Dispersion Analysis
  <https://ntrs.nasa.gov/citations/20040095913>`_
