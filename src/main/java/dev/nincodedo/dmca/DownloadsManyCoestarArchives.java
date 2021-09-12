package dev.nincodedo.dmca;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.helix.domain.ChannelInformation;
import com.github.twitch4j.helix.domain.Clip;
import com.github.twitch4j.helix.domain.Game;
import dev.nincodedo.dmca.model.ClipDTO;
import dev.nincodedo.dmca.model.DownloadConfig;
import dev.nincodedo.dmca.model.DownloadStatus;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class DownloadsManyCoestarArchives {

    @Autowired
    TwitchClient twitchClient;

    @Autowired
    OAuth2Credential credential;

    @Autowired
    DownloadConfig downloadConfig;

    public static void main(String[] args) {
        SpringApplication.run(DownloadsManyCoestarArchives.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext context) {
        return args -> getCoeClips();
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void getCoeClips() {
        log.info("Downloading popular clips for channel {} in the past {} days", "Coestar",
                downloadConfig.previousNumberOfDays());
        var coeClips = twitchClient.getHelix()
                .getClips(credential.getAccessToken(), "16678946", null, null, null, null, null,
                        LocalDate.now()
                                .minusDays(downloadConfig.previousNumberOfDays())
                                .atStartOfDay()
                                .toInstant(ZoneOffset.UTC),
                        LocalDate.now()
                                .plusDays(1)
                                .atStartOfDay()
                                .toInstant(ZoneOffset.UTC))
                .execute();
        var clipList = coeClips.getData()
                .stream()
                .filter(clip -> clip.getViewCount() >= downloadConfig.minimumViews())
                .toList();

        log.info("Total clips to download: {}", clipList.size());

        Map<String, String> clipCreators = getClipCreators(clipList);
        Map<String, String> games = getGameNames(clipList);
        var downloadStatuses = new ArrayList<DownloadStatus>();

        clipList.forEach(clip -> {
            var localClip = mapExtraClipData(clip, games.get(clip.getGameId()), clipCreators.get(clip.getCreatorId()));
            downloadStatuses.add(downloadClip(localClip));
        });

        log.info("Finished downloading {} clips {}", downloadStatuses.size(), downloadStatuses);
        if (downloadConfig.runOnce()) {
            System.exit(0);
        }
    }

    @NotNull
    private Map<String, String> getGameNames(List<Clip> clipList) {
        var gameIdList = clipList.stream().map(Clip::getGameId).toList();
        var games = twitchClient.getHelix()
                .getGames(credential.getAccessToken(), gameIdList, null)
                .execute()
                .getGames().stream().collect(Collectors.toMap(Game::getId, Game::getName));
        return games;
    }

    @NotNull
    private Map<String, String> getClipCreators(List<Clip> clipList) {
        var clipCreatorIds = clipList.stream().map(Clip::getCreatorId).toList();
        var clipCreators = twitchClient.getHelix()
                .getChannelInformation(credential.getAccessToken(), clipCreatorIds)
                .execute()
                .getChannels()
                .stream()
                .collect(Collectors.toMap(ChannelInformation::getBroadcasterId,
                        ChannelInformation::getBroadcasterName));
        return clipCreators;
    }

    private ClipDTO mapExtraClipData(Clip clip, String gameName, String clipCreatorName) {
        ClipDTO localClip = new ClipDTO(clip);
        localClip.setGameName(gameName);
        localClip.setCreatorName(clipCreatorName);
        return localClip;
    }

    public DownloadStatus downloadClip(ClipDTO localClip) {
        log.info("Downloading clip {}", localClip);
        var downloadStatus = DownloadStatus.FAILED;
        try {
            String commandExecutor = System.getProperty("os.name")
                    .toLowerCase()
                    .contains("windows") ? "cmd.exe /c" : "sh";
            ProcessBuilder builder = new ProcessBuilder(
                    commandExecutor,
                    "\"" + downloadConfig.downloadDirectory() + "\" && youtube-dl " + localClip.getUrl());
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                log.info(line);
                if (line.contains("has already been downloaded")) {
                    downloadStatus = DownloadStatus.SKIPPED;
                }
            }
            if (!downloadStatus.equals(DownloadStatus.SKIPPED)) {
                downloadStatus = DownloadStatus.SUCCESSFUL;
            }
            downloadStatus.setClipId(localClip.getId());
            return downloadStatus;
        } catch (Exception e) {
            log.error("o no", e);
            downloadStatus.setClipId(localClip.getId());
            return downloadStatus;
        }
    }
}
