/*
 * Copyright 2019 Ivan Pekov (MrIvanPlays)

 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **/
package com.github.mrivanplays.poll.util;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Update checker
 * All credits should go to the author
 * https://www.spigotmc.org/threads/an-actually-decent-plugin-update-checker.344327/
 *
 * @author Parker Hawke - 2008Choco
 */
public final class UpdateChecker {

    public static final VersionScheme VERSION_SCHEME_DECIMAL = (first, second) -> {
        String[] firstSplit = splitVersionInfo(first), secondSplit = splitVersionInfo(second);
        if (firstSplit == null || secondSplit == null) {
            return null;
        }

        for (int i = 0; i < Math.min(firstSplit.length, secondSplit.length); i++) {
            int currentValue = NumberUtils.toInt(firstSplit[i]), newestValue = NumberUtils.toInt(secondSplit[i]);

            if (newestValue > currentValue) {
                return second;
            } else if (newestValue < currentValue) {
                return first;
            }
        }

        return (secondSplit.length > firstSplit.length) ? second : first;
    };

    private static final String USER_AGENT = "Poll-update-checker";
    private static final String UPDATE_URL = "https://api.spigotmc.org/legacy/update.php?resource=%d";
    private static final Pattern DECIMAL_SCHEME_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)*");

    private static UpdateChecker instance;

    private UpdateResult lastResult = null;

    private final JavaPlugin plugin;
    private final int pluginID;
    private final VersionScheme versionScheme;

    private UpdateChecker(JavaPlugin plugin, int pluginID, VersionScheme versionScheme) {
        this.plugin = plugin;
        this.pluginID = pluginID;
        this.versionScheme = versionScheme;
    }

    public CompletableFuture<UpdateResult> requestUpdateCheck() {
        return CompletableFuture.supplyAsync(() -> {
            int responseCode;
            try {
                URL url = new URL(String.format(UPDATE_URL, pluginID));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("User-Agent", USER_AGENT);

                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                responseCode = connection.getResponseCode();

                JsonElement element = new JsonParser().parse(reader);
                if (!element.isJsonArray()) {
                    return new UpdateResult(UpdateReason.INVALID_JSON);
                }

                reader.close();

                JsonObject versionObject = element.getAsJsonArray().get(0).getAsJsonObject();
                String current = plugin.getDescription().getVersion(), newest = versionObject.get("name").getAsString();
                String latest = versionScheme.compareVersions(current, newest);

                if (latest == null) {
                    return new UpdateResult(UpdateReason.UNSUPPORTED_VERSION_SCHEME);
                } else if (latest.equals(current)) {
                    return new UpdateResult(current.equals(newest) ? UpdateReason.UP_TO_DATE : UpdateReason.UNRELEASED_VERSION);
                } else if (latest.equals(newest)) {
                    return new UpdateResult(UpdateReason.NEW_UPDATE, latest);
                }
            } catch (IOException e) {
                return new UpdateResult(UpdateReason.COULD_NOT_CONNECT);
            } catch (JsonSyntaxException e) {
                return new UpdateResult(UpdateReason.INVALID_JSON);
            }

            return new UpdateResult(responseCode == 401 ? UpdateReason.UNAUTHORIZED_QUERY : UpdateReason.UNKNOWN_ERROR);
        });
    }

    public UpdateResult getLastResult() {
        return lastResult;
    }

    private static String[] splitVersionInfo(String version) {
        Matcher matcher = DECIMAL_SCHEME_PATTERN.matcher(version);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group().split("\\.");
    }

    public static UpdateChecker init(JavaPlugin plugin, int pluginID, VersionScheme versionScheme) {
        Preconditions.checkArgument(plugin != null, "Plugin cannot be null");
        Preconditions.checkArgument(pluginID > 0, "Plugin ID must be greater than 0");
        Preconditions.checkArgument(versionScheme != null, "null version schemes are unsupported");

        return (instance == null) ? instance = new UpdateChecker(plugin, pluginID, versionScheme) : instance;
    }

    public static UpdateChecker init(JavaPlugin plugin, int pluginID) {
        return init(plugin, pluginID, VERSION_SCHEME_DECIMAL);
    }

    public static UpdateChecker get() {
        Preconditions.checkState(instance != null, "Instance has not yet been initialized. Be sure #init() has been invoked");
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }


    @FunctionalInterface
    public static interface VersionScheme {

        /**
         * Compare two versions and return the higher of the two. If null is returned, it is assumed
         * that at least one of the two versions are unsupported by this version scheme parser.
         *
         * @param first  the first version to check
         * @param second the second version to check
         * @return the greater of the two versions. null if unsupported version schemes
         */
        public String compareVersions(String first, String second);

    }

    public static enum UpdateReason {

        /**
         * A new update is available for download on SpigotMC.
         */
        NEW_UPDATE, // The only reason that requires an update

        /**
         * A successful connection to the SpiGet API could not be established.
         */
        COULD_NOT_CONNECT,

        /**
         * The JSON retrieved from SpiGet was invalid or malformed.
         */
        INVALID_JSON,

        /**
         * A 401 error was returned by the SpiGet API.
         */
        UNAUTHORIZED_QUERY,

        /**
         * The version of the plugin installed on the server is greater than the one uploaded
         * to SpigotMC's resources section.
         */
        UNRELEASED_VERSION,

        /**
         * An unknown error occurred.
         */
        UNKNOWN_ERROR,

        /**
         * The plugin uses an unsupported version scheme, therefore a proper comparison between
         * versions could not be made.
         */
        UNSUPPORTED_VERSION_SCHEME,

        /**
         * The plugin is up to date with the version released on SpigotMC's resources section.
         */
        UP_TO_DATE

    }

    public final class UpdateResult {

        private final UpdateReason reason;
        private final String newestVersion;

        { // An actual use for initializer blocks. This is madness!
            lastResult = this;
        }

        private UpdateResult(UpdateReason reason, String newestVersion) {
            this.reason = reason;
            this.newestVersion = newestVersion;
        }

        private UpdateResult(UpdateReason reason) {
            Preconditions.checkArgument(reason != UpdateReason.NEW_UPDATE, "Reasons that require updates must also provide the latest version String");
            this.reason = reason;
            newestVersion = plugin.getDescription().getVersion();
        }

        public UpdateReason getReason() {
            return reason;
        }

        public boolean requiresUpdate() {
            return reason == UpdateReason.NEW_UPDATE;
        }

        public String getNewestVersion() {
            return newestVersion;
        }

    }

}
