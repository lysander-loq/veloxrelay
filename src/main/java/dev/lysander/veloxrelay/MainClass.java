package dev.lysander.veloxrelay;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import net.kyori.adventure.text.Component;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;

import org.bstats.charts.SingleLineChart;
import org.bstats.velocity.Metrics;
@Plugin(
    id = "veloxrelay",
    name = "VeloxRelay",
    version = "1.0.0",
    authors = {"lysander"}
)
public final class MainClass {
    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private List<String> webhooks;
    private String format;
    private String joinformat;
    private String enterformat;
    private String leaveformat;
    private final Metrics.Factory factory;
    private final HttpClient http = HttpClient.newHttpClient();
    @Inject
    public MainClass(
        ProxyServer proxy,
        Logger logger,
        @com.velocitypowered.api.plugin.annotation.DataDirectory Path dataDirectory,
        Metrics.Factory metricsFactory
    ) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.factory = metricsFactory;
    }
    private void loadConfig() {
        try {
            Files.createDirectories(dataDirectory);
            Path config = dataDirectory.resolve("config.yml");
            if (!Files.exists(config)) {
                try (OutputStream out = Files.newOutputStream(config)) {
                    MainClass.class.getClassLoader()
                        .getResourceAsStream("config.yml")
                        .transferTo(out);
                }
            }
            ConfigurationNode root = YamlConfigurationLoader.builder()
                .path(config)
                .build()
                .load();
            webhooks = root.node("webhooks").getList(String.class);
            webhooks = webhooks.stream()
                .filter(url -> url.startsWith("https://"))
                .toList();
            format = root.node("format").getString("`{server}` **{player}**: {message}");
            joinformat = root.node("joinformat").getString("**{player}** has connected to the proxy.");
            enterformat = root.node("enterformat").getString("**{player}** has entered `{server}`.");
            leaveformat = root.node("leaveformat").getString("**{player}** has disconnected from the proxy.");
            logger.info("Loaded {} webhook(s).", webhooks.size());
        } catch (Exception e) {logger.error("Failed to load config.",e);}
    }
    public void broadcastMessage(String content) {
        String json = """
            {
              "content": "%s",
              "allowed_mentions":{"parse":[]}
            }
            """.formatted(escape(content));
        for (String webhook : webhooks) {
            if (webhook == null || !webhook.startsWith("https://")) {
                continue;
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhook))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            http.sendAsync(request, HttpResponse.BodyHandlers.discarding());
        }
    }
    private class ReloadCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            if (!(source instanceof ConsoleCommandSource)&&!source.hasPermission("veloxrelay.reload")) {
                source.sendMessage(Component.text("No permission."));
                return;
            }
            loadConfig();
            source.sendMessage(Component.text("VeloxRelay config reloaded."));
        }
        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission("veloxrelay.reload")||invocation.source() instanceof ConsoleCommandSource;
        }
    }
    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        loadConfig();
        Metrics metrics = factory.make(this, 32936);
        metrics.addCustomChart(new SingleLineChart(
            "webhook_count",
            () -> webhooks.size()
        ));
        metrics.addCustomChart(new SingleLineChart(
            "average_players",
            () -> proxy.getPlayerCount()
        ));
        proxy.getCommandManager().register(
            proxy.getCommandManager().metaBuilder("vxreload")
                .plugin(this)
                .aliases("vxrelayreload")
                .build(),
            new ReloadCommand()
        );
    }
    @Subscribe
    public void onChat(PlayerChatEvent event) {
        if (format.trim().equals("")) return;
        Player player = event.getPlayer();
        String server = player.getCurrentServer()
            .map(s -> s.getServerInfo().getName())
            .orElse("unknown");
        String content = format
            .replace("{server}", server)
            .replace("{player}", player.getUsername())
            .replace("{message}", event.getMessage());
        broadcastMessage(content);
    }
    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        if (joinformat.trim().equals("")) return;
        Player player = event.getPlayer();
        String content = joinformat.replace("{player}", player.getUsername());
        broadcastMessage(content);
    }
    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        if (enterformat.trim().equals("")) return;
        Player player = event.getPlayer();
        String server = player.getCurrentServer()
            .map(s -> s.getServerInfo().getName())
            .orElse("unknown");
        String content = enterformat
            .replace("{server}", server)
            .replace("{player}", player.getUsername());
        broadcastMessage(content);
    }
    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (leaveformat.trim().equals("")) return;
        Player player = event.getPlayer();
        String content = leaveformat.replace("{player}", player.getUsername());
        broadcastMessage(content);
    }
    private static String escape(String s) {
        return s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "");
    }
}
