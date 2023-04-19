# Contributing

Although the current form of this project is a set of OpenRocket parts files, as the research has progressed
it has become a significant resource cataloging historical and current sport rocket vehicle parts.
Anyone who has relevant knowledge, expertise or specific information is encouraged to contribute.
The long term goal is to reduce actual errata to almost nil, with uncertain or disputed information clearly
marked as such.

I'm very interested in:

* Data for missing parts, including source attribution.
* Information about small manufacturers that are not represented in stock OpenRocket or this project
* Errata of any kind

If you have a large contribution, please fork the repo, make your changes, and submit a pull request.

This project follows the standard GitHub model for contribution:  please fork the repo, make and test your changes,
and then submit a pull request.  If you are adding new entries, please try to follow in detail the format of
existing material, as this will simplify future automated processing.

## Reporting Problems

Please file issues here on GitHub so that they can be tracked and get comments.
Please don't report problems on TRF, via email, etc. - use GitHub issues; all others may be ignored.

Here are some typical problem types:

* Parts that insert into OpenRocket with zero mass (indicates a problem in the material definition)
* Seeing a "discontinuity in diameter" message unexpectedly with supposedly compatible parts
* Parts that cause an error when a .ork file containing that part is saved (usually indiates a bad character in the part number) 

If you see a problem but don't know how or where it should get fixed, please go ahead and file a GitHub issue for it.
This is also be a good way to provide general information that doesn't seem to have a specific home in the
data files yet.
