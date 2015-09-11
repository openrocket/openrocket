<?
$logfiles = "/home/groups/o/op/openrocket/persistent/logs/access-";


// getallheaders method
if (!function_exists('getallheaders')) {
    function getallheaders() {
       foreach ($_SERVER as $name => $value) {
           if (substr($name, 0, 5) == 'HTTP_') {
               $headers[str_replace(' ', '-', ucwords(strtolower(str_replace('_', ' ', substr($name, 5)))))] = $value;
           }
       }
       return $headers;
    }
}


// Parse + validate headers
$orid = "";
$orversion = "";
$oros = "";
$orjava = "";
$orcountry = "";
$orcores = "";
$orlocale = "";
foreach (getallheaders() as $header => $value) {
    if (preg_match("/^[a-zA-Z0-9 !$%&()*+,.\\/:=?@_~-]{1,40}$/", $value)) {
	$h = strtolower($header);
	if ($h == 'x-openrocket-version') {
	    $orversion = $value;
	} else if ($h == 'x-openrocket-id') {
	    $orid = $value;
	} else if ($h == 'x-openrocket-os') {
	    $oros = $value;
	} else if ($h == 'x-openrocket-java') {
	    $orjava = $value;
	} else if ($h == 'x-openrocket-country') {
	    $orcountry = $value;
	} else if ($h == 'x-openrocket-cpus') {
	    $orcores = $value;
	} else if ($h == 'x-openrocket-locale') {
	    $orlocale = $value;
	}
    }
}

// Log the request
if ((strlen($orversion) > 0 || strlen($orid) > 0 || strlen($oros) > 0
     || strlen($orjava) > 0 || strlen($orcountry) > 0 
     || strlen($orcores) > 0 || strlen($orlocale) > 0) &&
    (strlen($orversion) < 20 && strlen($orid) < 50 && strlen($oros) < 50
     && strlen($orjava) < 50 && strlen($orcountry) < 50) 
     && strlen($orcores) < 10 && strlen($orlocale) < 20) {

    $file = $logfiles . gmdate("Y-m");
    $line = gmdate("Y-m-d H:i:s") . ";" . $orid . ";" . $orversion .
	";" . $oros . ";" . $orjava . ";" . $orcountry . ";" . $orcores . 
	";" . $orlocale . "\n";

    $fp = fopen($file, 'a');
    if ($fp != FALSE) {
	fwrite($fp, $line);
	fclose($fp);
    }
}


// Set HTTP content-type header
// No charset allowed for 0.9.4
//header("Content-type: text/plain; charset=utf-8");
header("Content-type: text/plain");

/*
 * Currently all old versions are handled manually.
 * Update checking was introduced in OpenRocket 0.9.4
 *
 * We ignore "pre" versions, they are handled exacly like
 * their non-pre counterparts.
 */
$version = $_GET["version"];
$updates = "";

$unstable = "15.03";
$stable = "1.0.0";


