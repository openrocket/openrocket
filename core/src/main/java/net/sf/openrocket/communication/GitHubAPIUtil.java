package net.sf.openrocket.communication;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Utility functions for the GitHub API.
 */
public class GitHubAPIUtil {
    private static final Logger log = LoggerFactory.getLogger(GitHubAPIUtil.class);
    private static final Translator trans = Application.getTranslator();

    /**
     * Generate a URL with a set of parameters included.
     * E.g. url = github.com/openrocket/openrocket/releases, params = {"lorem", "ipsum"}
     *      => formatted url: github.com/openrocket/openrocket/releases?lorem=ipsum
     * @param url base URL
     * @param params parameters to include
     * @return formatted URL (= base URL with parameters)
     */
    public static String generateUrlWithParameters(String url, Map<String, String> params) {
        StringBuilder formattedUrl = new StringBuilder(url);
        formattedUrl.append("?");        // Identifier for start of query string (for parameters)

        // Append the parameters to the URL
        int idx = 0;
        for (Map.Entry<String, String> e : params.entrySet()) {
            formattedUrl.append(String.format("%s=%s", e.getKey(), e.getValue()));
            if (idx < params.size() - 1) {
                formattedUrl.append("&");    // Identifier for more parameters
            }
            idx++;
        }
        return formattedUrl.toString();
    }

    /**
     * Fetches the JSON info from the specified GitHub API URL.
     * @param urlLink GitHub API link of the desired releases
     * @return JSON-formatted string (can be a JSON-array or JSON-object)
     * @throws Exception if an error occurred (e.g. no internet connection)
     */
    public static String fetchPageInfo(String urlLink) throws Exception {
        HttpsURLConnection connection = null;
        try {
            // Set up connection info to the GitHub release page
            URL url = new URL(urlLink);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(Communicator.CONNECTION_TIMEOUT);
            connection.setReadTimeout(Communicator.CONNECTION_TIMEOUT);

            // Connect to the GitHub page and get the status response code
            connection.connect();
            int status = connection.getResponseCode();
            log.debug("Update checker response code: " + status);

            // Invalid response code
            if (status != 200) {
                log.warn(String.format("Bad response code from server: %d", status));
                throw new Exception(String.format(trans.get("update.fetcher.badResponse"), status));
            }

            // Read the response JSON data into a StringBuilder
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();

            return sb.toString();
        } catch (UnknownHostException | SocketTimeoutException | ConnectException e) {
            log.warn(String.format("Could not connect to URL: %s. Please check your internet connection.", urlLink));
            throw new Exception(trans.get("update.fetcher.badConnection"));
        } catch (MalformedURLException e) {
            log.warn("Malformed URL: " + urlLink);
            throw new Exception(String.format(trans.get("update.fetcher.malformedURL"), urlLink));
        } catch (IOException e) {
            throw new Exception(String.format("Exception - %s: %s", e, e.getMessage()));
        } finally {     // Close the connection to the release page
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ex) {
                    log.warn("Could not disconnect update checker connection");
                }
            }
        }
    }
}
