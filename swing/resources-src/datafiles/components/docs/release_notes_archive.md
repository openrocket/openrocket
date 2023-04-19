# Release Notes Archive

Here are release notes from non-recent versions of the OpenRocket parts database.

0.9.1.9 - Nov 2019
* Additions
   * Add note about existence of MMX X-15 starter set, per message from Chris Michielssen

0.9.1.8 - Apr 2019
* Fixes
   * LOC - add note confirming PNC-7.51 as 22.0" long and 30.6 oz weight

0.9.1.7 - Feb 2019
* Fixes
   * semroc - rearrange some NC description fields to newest layout
   * estes - match up numeric PN for PNC-20Y, link to authoritative YORF discussion
   * README - some general editing
* Additions
   * LOC - add note about official confirmation that BT-2.56 has always been 30" long

0.9.1.6 - Jan 2019
* Fixes
   * Quest - greatly improve measurements and mass on 3 transitions based on actual parts
* Additions
   * README - improved writeups on various company histories
   * Bluetube - added note about airframe/MMT ambiguity for the 1.15" and 1.52" sizes

0.9.1.5 - Dec 2018
* Fixes
   * Quest - Q10303 18mm motor mount tube - fix length to 2.6875", was 30"
   * Quest - 18mm thrust ring - set ID to measured value of 13.6 mm
   * Quest - 30-35mm transition Q21056 - set shoulder diameters to measured values
   * Quest - PNC35Nike - add some measurement info and make minor corrections to values
   * Quest - all plastic NC - set thickness to 1.3mm based on an actual instance
   * Quest - Q7810 14" parachute - set thickness and shroud length to match actual instance.
   * Semroc - make sure all Centuri nose cone cross-refs are referenced in the .orc
   * Semroc - fix reversed Centuri attribution on BC-846 and BC-846G
   * Semroc - improve discussion of Estes PNC-50Y, BNC-50Y vs Semroc BC-943 and BC-944.
   * Semroc - NC shoulder length fixups: completed BC-8xx, BC-8Fxx, BC-9xx, BNC-10x, BNC-19xx, BNC-2xx series.
   * Semroc - NC shoulder length fixups: started BNC-20xx series.
   * Semroc - fix large length error on BNC-20LS
* Additions
   * Semroc - added research note about the BC-BOID random ST-8 nose cone PN
   * Semroc - add note about ST-8F tube having same OD as old FSI HRT-8 tube
   * FSI - figured out correct IDs of RT-6, RT-8 / HRT-8 tubes, added to tube_data.txt
   * CMR - added CMR tube sizes to tube_data.txt


0.9.1.4 - Nov 2018
* Fixes
   * Estes: PK-18 and all other 18" chutes: line length is in inches, not meters.
   * Semroc: Corrected balsa material string on about 10% of parts, fixing zero masses
   * Semroc: Fix mfr on BNC-5W (was Estes)
   * Semroc: Verify/adjust nose cone shoulder lengths for BC-275xx, BC-5XX, BC-6xx, BC-7xx
   * Semroc: Harmonize many description strings
   * Semroc: BC-731 flagged possible design change to "short shoulder" version
   * Semroc: BC-818 changed shape to ELLIPSOID to better match shape (was OGIVE)
   * Semroc: Removed note about odd pricing of BC-739G, now stands corrected on e-rockets site
   * Semroc: Changed length of BC-RW825 to 2.3", apparent update on e-rockets website
* Additions
   * Semroc:
     * BC-5xx Centuri compatible nose cones, all were missing
     * BC-730, was missing
     * BC-715CN, BC-721CN, BC-726CN nacelle cone and nozzle sets as separate parts
     * BC-821, was missing
   * Quest: imported original Quest.orc and fixed it up.  98% done now.

0.9.1.3 - 2 Oct 2018
* Additions
  * LOC: added phenolic tubes that recently appeared on the website

0.9.1.2 - 24 Sep 2018
* Fixes
  * Semroc: Nose cone shoulder lengths corrected to mfg drawings for:
      BC-200xx, BC-225xx
  * LOC: Add 2018 website tube specs to tube_data.txt

0.9.1.1 - 14 Sep 2018
* Fixes
  * Semroc: BC-2ET Apollo escape NC/nozzle unit split into 2 correct pieces
  * Semroc: BC-1674 length is stated as 7.4", but mfg drawings scale out to 7.25".
  * Semroc: Note discontinuance of BC-175, BC-225, BC-275 nose cones as of 2018
  * Semroc: Nose cone shoulder length validation/correction for:
      BC-125xx, BC-13xx, BC-150xx, BC-16xx, BC-175xx, BC-18xx, BC-20xx
* Additions
  * Semroc: EBR-xx ejection baffle rings, fiber and plywood.
  * Semroc: BC-16838 "special" nose cone for 1.75" OD tube, from new eRockets site.  Matches no known tube size.

