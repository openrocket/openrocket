#!/usr/bin/perl

#
# Verify that keys used in Java files are present in the translation file.
# 
# Usage:
#    verifyTranslationKeys.pl <property file> <Java files...>
#
# For example:
#    find src/ -name "*.java" -exec ./scripts/verifyTranslationKeys.pl l10n/messages.properties {} +
#



# Read the translation file
my %keys;
print "Reading translation keys...\n";
while ($str = <>) {
    if ($ARGV!~/\.properties/) {
	last;
    }

    if ($str=~/^\s*($|[#!])/) {
	next;
    }

    if ($str=~/^([a-zA-Z0-9._-]+)\s*=/) {
	$keys{$1} = 1;
    } else {
	print "ERROR:  Invalid line in $ARGV: $str";
    }
}


# Read Java files
my $oldFile = $ARGV;
my $class="";
print "Reading Java files...\n";
while ($str = <>) {

    # Check for new file
    if ($ARGV != $oldFile) {
	$class = "";
    }
    
    # Check for irregular translator definition (exclude /l10n/ and /startup/)
    if ($str =~ / Translator / &&
	$str !~ /private static final Translator trans = Application.getTranslator\(\);/ &&
	$ARGV !~ /\/(l10n|startup)\//) {
	print "ERROR:  Unusual translator usage in file $ARGV: $str";
    }

    # Check for new class definition
    if ($str =~ /^[\sa-z]*class ([a-zA-Z0-9]+) /) {
	$class = $1;
    }

    # Check for translator usage
    if ($str =~ /trans\.get\(\"([^\"]+)\"\)/) {
	$key = $1;
	if (!(exists $keys{$key}) && 
	    !(exists $keys{$class . "." . $key})) {
	    print "ERROR:  Missing translation for '$key' in file $ARGV\n";
	}
    }

}
