package bz.dcr.geld.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.*;
import java.util.Date;
import java.util.Locale;

public class LangManager {

    private File langFile;
    private YamlConfiguration lang;

    private DecimalFormat currencyFormat;
    private DecimalFormatSymbols currencySymbols = new DecimalFormatSymbols(Locale.GERMAN);

    private final DateFormat timeFormat = new SimpleDateFormat("dd.MM.yyyy | HH:mm");

    // Constructor
    public LangManager(File langFile) {
        this.currencySymbols.setDecimalSeparator(',');
        this.currencySymbols.setGroupingSeparator('.');
        this.currencyFormat = new DecimalFormat("#,##0.00", this.currencySymbols);

        this.langFile = langFile;
        this.lang = YamlConfiguration.loadConfiguration(this.langFile);
        this.lang.options().copyDefaults(true);

        this.lang.addDefault("prefix", "&a&l&oGELD &0\\ &r");
        this.lang.addDefault("playerCommand", "Dieser Befehl kann nur als Spieler ausgeführt werden.");
        this.lang.addDefault("noPermission", "&cDu hast keine Berechtigung dafür!");
        this.lang.addDefault("noMoney", "&cDu hast nicht genügend Geld dafür!");
        this.lang.addDefault("noMoneyTransfer", "&cDu hast nicht genügend Geld für diese Transaktion.");
        this.lang.addDefault("noMoneyTransferTax", "&cDu hast nicht genügend Geld um die Steuer von &f{0} &7({1}%) &czu zahlen.");
        this.lang.addDefault("invalidPage", "&cDie Seite &f{0} &cexistiert nicht.");
        this.lang.addDefault("unknownPlayer", "&cDieser Spieler ist uns nicht bekannt.");
        this.lang.addDefault("unknownNamedPlayer", "&cDer Spieler &f{0} &cist uns nicht bekannt.");
        this.lang.addDefault("inexistentPlayer", "&cDieser Spieler existiert nicht.");
        this.lang.addDefault("inexistentNamedPlayer", "&cDer Spieler &f{0} &cexistiert nicht.");
        this.lang.addDefault("transactionError", "&cBei der Überweisung ist ein unbekannter Fehler aufgetreten. Bitte wende dich an das Team!");
        this.lang.addDefault("invalidNumber", "&f{0} &cist keine gültige Zahl.");
        this.lang.addDefault("currentBalance", "&aDein Kontostand: &f{0} {1}");
        this.lang.addDefault("currentBalanceOthers", "&aKontostand von &f{0}&a: &f{1} {2}");
        this.lang.addDefault("balanceSet", "&aDu hast deinen Kontostand auf &f{0} {1} &agesetzt.");
        this.lang.addDefault("balanceSetOthers", "&aDu hast den Kontostand von &f{0} auf &f{1} {2} &agesetzt.");
        this.lang.addDefault("balanceGive", "&aDu hast dir &f{0} {1} &agegeben.");
        this.lang.addDefault("balanceGiveOthers", "&aDu hast &f{0} {1} {2} &agegeben.");
        this.lang.addDefault("balanceTake", "&aDu hast dir &f{0} {1} &agenommen.");
        this.lang.addDefault("balanceTakeOthers", "&aDu hast &f{0} {1} {2} &agenommen.");
        this.lang.addDefault("targetTransferDisabled", "&cDer Spieler &f{0} &cnimmt keine Überweisungen an.");
        this.lang.addDefault("transactionSent", "&aDu hast &f{0} {1} {2} &aüberwiesen.");
        this.lang.addDefault("transactionReceived", "&f{0} &ahat dir &f{1} {2} &aüberwiesen.");
        this.lang.addDefault("logTransaction", "{0} -> {1} ({2})");
        this.lang.addDefault("acceptTransfersOn", "&aDu akzeptierst nun Überweisungen.");
        this.lang.addDefault("acceptTransfersOff", "&cDu lehnst Überweisungen nun ab.");
        this.lang.addDefault("pinGenerated", "&7Generierte PIN: &f{0}\n&7Wert: &f{1} {2}");
        this.lang.addDefault("pinGeneratedHover", "&7Klicke um den Code in die\n&7Eingabezeile zu kopieren.");
        this.lang.addDefault("pinBought", "&aDu hast einen PIN-Code im Wert von &f{0} {1} &agekauft.\n&7(Fahre &7mit der Maus über diese Nachricht)");
        this.lang.addDefault("pinBoughtHover", "&7Klicke um den Code in die\n&7Eingabezeile zu kopieren.");
        this.lang.addDefault("pinRedeemed", "&aDu hast einen PIN-Code im Wert von &f{0} {1} &aeingelöst.");
        this.lang.addDefault("pinAlreadyRedeemed", "&cDieser PIN-Code wurde bereits eingelöst.");
        this.lang.addDefault("pinNotValid", "&cDieser PIN-Code ist nicht gültig.");
        this.lang.addDefault("criticalTransaction", "&c&lKritische Transaktion: &f{0} &7> &f{1} &7> &f{2}");
        this.lang.addDefault("transactionsHeader", "&7--==≡ &6&lTransaktionen &7≡==--");
        this.lang.addDefault("transactionsFooter", "&7---===< &6Seite {0}/{1} &7>===---");

        try {
            this.lang.save(this.langFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getMessage(String key, Object... replacements) {
        return MessageFormat.format(ChatColor.translateAlternateColorCodes('&', this.lang.getString(key)), replacements).replace("\\n", "\n");
    }

    public String getPrefixedMessage(String key, Object... replacements) {
        return ChatColor.translateAlternateColorCodes('&', this.lang.getString("prefix")) + this.getMessage(key, replacements);
    }


    /* UTILS */

    public String formatCurrency(double currency) {
        return this.currencyFormat.format(currency);
    }

    public String formatTimestamp(Date date) {
        return this.timeFormat.format(date);
    }
}
