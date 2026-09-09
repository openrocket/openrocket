# NACA Report 1307 chart digitization metadata

This file records the provenance and interpolation metadata for the plotted
data used by `NACA1307FinBodyInterference`.  The source is NACA Report 1307,
*Lift and Center of Pressure of Wing-Body-Tail Combinations at Subsonic,
Transonic, and Supersonic Speeds*, NASA NTRS 19930091008.  The relevant source
plots are chart 15 on printed pages 65--67 and chart 16 on printed pages
68--70.  The rectangular-wing incidence data are from chart 3 on printed
page 49, with its selection rule on printed page 47.

The report publishes plots rather than numerical tables.  Consequently these
values are engineering digitizations, not exact source ordinates.  The analytic
slender-body, planar-pressure, and Appendix-D endpoints remain authoritative;
the digitized values only fair between those endpoints.

## Planform family mapping

The first Java array index maps to sweep condition and the second to taper
ratio.  The report panel mapping is:

| Sweep index | Sweep condition | Taper 0 | Taper 1/2 | Taper 1 |
| --- | --- | --- | --- | --- |
| 0 | no leading-edge sweep | (a), low-aspect extrapolation absent | (b) | (c) |
| 1 | no midchord sweep | (d) | (e) | (f) |
| 2 | no trailing-edge sweep | (g) | (h) | (i) |

The four radius families are the report's labelled
`r/s = 0, 0.2, 0.4, 0.6` curves.  Intermediate taper, sweep, and radius ratios
are linearly interpolated.  No extrapolation beyond the outer planform or
radius families is permitted by `isApplicable()`.

The report states on printed page 18 that the chart-15 extrapolation was not
attempted for panel (a), taper zero with no leading-edge sweep.  OpenRocket
therefore does not evaluate the complete model when interpolation would give
that missing panel a nonzero weight (`lambda < 0.5` and a leading-edge sweep
fraction below 0.5).  Such fins use the documented scalar fallback.  The
adjacent half-taper and no-midchord-sweep boundaries remain included because
panel (a) has zero interpolation weight there.

## Chart 15

The abscissa is represented as progress from `beta*A = 0` to the equation-22
planar boundary.  The ordinate is normalized as

```
w = (x_cp - x_slender) / (x_planar_boundary - x_slender).
```

For panels other than (c), the common dotted-curve guide was read at normalized boundary progress
`0, 0.125, ..., 1` and stored as
`0, 0.23, 0.43, 0.61, 0.75, 0.86, 0.94, 0.985, 1`.
The following abscissa multipliers align that guide with the four labelled
radius curves.  Rows are report panels (a)--(i); columns are radius ratio
`0, 0.2, 0.4, 0.6`.

| Panel | Multipliers |
| --- | --- |
| (a), not evaluated | 1.00, 0.98, 0.94, 0.90 |
| (b) | 1.00, 0.97, 0.92, 0.87 |
| (c) | 1.00, 0.97, 0.92, 0.87 |
| (d) | 1.00, 0.98, 0.93, 0.88 |
| (e) | 1.00, 0.98, 0.93, 0.88 |
| (f) | 1.00, 0.97, 0.92, 0.87 |
| (g) | 1.00, 1.00, 1.00, 1.00 |
| (h) | 1.00, 0.98, 0.94, 0.90 |
| (i) | 1.00, 0.97, 0.92, 0.87 |

Panel (c), the rectangular unswept planform, is not represented by those
small abscissa adjustments.  Its four radius curves visibly have different
curvature, especially near the origin.  The following normalized ordinates
were read independently and are stored directly:

| `r/s` | 0 | 0.25 | 0.50 | 0.75 | 1.00 |
| --- | --- | --- | --- | --- | --- |
| 0.0 | 0.000 | 0.100 | 0.700 | 0.940 | 1.000 |
| 0.2 | 0.000 | 0.359 | 0.705 | 0.872 | 1.000 |
| 0.4 | 0.000 | 0.336 | 0.690 | 0.858 | 1.000 |
| 0.6 | 0.000 | 0.275 | 0.550 | 0.775 | 1.000 |

Here the column headings are normalized progress to the equation-22
boundary.  For panel (c), that boundary is `beta*A=2`, so the three interior
columns correspond to the source abscissae `beta*A=0.5, 1.0, 1.5`.  Storing
the radius families independently corrects the largest former error: at
`r/s=0.6`, `beta*A=0.5`, the normalized ordinate is 0.275 rather than 0.378,
which had placed the body-load CP about `0.14*c_r` too far aft in the source
configuration.

