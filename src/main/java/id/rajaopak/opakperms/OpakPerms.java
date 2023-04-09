package id.rajaopak.opakperms;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.common.reflect.ClassPath;
import id.rajaopak.opakperms.listener.LuckPermsListener;
import id.rajaopak.opakperms.redis.RedisManager;
import id.rajaopak.opakperms.util.Utils;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.text;

@Getter
public final class OpakPerms extends JavaPlugin {

    @Getter
    private static boolean debug;
    @Getter
    private static OpakPerms instance;
    private LuckPerms luckPerms;
    private AnnotationParser<CommandSender> annotationParser;
    private PaperCommandManager<CommandSender> manager;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private BukkitAudiences audiences;
    private RedisManager redisManager;
    private Utils utils;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
            new LuckPermsListener(this).register();
        }

        this.saveDefaultConfig();

        debug = this.getConfig().getBoolean("debug");

        this.redisManager = new RedisManager(this);
        this.utils = new Utils(this);

        if (this.redisManager.connect(this.getConfig().getString("host"),
                this.getConfig().getInt("port"),
                this.getConfig().getString("password"),
                this.getConfig().getString("channel"))) {
            Utils.logDebug("(Redis) Successfully connecting to redis server!");
        } else {
            Utils.logDebug("(Redis) Failed to connect to redis server!");
        }

        this.register();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.redisManager.close();
        Bukkit.getScheduler().cancelTasks(this);
    }

    @SneakyThrows
    public void register() {
        Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction = CommandExecutionCoordinator.simpleCoordinator();
        Function<CommandSender, CommandSender> mapperFunction = Function.identity();

        try {
            this.manager = new PaperCommandManager<>(this, executionCoordinatorFunction, mapperFunction, mapperFunction);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                CommandMeta.simple().with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description")).build();

        this.annotationParser = new AnnotationParser<>(this.manager,
                CommandSender.class, commandMetaFunction);

        this.audiences = BukkitAudiences.create(this);

        this.minecraftHelp = new MinecraftHelp<>("/opakperms help",
                this.audiences::sender,
                this.manager);

        if (this.manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            this.manager.registerBrigadier();
        }

        if (this.manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            this.manager.registerAsynchronousCompletions();
        }

        new MinecraftExceptionHandler<CommandSender>()
                .withInvalidSyntaxHandler()
                .withInvalidSenderHandler()
                .withArgumentParsingHandler()
                .withCommandExecutionHandler()
                .withDecorator(
                        component -> text()
                                .append(Utils.getPrefix())
                                .append(component).build()
                ).apply(this.manager, this.audiences::sender);

        this.minecraftHelp.setHelpColors(MinecraftHelp.HelpColors.of(
                NamedTextColor.DARK_GRAY,
                NamedTextColor.YELLOW,
                NamedTextColor.GRAY,
                NamedTextColor.GOLD,
                NamedTextColor.DARK_GRAY));

        this.commandRegister();
    }

    @SuppressWarnings("UnstableApiUsage")
    public void commandRegister() {
        this.getLogger().info("Loading and registering commands...");
        try {
            ClassPath classPath = ClassPath.from(this.getClass().getClassLoader());
            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive("id.rajaopak.opakperms.commands")) {
                try {
                    Class<?> commandClass = Class.forName(classInfo.getName());

                    Constructor<?>[] cons = commandClass.getConstructors();

                    for (Constructor<?> constructor : cons) {
                        if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].isAssignableFrom(OpakPerms.class)) {
                            this.parseAnnotationCommands(constructor.newInstance(this));
                            System.out.println(constructor.getName());
                        }
                    }
                } catch (Exception e) {
                    this.getLogger().severe("Failed loading command class: " + classInfo.getName());
                    e.printStackTrace();
                }
            }
            this.getLogger().info("Finish! " + manager.commands().size() + " Commands has been registered.");
        } catch (IOException e) {
            this.getLogger().severe("Failed loading command classes!");
            e.printStackTrace();
        }
    }

    private void parseAnnotationCommands(Object... clazz) {
        Arrays.stream(clazz).forEach(this.annotationParser::parse);
    }

    public void reload() {
        debug = this.getConfig().getBoolean("debug");
    }

    public boolean isSyncRank() {
        return this.getConfig().getBoolean("sync-player-rank");
    }

    public boolean isSyncPrefix() {
        return this.getConfig().getBoolean("sync-player-prefix");
    }

    public boolean isSyncSuffix() {
        return this.getConfig().getBoolean("sync-player-suffix");
    }

    public boolean isSyncPermission() {
        return this.getConfig().getBoolean("sync-player-permission");
    }

    public boolean isListenerEnable() {
        return this.getConfig().getBoolean("enable-listener");
    }

    public String getRedisChannel() {
        return this.getConfig().getString("channel");
    }
}
