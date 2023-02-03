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
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
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

public final class OpakPerms extends JavaPlugin {

    private LuckPerms luckPerms;

    private AnnotationParser<CommandSender> annotationParser;
    private PaperCommandManager<CommandSender> manager;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private RedisManager redisManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
            new LuckPermsListener(this).register();
        }
        
        this.saveDefaultConfig();

        this.redisManager = new RedisManager(this);

        this.redisManager.connect(this.getConfig().getString("host"),
                this.getConfig().getInt("port"),
                this.getConfig().getString("password"),
                this.getConfig().getString("channel"));

        this.register();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.redisManager.close();
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

        BukkitAudiences bukkitAudiences = BukkitAudiences.create(this);

        this.minecraftHelp = new MinecraftHelp<>("/opakperms help",
                bukkitAudiences::sender,
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
                                .append(text(Utils.colors(Utils.getPrefix())))
                                .append(component).build()
                ).apply(this.manager, bukkitAudiences::sender);

        this.minecraftHelp.setHelpColors(MinecraftHelp.HelpColors.of(
                TextColor.color(5592405),
                TextColor.color(16777045),
                TextColor.color(11184810),
                TextColor.color(5635925),
                TextColor.color(5592405)));

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

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public AnnotationParser<CommandSender> getAnnotationParser() {
        return this.annotationParser;
    }

    public PaperCommandManager<CommandSender> getManager() {
        return this.manager;
    }

    public MinecraftHelp<CommandSender> getMinecraftHelp() {
        return this.minecraftHelp;
    }

    public RedisManager getRedisManager() {
        return this.redisManager;
    }
}
