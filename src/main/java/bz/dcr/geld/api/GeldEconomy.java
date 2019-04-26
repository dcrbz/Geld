package bz.dcr.geld.api;

import bz.dcr.geld.Geld;
import bz.dcr.geld.data.MoneyManager;
import bz.dcr.geld.data.PlayerData;
import bz.dcr.geld.logging.VaultLogger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GeldEconomy implements Economy {

    private Geld plugin;

    // Constructor
    public GeldEconomy(Geld plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean isEnabled() {
        return this.plugin.isEnabled();
    }

    @Override
    public String getName() {
        return this.plugin.getName();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double value) {
        return this.plugin.getLang().formatCurrency(value);
    }

    @Override
    public String currencyNamePlural() {
        return this.plugin.getConfig().getString("Currency.Name-Plural");
    }

    @Override
    public String currencyNameSingular() {
        return this.plugin.getConfig().getString("Currency.Name-Singular");
    }

    @Override
    public boolean hasAccount(String name) {
        final Optional<UUID> target = plugin.getIdentificationProvider().getUUID(name);

        return target.isPresent() && this.plugin.getDB().hasPlayer(target.get());
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return this.plugin.getDB().hasPlayer(offlinePlayer.getUniqueId());
    }

    @Override
    public boolean hasAccount(String name, String s1) {
        return this.hasAccount(name);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return this.hasAccount(offlinePlayer);
    }

    @Override
    public double getBalance(String name) {
        final Optional<UUID> target = plugin.getIdentificationProvider().getUUID(name);

        if (!target.isPresent())
            return 0.0D;

        final Optional<PlayerData> data = this.plugin.getEconomy().getPlayerData(target.get());

        if (!data.isPresent())
            return 0.0D;

        return data.get().getBalance();
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        final Optional<PlayerData> data = this.plugin.getEconomy().getPlayerData(offlinePlayer.getUniqueId());

        if (!data.isPresent())
            return 0.0D;

        return data.get().getBalance();
    }

    @Override
    public double getBalance(String name, String s1) {
        return this.getBalance(name);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return this.getBalance(offlinePlayer);
    }

    @Override
    public boolean has(String name, double value) {
        return this.getBalance(name) >= value;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double value) {
        return this.getBalance(offlinePlayer) >= value;
    }

    @Override
    public boolean has(String name, String s1, double value) {
        return this.has(name, value);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double value) {
        return this.has(offlinePlayer, value);
    }

    @Override
    public EconomyResponse withdrawPlayer(String name, double value) {
        final Optional<UUID> target = plugin.getIdentificationProvider().getUUID(name);

        if (!target.isPresent())
            return new EconomyResponse(value, this.getBalance(name), EconomyResponse.ResponseType.FAILURE, "Player not existing");

        final MoneyManager.Result result = this.plugin.getEconomy().decreaseBalance(target.get(), value);

        // Logging
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        this.plugin.getExecutor().execute(() -> this.plugin.getVaultLogger().log(name, ste, value, VaultLogger.LogLevel.DECREASE));

        switch (result) {
            case SUCCESS:
                return new EconomyResponse(value, this.getBalance(name), EconomyResponse.ResponseType.SUCCESS, "");
            case UNKNOWN_PLAYER:
                return new EconomyResponse(value, 0.0D, EconomyResponse.ResponseType.FAILURE, "Player not known");
            case INEXISTENT_PLAYER:
                return new EconomyResponse(value, this.getBalance(name), EconomyResponse.ResponseType.FAILURE, "Player not existing");
        }

        return new EconomyResponse(value, 0.0D, EconomyResponse.ResponseType.FAILURE, "Unknown error");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double value) {
        final MoneyManager.Result result = this.plugin.getEconomy().decreaseBalance(offlinePlayer.getUniqueId(), value);

        // Logging
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        this.plugin.getExecutor().execute(() -> this.plugin.getVaultLogger().log(offlinePlayer.getName(), ste, value, VaultLogger.LogLevel.DECREASE));

        switch (result) {
            case SUCCESS:
                return new EconomyResponse(value, this.getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "");
            case UNKNOWN_PLAYER:
                return new EconomyResponse(value, 0.0D, EconomyResponse.ResponseType.FAILURE, "Player not known");
            case INEXISTENT_PLAYER:
                return new EconomyResponse(value, this.getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Player not existing");
        }

        return new EconomyResponse(value, 0.0D, EconomyResponse.ResponseType.FAILURE, "Unknown error");
    }

    @Override
    public EconomyResponse withdrawPlayer(String name, String s1, double value) {
        return this.withdrawPlayer(name, value);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double value) {
        return this.withdrawPlayer(offlinePlayer, value);
    }

    @Override
    public EconomyResponse depositPlayer(String name, double value) {
        final Optional<UUID> target = plugin.getIdentificationProvider().getUUID(name);

        if (!target.isPresent())
            return new EconomyResponse(value, this.getBalance(name), EconomyResponse.ResponseType.FAILURE, "Player not existing");

        final MoneyManager.Result result = this.plugin.getEconomy().increaseBalance(target.get(), value);

        // Logging
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        this.plugin.getExecutor().execute(() -> this.plugin.getVaultLogger().log(name, ste, value, VaultLogger.LogLevel.INCREASE));

        switch (result) {
            case SUCCESS:
                return new EconomyResponse(value, this.getBalance(name), EconomyResponse.ResponseType.SUCCESS, "");
            case UNKNOWN_PLAYER:
                return new EconomyResponse(value, 0.0D, EconomyResponse.ResponseType.FAILURE, "Player not known");
            case INEXISTENT_PLAYER:
                return new EconomyResponse(value, this.getBalance(name), EconomyResponse.ResponseType.FAILURE, "Player not existing");
        }

        return new EconomyResponse(value, 0.0D, EconomyResponse.ResponseType.FAILURE, "Unknown error");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double value) {
        final MoneyManager.Result result = this.plugin.getEconomy().increaseBalance(offlinePlayer.getUniqueId(), value);

        // Logging
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        this.plugin.getExecutor().execute(() -> this.plugin.getVaultLogger().log(offlinePlayer.getName(), ste, value, VaultLogger.LogLevel.INCREASE));

        switch (result) {
            case SUCCESS:
                return new EconomyResponse(value, this.getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "");
            case UNKNOWN_PLAYER:
                return new EconomyResponse(value, 0.0D, EconomyResponse.ResponseType.FAILURE, "Player not known");
            case INEXISTENT_PLAYER:
                return new EconomyResponse(value, this.getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Player not existing");
        }

        return new EconomyResponse(value, 0.0D, EconomyResponse.ResponseType.FAILURE, "Unknown error");
    }

    @Override
    public EconomyResponse depositPlayer(String name, String s1, double value) {
        return this.depositPlayer(name, value);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double value) {
        return this.depositPlayer(offlinePlayer, value);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0.0D, 0.0D, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String name) {
        final Optional<UUID> uuid = plugin.getIdentificationProvider().getUUID(name);

        if (!uuid.isPresent())
            return false;

        final PlayerData data = new PlayerData(uuid.get());
        this.plugin.getDB().addPlayer(data);
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        final PlayerData data = new PlayerData(offlinePlayer.getUniqueId());
        this.plugin.getDB().addPlayer(data);
        return true;
    }

    @Override
    public boolean createPlayerAccount(String name, String s1) {
        return this.createPlayerAccount(name);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return this.createPlayerAccount(offlinePlayer);
    }

}
