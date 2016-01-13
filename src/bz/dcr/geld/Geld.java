package bz.dcr.geld;

import bz.dcr.geld.api.GeldEconomy;
import bz.dcr.geld.cmd.CommandManager;
import bz.dcr.geld.cmd.MoneyCommand;
import bz.dcr.geld.cmd.PinCommand;
import bz.dcr.geld.cmd.money.*;
import bz.dcr.geld.cmd.pin.BuyCommand;
import bz.dcr.geld.cmd.pin.GenerateCommand;
import bz.dcr.geld.cmd.pin.RedeemCommand;
import bz.dcr.geld.data.*;
import bz.dcr.geld.listeners.ConnectListener;
import bz.dcr.geld.logging.VaultLogger;
import bz.dcr.geld.messaging.PluginMessageManager;
import bz.dcr.geld.pin.PinManager;
import bz.dcr.geld.util.AlertManager;
import bz.dcr.geld.logging.GeldLogger;
import bz.dcr.geld.util.LangManager;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Geld extends JavaPlugin {

    private Database database;
    private LangManager langManager;
    private MoneyManager moneyManager;
    private PinManager pinManager;
    private ExecutorService executor;
    private AlertManager alertManager;
    private PluginMessageManager pluginMessageManager;

    // Logging
    private GeldLogger geldLogger;
    private VaultLogger vaultLogger;

    // CommandManagers
    private CommandManager moneyCommandManager;
    private CommandManager pinCommandManager;

    @Override
    public void onEnable() {
        this.registerEconomy();

        if(this.isFirstStart()){
            this.loadConfig();
            this.getLogger().info("Erster Start: Bitte konfiguriere die Datenbank-Einstellungen!");
            this.getServer().shutdown();
            return;
        }

        this.pluginMessageManager = new PluginMessageManager(this, getConfig().getBoolean("BungeeCord"));
        this.executor = Executors.newCachedThreadPool();
        this.loadConfig();

        // Init loggers
        this.geldLogger = new GeldLogger(this, this.getConfig().getBoolean("Logging.Log-Transactions"));
        this.vaultLogger = new VaultLogger(this, this.getConfig().getBoolean("Logging.Log-Vault"));

        this.langManager = new LangManager(new File("plugins" + File.separatorChar + this.getName() + File.separatorChar + "lang.yml"));
        this.initDatabase();
        this.database.init();
        this.moneyManager = new MoneyManager(this);
        this.pinManager = new PinManager(this);
        this.alertManager = new AlertManager(this);
        this.registerCommands();
        this.registerListeners();
    }

    @Override
    public void onDisable() {
        try {
            if(this.geldLogger != null)
                this.geldLogger.close();
            if(this.vaultLogger != null)
                this.vaultLogger.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(this.database != null)
            this.database.shutdown();
    }


    private void loadConfig(){
        this.getConfig().options().copyDefaults(true);
        this.getConfig().addDefault("Database.Host", "127.0.0.1");
        this.getConfig().addDefault("Database.Port", 27017);
        this.getConfig().addDefault("Database.Database", "city_geld");
        this.getConfig().addDefault("Database.Auth.Enabled", false);
        this.getConfig().addDefault("Database.Auth.Type", "SCRAM-SHA-1");
        this.getConfig().addDefault("Database.Auth.Username", "root");
        this.getConfig().addDefault("Database.Auth.Password", "password");
        this.getConfig().addDefault("Database.Advanced.Min-Connections", 3);
        this.getConfig().addDefault("Database.Advanced.SSL-Enabled", true);
        this.getConfig().addDefault("BungeeCord", true);
        this.getConfig().addDefault("Currency.Name-Singular", "DM");
        this.getConfig().addDefault("Currency.Name-Plural", "DM");
        this.getConfig().addDefault("Pins.Expire-Days", 360);
        this.getConfig().addDefault("Pins.Generate-Tax-Percent", 5);
        this.getConfig().addDefault("Pins.Min-Value", 10.0D);
        this.getConfig().addDefault("Transaction-Tax.Enabled", true);
        this.getConfig().addDefault("Transaction-Tax.Percent", 5);
        this.getConfig().addDefault("Logging.Log-Transactions", true);
        this.getConfig().addDefault("Logging.Log-Vault", true);
        this.getConfig().addDefault("Logging.Transfers.Critical-Thresold", 50000.0D);
        this.saveConfig();
    }

    private void initDatabase(){
        MongoCredential dbCredential;
        if(this.getConfig().getBoolean("Database.Auth.Enabled")){
            switch (this.getConfig().getString("Database.Auth.Type").toUpperCase()){
                case "SCRAM-SHA-1":
                    dbCredential = MongoCredential.createScramSha1Credential(this.getConfig().getString("Database.Auth.Username"), this.getConfig().getString("Database.Database"), this.getConfig().getString("Database.Auth.Password").toCharArray());
                    break;
                case "MONGODB-CR":
                    dbCredential = MongoCredential.createMongoCRCredential(this.getConfig().getString("Database.Auth.Username"), this.getConfig().getString("Database.Database"), this.getConfig().getString("Database.Auth.Password").toCharArray());
                    break;
                case "X509":
                    dbCredential = MongoCredential.createMongoX509Credential(this.getConfig().getString("Database.Auth.Username"));
                    break;
                case "PLAIN":
                    dbCredential = MongoCredential.createPlainCredential(this.getConfig().getString("Database.Auth.Username"), this.getConfig().getString("Database.Database"), this.getConfig().getString("Database.Auth.Password").toCharArray());
                    break;
                case "GSSAPI":
                    dbCredential = MongoCredential.createGSSAPICredential(this.getConfig().getString("Database.Auth.Username"));
                    break;
                case "NONE":
                    dbCredential = MongoCredential.createCredential(this.getConfig().getString("Database.Auth.Username"), this.getConfig().getString("Database.Database"), this.getConfig().getString("Database.Auth.Password").toCharArray());
                    break;
                default:
                    dbCredential = MongoCredential.createCredential(this.getConfig().getString("Database.Auth.Username"), this.getConfig().getString("Database.Database"), this.getConfig().getString("Database.Auth.Password").toCharArray());
            }
        } else {
            dbCredential = MongoCredential.createCredential(this.getConfig().getString("Database.Auth.Username"), this.getConfig().getString("Database.Database"), this.getConfig().getString("Database.Auth.Password").toCharArray());
        }

        // Inform about used auth mechanism
        this.getLogger().info("Verwende Authentifizierungsmethode: " + (this.getConfig().getBoolean("Database.Auth.Enabled") ? this.getConfig().getString("Database.Auth.Type").toUpperCase() : "KEINE"));

        final MongoClientOptions dbOptions = MongoClientOptions.builder()
                .minConnectionsPerHost( this.getConfig().getInt("Database.Advanced.Min-Connections") )
                .sslEnabled( this.getConfig().getBoolean("Database.Advanced.SSL-Enabled") )
                .description("GELD_MONGO")
                .build();
        this.database = new MongoDB(
                new ServerAddress(
                        this.getConfig().getString("Database.Host"),
                        this.getConfig().getInt("Database.Port")
                ),
                dbOptions,
                dbCredential,
                this.getConfig().getBoolean("Database.Auth.Enabled")
        );
        this.database.init();
    }

    private void registerCommands(){
        // Money command
        this.moneyCommandManager = new CommandManager(this, "money", new MoneyCommand(this));
        this.moneyCommandManager.registerCommand("pay", new PayCommand(this));
        this.moneyCommandManager.registerCommand("balance", new BalanceCommand(this));
        this.moneyCommandManager.registerCommand("set", new SetCommand(this));
        this.moneyCommandManager.registerCommand("give", new GiveCommand(this));
        this.moneyCommandManager.registerCommand("take", new TakeCommand(this));
        this.moneyCommandManager.registerCommand("toggle", new ToggleCommand(this));
        this.moneyCommandManager.registerCommand("top", new TopCommand(this));
        this.moneyCommandManager.registerCommand("transactions", new TransactionsCommand(this));
        this.moneyCommandManager.registerCommand("ptransactions", new PTransactionsCommand(this));

        // Pin command
        this.pinCommandManager = new CommandManager(this, "pin", new PinCommand(this));
        this.pinCommandManager.registerCommand("buy", new BuyCommand(this));
        this.pinCommandManager.registerCommand("redeem", new RedeemCommand(this));
        this.pinCommandManager.registerCommand("generate", new GenerateCommand(this));
    }

    private void registerListeners(){
        this.getServer().getPluginManager().registerEvents(new ConnectListener(this), this);
    }

    private void registerEconomy(){
        if(this.getServer().getPluginManager().getPlugin("Vault") != null)
            this.getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, new GeldEconomy(this), this, ServicePriority.Highest);
        else
            this.getLogger().warning("Vault wurde nicht gefunden. Wechsle in API Modus.");
    }

    private boolean isFirstStart(){
        return !new File("plugins" + File.separatorChar + this.getName() + File.separatorChar + "config.yml").exists();
    }


    /* GETTERS */

    public Database getDB(){
        return database;
    }

    public LangManager getLang(){
        return langManager;
    }

    public MoneyManager getEconomy(){
        return moneyManager;
    }

    public PinManager getPinManager(){
        return pinManager;
    }

    public ExecutorService getExecutor(){
        return executor;
    }

    public GeldLogger getGeldLogger(){
        return geldLogger;
    }

    public VaultLogger getVaultLogger(){
        return vaultLogger;
    }

    public AlertManager getAlertManager(){
        return alertManager;
    }

    public PluginMessageManager getPluginMessageManager(){
        return pluginMessageManager;
    }


    // CommandManagers

    public CommandManager getMoneyCommandManager(){
        return this.moneyCommandManager;
    }

    public CommandManager getPinCommandManager(){
        return this.pinCommandManager;
    }

}