0.9.1 - 2 Sep 2018
* A minor release at last, based on bringing the Semroc file above 99% complete.

0.9.0.19 - 2 Sep 2018
* Fixes
  * Semroc: Add missing Semroc BTC-70HY and fix mass of Semroc BTC-70VY, was swapped with BTC-70HY
  * Semroc: Fix part number of BC-1022 (was BC-10222)
  * Semroc: BC-10xx, BC-11xx, BC-12xx - Fix shoulder lengths to match scaled drawings
  * Semroc: Fix length of BC-1045RR to match drawing.
  * Estes: Remove asterisk from Estes LaunchLug_0.25_x_2.0 PN to avoid PN handling bug in OR
  * Estes: Add missing carpet thread material to estes_classic file, shroud lines had no mass
* Additions
  * Semroc: Finished off balsa reducers, metric nose cones, launch lugs, parachutes and streamers
  * Semroc: Add missing BC-1338B
  * Estes: New file with extended discussion of Estes history and brands
  * Giant Leap Rocketry: Added info on company history and data availability
  * LOC: Obtained several missing centering ring thicknesses
  * LOC: Noted some data inconsistencies on sub-1-inch LOC tubes
  * AVI: Add discussion section

0.9.0.18 - Mar/Apr 2018
* Fixes
  * Corrected note about parts filtering - functionality exists but is effectively hidden by near zero-width field in UI.
  * Exterminated quote marks from 3 more Semroc part numbers due to bug identified by PR2, and verified that none
    exist in any other .orc files.
  * Fixed LOC parachute material to be 1.9 oz nylon per 2018 website
  * Minor fixes to ripstop nylon densities in `generic_materials.orc` and in the Top Flight file.
  * Reformatted many SOURCE ERROR documentation tags in semroc.orc to aid in parsing them for the upcoming
    errata report to e-rockets.
* Additions
  * Semroc balsa transitions BR-5xx, BR-6xx, BR-7xx, BR-8xx, BR-8Fxx, BR-9xx, BR-10xx, BR-11xx, BR-13xx
  * Semroc balsa transitions BR-16xx, BR-18xx, BR-085-xx, BR-115xx, BR-125xx, BR-150xx, BR-175xx, BR-225xx
  * Semroc balsa transitions TA-3xx, TA-5xx
  * Estes LL-2E 9.5" used in K-43 Mars Lander
  * Edited discussion of data situations for several vendors, added FSI and Quest
  * Added short python script to print out SOURCE ERROR tags
  * Added info on Quest/MPC metric tube sizes to tube_data.txt

0.9.0.17 - Jan 2018
* Fixes
  * Started matching Semroc NC shoulder lengths to drawings from legacy site (some were already done that way)
    * Series 200
    * Series 225
    * Series 275
* Additions
  * Semroc BC-20099
  * Note about the apparent high accuracy of nose cone drawings on the Semroc legacy site.

0.9.0.16 - Jan 2018
* Fixes
  * Merged PR2 - removed quote marks from Semroc PNs b/c OpenRocket mishandles in .ork's (thanks thzero)
  * Fix blue tube sizes/mass in tube_data.txt and body_tube_data.xlsx to current values from vendor
  * Fixed incorrect length of Estes PNC-55D
  * Fix ID/OD of Estes PST-65 to print catalog values (was erroneous 1974 custom parts catalog values)
  * Improve discussion of Estes part indexing situation
* Additions
  * Semroc nose cones completed!  Added:
    * BC-10, BC-11, BC-13, BNC-52, BNC-55, BC-125, BNC-58, BC-150, BC-16, BC-175, BC-18, BC-20, BC-200 series
    * BC-085, BNC-60, BNC-65, BNC-70, BNC-70H, BNC-80, BNC-80H series
    * BTC-11SC tailcone
  * Semroc - all clear payload tubes
  * bluetube.orc, with all body tubes and couplers, current published dimensions and empirical density
  * Estes SBT-xxx tube series added, complete per Brohm Appendix II
  * Estes PNC-60RL recovered from Semroc balsa clone dimensions and Brohm PN listing
  * Estes PNC-55EX, data recovered from actual sample and Semroc BNC-55EX
  * Estes BTC-55Z V-2 tail cone
  * Notes on PNC-55xx where no published data is available

0.9.0.15 - Nov 2017
* Fixes
  * Merged PR1 - ID/OD of BT-101 were swapped - thanks thzero
  * Fixed part number on Semroc BNC-50MA
* Additions
  * Estes PRP-1H, PNC-50K, PNC-50X, PNC-50V/PNC-50BB with tailcone
  * Semroc BNC-50 series finished off
  * Semroc BNC-51 series
  * ./gen/ directory with very beginnings of json -> XML part generator
  * ./docs/ directory with more focused explanation of Estes sizes and PNs

