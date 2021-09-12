package dev.nincodedo.dmca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.twitch4j.helix.domain.Clip;
import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
public class ClipDTO {

    public ClipDTO(Clip clip){
        id = clip.getId();
        url = clip.getUrl();
        embedUrl = clip.getEmbedUrl();
        broadcasterId = clip.getBroadcasterId();
        creatorId = clip.getCreatorId();
        videoId = clip.getVideoId();
        gameId = clip.getGameId();
        language = clip.getLanguage();
        title = clip.getTitle();
        viewCount = clip.getViewCount();
        createdAtInstant = clip.getCreatedAtInstant();
        thumbnailUrl = clip.getThumbnailUrl();
        duration = clip.getDuration();
    }

    /** ID of the clip being queried. */
    private String id;

    /** URL where the clip can be viewed. */
    private String url;

    /** URL to embed the clip. */
    private String embedUrl;

    /** User ID of the stream from which the clip was created. */
    private String broadcasterId;

    /** ID of the user who created the clip. */
    private String creatorId;
    private String creatorName;

    /** ID of the video from which the clip was created. */
    private String videoId;

    /** ID of the game assigned to the stream when the clip was created. */
    private String gameId;

    private String gameName;

    /** Language of the stream from which the clip was created. */
    private String language;

    /** Title of the clip. */
    private String title;

    /** Number of times the clip has been viewed. */
    private Integer viewCount;

    /** Date when the clip was created. */
    @JsonProperty("created_at")
    private Instant createdAtInstant;

    /** URL of the clip thumbnail. */
    private String thumbnailUrl;

    /** Duration of the Clip in seconds (up to 0.1 precision). */
    private Float duration;

    /**
     * @return the timestamp for the clip's creation
     * @deprecated in favor of getCreatedAtInstant()
     */
    @JsonIgnore
    @Deprecated
    public Date getCreatedAt() {
        return Date.from(createdAtInstant);
    }
}