Chart 15 was published for an afterbody.  For a finite cylindrical afterbody,
OpenRocket retains the normalized dotted-curve progress but replaces the
planar endpoint with the pressure integral clipped at the physical body end.
This is the construction suggested in the report's discussion preceding chart
15, using the no-afterbody result from chart 14(b).

## Chart 16

Each panel's `r/s = 0` dotted curve was read at
`beta*A = 0, 0.5, ..., 8`.  Its ordinate is normalized from the analytic
slender-body CP to the Appendix-D lifting-line CP.  The resulting ordinates are
stored in `CHART_16_LIFTING_LINE_WEIGHT`.

The following multipliers align the abscissa with the finite-radius curves.
Rows and columns use the same mapping as chart 15.

| Panel | Multipliers for r/s = 0, 0.2, 0.4, 0.6 |
| --- | --- |
| (a) | 1.00, 1.05, 1.10, 1.15 |
| (b) | 1.00, 1.02, 1.04, 1.06 |
| (c) | 1.00, 1.00, 1.00, 1.00 |
| (d) | 1.00, 1.15, 1.10, 1.05 |
| (e) | 1.00, 1.08, 1.05, 1.02 |
| (f) | 1.00, 1.00, 1.00, 1.00 |
| (g) | 1.00, 1.75, 1.75, 1.75 |
| (h) | 1.00, 1.12, 1.08, 1.05 |
| (i) | 1.00, 1.00, 1.00, 1.00 |

The large factor in panel (g) is directly traceable to the plot: the `r/s = 0`
curve reaches its lifting-line endpoint near `beta*A = 7`, while all three
finite-radius curves reach theirs near `beta*A = 4`; `7/4 = 1.75`.  Radius
interpolation is piecewise linear, so there is no discontinuity between the
`r/s = 0` and `r/s = 0.2` nodes.

## Chart 3

Chart 3 supplies the lowercase wing-incidence factor for rectangular wings.
Its solid linear-theory curves were digitized at radius/semispan ratios
`0, 0.05, 0.10, 0.15, 0.20, 0.30, 0.40, 0.60, 0.80, 1.0` for
`beta*A = 2, 3, 4`.  Radius interpolation is linear.  The selection guide uses
chart 3 only for supersonic rectangular wings with `beta*A > 2`; equation 19
is retained elsewhere.  Because the two approximations do not meet exactly,
their factors are smoothly faired over `beta*A=2.00` to `2.25` to avoid a
Mach-dependent roll-force step.  Between the three finite families
interpolation is linear in `beta*A`.  Above four, interpolation to the plotted
`beta*A=infinity` value of one is linear in `1/(beta*A)`.

| `beta*A` | `r/s=0` | 0.05 | 0.10 | 0.15 | 0.20 | 0.30 | 0.40 | 0.60 | 0.80 | 1.00 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 2 | 1.000 | 0.944 | 0.875 | 0.852 | 0.865 | 0.905 | 0.925 | 0.960 | 0.985 | 1.000 |
| 3 | 1.000 | 0.945 | 0.915 | 0.920 | 0.940 | 0.960 | 0.973 | 0.990 | 0.998 | 1.000 |
| 4 | 1.000 | 0.960 | 0.944 | 0.950 | 0.965 | 0.978 | 0.986 | 0.996 | 1.000 | 1.000 |
| infinity | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 | 1.000 |

The guide also points the uppercase angle-of-attack factor to chart 2 in that
same rectangular regime.  That instruction conflicts with the report's
discussion on printed page 5, which says linear theory neglects the wing-lift
loss near the body and recommends the slender-body uppercase factor for all
combinations.  OpenRocket follows that explicit physical recommendation and
continues to use equation 14 for the uppercase factor.  The report says the
two uppercase methods agree within five percent.

## Digitization tolerance and maintenance

The scan grid and curve thickness limit repeatability.  Source-point tests use
an acceptance tolerance of `0.02*c_r` for chart 15, `0.015*c_r` for chart 16,
and 0.006 in the dimensionless chart-3 factor.  These are digitization QA
tolerances, not statistical uncertainty bounds on the underlying aerodynamic
theory.

When updating the data:

1. Keep the source page, panel, taper, sweep, and radius family with every new
   point.
2. Normalize against the analytic endpoints before fitting or interpolating.
3. Preserve exact endpoint values at zero and one.
4. Validate at least one interior point from every altered panel and retain the
   tolerances above unless a higher-resolution source justifies tightening them.
