# python script to scan and print SOURCE ERROR comments from an XML file

import sys
import regex


def usage():
    print("Usage: ")
    print("   source_errs.py foo.orc")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("name of file to scan for SOURCE ERROR comments is required!")
        usage()
        exit(1)

    with open(sys.argv[1], 'r') as xmlfile:
        xml_lines = xmlfile.readlines()

    # remove newlines since many SOURCE ERROR comments are multi-line
    # replace with unique string so can put line breaks back in multi-line tags
    xml = "XXnewlineXX".join(line.strip() for line in xml_lines)

    # print any comment containing SOURCE ERROR, stripping XML comment tags
    for i in regex.findall(r"<!--\s*?SOURCE ERROR.*?-->", xml):
        j = i.replace("XXnewlineXX", "\n").replace("<!-- ", "").replace("-->", "")
        print(j + "\n")
