package fr.bebedlastreat.discord.velocity.implementations;

import fr.bebedlastreat.discord.common.interfaces.ICommonRunner;
import fr.bebedlastreat.discord.velocity.DiscordSyncVelocity;

import java.util.concurrent.TimeUnit;

public class VelocityRunner implements ICommonRunner {
    @Override
    public void runAsync(Runnable runnable) {
        DiscordSyncVelocity.getServer().getScheduler().buildTask(DiscordSyncVelocity.getInstance(), runnable).schedule();
    }

    @Override
    public void runLater(Runnable runnable, int ticks) {
        DiscordSyncVelocity.getServer().getScheduler().buildTask(DiscordSyncVelocity.getInstance(), runnable).delay(ticks* 50L, TimeUnit.MILLISECONDS).schedule();
    }
}
