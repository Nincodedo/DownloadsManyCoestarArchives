package dev.nincodedo.dmca.model;

public record DownloadConfig(int previousNumberOfDays, int minimumViews, boolean runOnce) {
}
