package bz.dcr.geld.identification;

import bz.dcr.geld.Geld;

import java.util.Optional;
import java.util.UUID;

public class IdentificationProvider {

    private Geld plugin;


    public IdentificationProvider(Geld plugin) {
        this.plugin = plugin;
    }


    public Optional<String> getName(UUID uuid) {
        return Optional.ofNullable(
                plugin.getDcCorePlugin().getIdentificationProvider().getName(uuid)
        );
    }

    public Optional<UUID> getUUID(String name) {
        return Optional.ofNullable(
                plugin.getDcCorePlugin().getIdentificationProvider().getUUID(name)
        );
    }

}
