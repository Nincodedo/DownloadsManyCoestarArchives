package dev.nincodedo.dmca;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import dev.nincodedo.dmca.model.DownloadConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"dev.nincodedo.dmca"})
public class BeanConfig {

    @Value("${dmca.twitch.token}")
    private String twitchAccessToken;

    @Value("${dmca.previous_days:7}")
    private int previousNumberOfDays;

    @Value("${dmca.minimum_views:20}")
    private int minimumViews;

    @Value("${dmca.run_once:false}")
    private boolean runOnce;

    @Bean
    public TwitchClient twitchClient(OAuth2Credential credential) {
        return TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withChatAccount(credential)
                .withDefaultAuthToken(credential)
                .withEnableChat(true)
                .build();
    }

    @Bean
    public OAuth2Credential credential() {
        return new OAuth2Credential("twitch", twitchAccessToken);
    }

    @Bean
    public DownloadConfig downloadConfig() {
        return new DownloadConfig(previousNumberOfDays, minimumViews, runOnce);
    }


}
