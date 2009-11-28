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
	}
    }
}

// Log the request
if ((strlen($orversion) > 0 || strlen($orid) > 0 || strlen($oros) > 0
     || strlen($orjava) > 0 || strlen($orcountry) > 0) &&

    (strlen($orversion) < 20 && strlen($orid) < 50 && strlen($oros) < 50
     && strlen($orjava) < 50 && strlen($orcountry) < 50)) {

    $file = $logfiles . gmdate("Y-m");
    $line = gmdate("Y-m-d H:i:s") . ";" . $orid . ";" . $orversion .
	";" . $oros . ";" . $orjava . ";" . $orcountry . "\n";

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
 */
$version = $_GET["version"];
$updates = "";

if (preg_match("/^0\.9\.(4|5pre)/",$version)) {
  $updates = "Version: 0.9.5\n" .
    "5: Important bug fixes";
}


if (strlen($updates) == 0) {

  // No updates available
  header("HTTP/1.0 204 No Content");

} else {

  header("HTTP/1.0 200 OK");
  echo $updates;

}

?>