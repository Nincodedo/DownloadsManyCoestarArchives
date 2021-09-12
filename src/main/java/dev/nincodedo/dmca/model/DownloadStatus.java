package dev.nincodedo.dmca.model;

import lombok.ToString;

@ToString
public enum DownloadStatus {
    FAILED, SUCCESSFUL, SKIPPED;

    private String clipId;

    public void setClipId(String clipId) {
        this.clipId = clipId;
    }
}
