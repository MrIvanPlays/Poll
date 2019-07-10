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
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
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

                BufferedReader buffered = new BufferedReader(reader);
                String newest = buffered.readLine();
                buffered.close();
                reader.close();

                String current = plugin.getDescription().getVersion();
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

        public String compareVersions(String first, String second);

    }

    public static enum UpdateReason {

        NEW_UPDATE,
        COULD_NOT_CONNECT,
        UNAUTHORIZED_QUERY,
        UNRELEASED_VERSION,
        UNKNOWN_ERROR,
        UNSUPPORTED_VERSION_SCHEME,
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
            Preconditions.checkArgument(reason != UpdateReason.NEW_UPDATE,
                    "Reasons that require updates must also provide the latest version String");
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