0.9.0.14 - Jul 2017
* Fixes
  * Reorganized research notes and errata in madcow.orc
  * Removed bogus "BNC-80K" tailcone transition from estes_classic.orc (Semroc only)
  * Fixed Top Flight parachute material specs to match generic_materials.orc
  * Adjusted Madcow coupler FC55 dimensions based on the official DX3 Massive RockSim file
  * Found numeric PN for Estes BNC-50BA on Semroc legacy site
  * Set length of Estes BNC-50BC and BNC-50BD to Estes specified values
  * Fixed ID/OD of Semroc BT-2+ (Semroc site was recently updated)
* Additions
  * Madcow G10 fiberglass centering rings, G12 4" to 54mm transition,  and launch lug
  * Madcow switch bands, balsa and G10 tail cones, and generic nylon parachutes
  * __Madcow is done__ until we get data on the two balsa ramjet nacelles
  * Semroc BNC-2xx, BNC-40xx, BNC-50xx, BC-8xx and BC-9xx nose cones
  * Wrote up initial design for JSON based XML generator

0.9.0.13 - 18 Jun 2017
* Fixes
  * Fix masses of Madcow fiberglass nose cones
  * Fix material type on a few Semroc plywood centering rings
* Additions
  * Madcow plywood centering rings
  * Madcow fiberglass nose cones done
  * README note about handling metal tip fiberglass nose cones
  * Research data for modelrockets.us tubes added to tube data spreadsheet

0.9.0.12 - 8 Jun 2017
* Added FSI and CMR tube sizes to tube data spreadsheet
* Added Madcow tube couplers
* Fix Estes PSII nylon chutes to have mass (paste in correct materials)

0.9.0.11 - 18 May 2017
* Inserted correct dimensions for CPT-10 clear tube in tube_data.txt

0.9.0.10 - 18 May 2017
* Semroc additions: BC-2xx, BC-6xx, BC-7xx, BC-8xx, BC-8Fxx, BNC-20xx completed

0.9.0.9 - 11 May 2017
* Estes additions:  BNC-3A, BNC-5RA, BWP-EJ, BNC-2 (Apollo capsule cone for BT-50), BNC-55AZ,
  BNC-55BE, oddball BNP-41 and PSM-1.  I am fairly confident that I now have all Estes balsa
  nose cones that existed prior to 2010.
* Added MPC file `mpc.orc`, which is essentially complete.
* Fixed material field on Estes BNC-70AJ
* Semroc additions: all BNC-5xx, BNC-10xx, BNC-19xx nose cones
* Added references section to README and moved in material from the estes and semroc files.
* Updated info about materials UnitsOfMeasure with improvements that appeared in OR 15.03
* Added discussion of MPC parts and catalogs.

0.9.0.8 - 3 May 2017
* Added a number of Estes balsa nose cones with dimensions recovered from 1974 custom parts
  catalog and Semroc legacy site.  Partial list: BNC-5AW, BNC-5BA, BNC-50AR, BNC-52G, BNC-52AG,
  BNC-55AW, BNC-60AL, BNC-65AF
* Added research notes on Estes BNC-60T vs BNC-60AK balsa Mercury capsules.
* Moved BNC-5AL from Estes to Semroc file after finding no proof Estes ever listed it and noting
  its absence from the Semroc cross reference list.
* Rectified a lot of Estes nose cone shapes where PARABOLIC should have been ELLIPSOID
* Removed unneeded mass overrides for Estes balsa nose cones and added some needed ones
* Reduced completion status of estes_classic file after finding various problems

0.9.0.7 - 1 May 2017
* Add README discussion about specialty nose cones and printing XML tags
* Document inability to set nose/transition shape parameter in .orc files
* Added first increment of Semroc nose cones (LT-225, LT-275)
* Added Estes BNC-5AX after recovering shape and length from Semroc upscale BC-22597

0.9.0.6 - 30 Apr 2017
* Semroc balsa couplers added, fixed various errors in Semroc file
* Added legacy glassine thin-wall version of JT-80C in Semroc and Estes files

0.9.0.5 - 26 Apr 2017
* Semroc tube couplers are in, except for balsa ones.
* Found and fixed a few errors in the Semroc file.

0.9.0.4 - 23 Apr 2017
* Finished Semroc centering rings, started on couplers.
* Added FSI tube data to tube data text file, renamed to tube_data.txt, moved to data/ directory.

0.9.0.3 - 19 Apr 2017
* Semroc body tubes completed, added around half of centering rings

0.9.0.2 - 16 Apr 2017
* Added estes_ps2.orc with nearly all known Pro Series II parts
* 80% of SEMROC tubes added
* Added tube data .txt file
* Many fixes, improvements and additions in loc_precision.orc; it's effectively done
* Documented procedure for getting correct mass and CG of hollow nose cones
* Fixed thickness of Estes AR-2050 and AR-2055

0.9.0.1 - 2 Apr 2017
* Publishing what I have to date: Estes and LOC Precision.