if (preg_match("/^14.11$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: Experimental support for TubeFins\n".
    "10: Scriptable simulation extensions\n".
    "10: User configurable default mach\n".
    "6: Updated thrustcurves\n" .
    "4: Fixed a few bugs\n" .
    "";
} else if (preg_match("/^14.06$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "6: Updated thrustcurves\n" .
    "4: Fixed a few bugs\n" .
    "";
} else if (preg_match("/^14.05$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: Added Knots to windspeed and velocity units\n" .
    "6: Updated thrustcurves\n" .
    "4: Added Klima motor texture\n" .
    "4: Fixed annoying table bug\n" .
    "";
} else if (preg_match("/^14.03$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: Simulations will now compute the optimum delay for the sustainer\n" .
    "10: CG and Mass overrides are now indicated in the component tree\n" .
    "6: Updated thrustcurves - added Klima and SCR motors and various others\n" .
    "4: Removed the preset component Estes PNC-80FB since it was never produced\n" .
    "4: Updated 3D libraries\n" .
    "4: Bug fixes in motor selection dialog\n".
    "";
} else if (preg_match("/^13.11.2$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: Realistic in flight 3D photo construction\n" .
    "6: Updated thrustcurves\n".
    "4: Bug fixes\n".
    "";
} else if (preg_match("/^13.11.1$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: Realistic in flight 3D photo construction\n" .
    "6: Updated thrustcurves\n".
    "4: Bug fixes - Various fixes to motor selection\n".
    "";
} else if (preg_match("/^13.11$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: Realistic in flight 3D photo construction\n" .
    "6: Updated thrustcurves\n".
    "4: Bug fixes - Tube Coupler configuration among other things\n".
    "4: Bug fixes - Various fixes to motor selection\n".
    "";
} else if (preg_match("/^13.09.1$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: Realistic in flight 3D photo construction\n" .
    "6: Updated thrustcurves\n".
    "6: Simplified flight configuration process\n" .
    "4: New translations for Chinese\n" .
    "4: Updated 3D libraries\n" .
    "4: Bug fixes\n" .
    "";
} else if (preg_match("/^13.09$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: Realistic in flight 3D photo construction\n" .
    "6: Updated thrustcurves\n".
    "4: Updated Translations to Russian\n" .
    "4: Updated 3D libraries\n" .
    "4: Bug fixes\n" .
    "";
} else if (preg_match("/^13.05$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: Realistic in flight 3D photo construction\n" .
    "6: Updated thrustcurves\n".
    "6: Simplified flight configuration process\n" .
    "6: New thrustcurves for Aerotech C3 and D2\n" .
    "4: New translations for Chinese\n" .
    "4: Updated Translations to Russian\n" .
    "4: Updated 3D libraries\n" .
    "4: Bug fixes\n" .
    "";
} else if (preg_match("/^12.09.1$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: Realistic 3D rendering\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "6: New thrustcurves for Aerotech C3 and D2\n" .
    "5: New translations\n" .
    "4: Bug fixes\n" .
    "";
} else if (preg_match("/^12.09$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: Realistic 3D rendering\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "5: New translations\n" .
    "6: New thrustcurves for Aerotech C3 and D2\n" .
    "4: Bug fixes\n" .
    "";
} else if (preg_match("/^12.03$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "9: Component presets\n" .
    "4: Printing centering rings\n" .
    "";
} else if (preg_match("/^1\.1\.9$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "9: Component presets\n" .
    "8: Writing RKT files\n" .
    "5: Configurable stage separation\n" .
    "4: Freeform fin import from images\n" .
    "4: Translations to Italian and Russian\n" .
    "";
} else if (preg_match("/^1\.1\.8$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "9: Component presets\n" .
    "8: Writing RKT files\n" .
    "6: Additional template printing\n" .
    "5: Geodetic computations\n" .
    "5: Configurable stage separation\n" .
    "5: Guided tours\n" .
    "4: Freeform fin import from images\n" .
    "4: Translations to Italian and Russian\n" .
    "4: Bug fixes\n" .
    "";
} else if (preg_match("/^1\.1\.7$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "8: Writing RKT files\n" .
    "6: Additional template printing\n" .
    "5: Geodetic computations\n" .
    "5: Configurable stage separation\n" .
    "5: Guided tours\n" .
    "4: Freeform fin import from images\n" .
    "4: Translations to Italian and Russian\n" .
    "4: Bug fixes\n" .
    "";
} else if (preg_match("/^1\.1\.6$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "9: Component presets\n" .
    "8: Writing RKT files\n" .
    "8: Automatic rocket design optimization\n" .
    "6: Additional template printing\n" .
    "5: Geodetic computations\n" .
    "";
} else if (preg_match("/^1\.1\.5$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "9: Component presets\n" .
    "8: Writing RKT files\n" .
    "8: Automatic rocket design optimization\n" .
    "6: Initial localization support\n" .
    "6: Additional template printing\n" .
    "5: Geodetic computations\n" .
    "5: Scaling support\n" .
    "4: Bug fixes\n" .
    "";
} else if (preg_match("/^1\.1\.4$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "9: Component presets\n" .
    "8: Writing RKT files\n" .
    "8: Automatic rocket design optimization\n" .
    "6: Initial localization support\n" .
    "5: Fixes to printing system\n" .
    "5: Scaling support\n" .
    "";
} else if (preg_match("/^1\.1\.3$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "9: Component presets\n" .
    "8: Writing RKT files\n" .
    "8: Automatic rocket design optimization\n" .
    "7: Initial printing support\n" .
    "6: Initial localization support\n" .
    "5: Scaling support\n" .
    "4: Bug fixes\n" .
    "";
} else if (preg_match("/^1\.1\.[12]$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "9: Component presets\n" .
    "8: Writing RKT files\n" .
    "8: Automatic rocket design optimization\n" .
    "6: Initial printing support\n" .
    "5: Initial drag-and-drop support\n" .
    "5: Initial localization support\n" .
    "5: Scaling support\n" .
    "4: Bug fixes\n" .
    "";
} else if (preg_match("/^1\.1\.0$/", $version)) {
  $updates = "Version: " . $unstable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "9: Component presets\n" .
    "8: Writing RKT files\n" .
    "8: Automatic rocket design optimization\n" .
    "6: Initial printing support\n" .
    "6: Initial localization support\n" .
    "6: Enhanced motor selection\n" .
    "5: Rewritten simulation code\n" .
    "5: Drag-and-drop support\n" .
    "4: Bug fixes\n" .
    "";
} else if (preg_match("/^0\.9\.6/", $version)) {
  $updates = "Version: " . $stable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "9: Component presets\n" .
    "8: Writing RKT files\n" .
    "6: Hundreds of new thrustcurves\n" .
    "5: Bug fixes\n" .
    "";
} else if (preg_match("/^0\.9\.[45]/", $version)) {
  $updates = "Version: " . $stable . "\n" .
    "10: 3D design view\n" .
    "9: Flight configurations\n" .
    "8: Lower stage simulation\n" .
    "9: Component presets\n" .
    "8: Writing RKT files\n" .
    "7: Hundreds of new thrustcurves\n" .
    "6: Aerodynamic computation updates\n" .
    "5: Numerous bug fixes" .
    "";
}


if (strlen($updates) == 0) {

  // No updates available
  header("HTTP/1.0 204 No Content");

} else {

  header("HTTP/1.0 200 OK");
  echo $updates;

}

?>
