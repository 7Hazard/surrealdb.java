package com.surrealdb;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * Builder style configuration for SurrealDB capabilities.
 * Mirrors the SurrealDB Rust SDK capabilities API.
 */
public final class Capabilities {
    static final int MODE_DEFAULT = 0;
    static final int MODE_ALLOW_ALL = 1;
    static final int MODE_ALLOW_NONE = 2;

    private boolean scripting;
    private boolean guestAccess;
    private boolean liveQueryNotifications;

    private int functionsMode;
    private int netTargetsMode;
    private int experimentalMode;

    private final List<String> allowedFunctions;
    private final List<String> deniedFunctions;
    private final List<String> allowedNetTargets;
    private final List<String> deniedNetTargets;
    private final EnumSet<ExperimentalFeature> allowedExperimentalFeatures;
    private final EnumSet<ExperimentalFeature> deniedExperimentalFeatures;

    private Capabilities(
        boolean scripting,
        boolean guestAccess,
        boolean liveQueryNotifications,
        int functionsMode,
        int netTargetsMode,
        int experimentalMode
    ) {
        this.scripting = scripting;
        this.guestAccess = guestAccess;
        this.liveQueryNotifications = liveQueryNotifications;
        this.functionsMode = functionsMode;
        this.netTargetsMode = netTargetsMode;
        this.experimentalMode = experimentalMode;
        this.allowedFunctions = new ArrayList<>();
        this.deniedFunctions = new ArrayList<>();
        this.allowedNetTargets = new ArrayList<>();
        this.deniedNetTargets = new ArrayList<>();
        this.allowedExperimentalFeatures = EnumSet.noneOf(ExperimentalFeature.class);
        this.deniedExperimentalFeatures = EnumSet.noneOf(ExperimentalFeature.class);
    }

    public static Capabilities defaults() {
        return new Capabilities(false, false, true, MODE_DEFAULT, MODE_DEFAULT, MODE_DEFAULT);
    }

    public static Capabilities all() {
        return new Capabilities(true, true, true, MODE_ALLOW_ALL, MODE_ALLOW_ALL, MODE_DEFAULT);
    }

    public static Capabilities none() {
        return new Capabilities(false, false, false, MODE_ALLOW_NONE, MODE_ALLOW_NONE, MODE_ALLOW_NONE);
    }

    public Capabilities withScripting(boolean enabled) {
        this.scripting = enabled;
        return this;
    }

    public Capabilities withGuestAccess(boolean enabled) {
        this.guestAccess = enabled;
        return this;
    }

    public Capabilities withLiveQueryNotifications(boolean enabled) {
        this.liveQueryNotifications = enabled;
        return this;
    }

    public Capabilities withAllFunctionsAllowed() {
        this.functionsMode = MODE_ALLOW_ALL;
        this.allowedFunctions.clear();
        return this;
    }

    public Capabilities withNoFunctionsAllowed() {
        this.functionsMode = MODE_ALLOW_NONE;
        this.allowedFunctions.clear();
        return this;
    }

    public Capabilities withFunctionAllowed(String function) {
        Objects.requireNonNull(function, "function");
        this.functionsMode = MODE_DEFAULT;
        this.allowedFunctions.add(function);
        return this;
    }

    public Capabilities withFunctionDenied(String function) {
        Objects.requireNonNull(function, "function");
        this.deniedFunctions.add(function);
        return this;
    }

    public Capabilities withAllNetTargetsAllowed() {
        this.netTargetsMode = MODE_ALLOW_ALL;
        this.allowedNetTargets.clear();
        return this;
    }

    public Capabilities withNoNetTargetsAllowed() {
        this.netTargetsMode = MODE_ALLOW_NONE;
        this.allowedNetTargets.clear();
        return this;
    }

    public Capabilities withNetTargetAllowed(String target) {
        Objects.requireNonNull(target, "target");
        this.netTargetsMode = MODE_DEFAULT;
        this.allowedNetTargets.add(target);
        return this;
    }

    public Capabilities withNetTargetDenied(String target) {
        Objects.requireNonNull(target, "target");
        this.deniedNetTargets.add(target);
        return this;
    }

    public Capabilities withAllExperimentalFeaturesAllowed() {
        this.experimentalMode = MODE_ALLOW_ALL;
        this.allowedExperimentalFeatures.clear();
        return this;
    }

    public Capabilities withNoExperimentalFeaturesAllowed() {
        this.experimentalMode = MODE_ALLOW_NONE;
        this.allowedExperimentalFeatures.clear();
        return this;
    }

    public Capabilities withExperimentalFeatureAllowed(ExperimentalFeature feature) {
        Objects.requireNonNull(feature, "feature");
        this.experimentalMode = MODE_DEFAULT;
        this.allowedExperimentalFeatures.add(feature);
        return this;
    }

    public Capabilities withExperimentalFeatureDenied(ExperimentalFeature feature) {
        Objects.requireNonNull(feature, "feature");
        this.deniedExperimentalFeatures.add(feature);
        return this;
    }

    boolean isScripting() {
        return scripting;
    }

    boolean isGuestAccess() {
        return guestAccess;
    }

    boolean isLiveQueryNotifications() {
        return liveQueryNotifications;
    }

    int getFunctionsMode() {
        return functionsMode;
    }

    int getNetTargetsMode() {
        return netTargetsMode;
    }

    int getExperimentalMode() {
        return experimentalMode;
    }

    String[] getAllowedFunctionsArray() {
        return allowedFunctions.toArray(new String[0]);
    }

    String[] getDeniedFunctionsArray() {
        return deniedFunctions.toArray(new String[0]);
    }

    String[] getAllowedNetTargetsArray() {
        return allowedNetTargets.toArray(new String[0]);
    }

    String[] getDeniedNetTargetsArray() {
        return deniedNetTargets.toArray(new String[0]);
    }

    int[] getAllowedExperimentalFeaturesArray() {
        return allowedExperimentalFeatures.stream().mapToInt(ExperimentalFeature::getValue).toArray();
    }

    int[] getDeniedExperimentalFeaturesArray() {
        return deniedExperimentalFeatures.stream().mapToInt(ExperimentalFeature::getValue).toArray();
    }
}
