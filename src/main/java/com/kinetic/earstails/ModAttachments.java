package com.kinetic.earstails;

import com.kinetic.earstails.capability.PlayerEarsTailData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * NeoForge 1.21.1: хранение настроек ушек/хвоста через Data Attachments
 * (замена старой Forge-капабилити-системы). Автоматически сохраняется в NBT
 * игрока и копируется при смерти (`copyOnDeath`).
 */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, KineticEarsTailsMod.MOD_ID);

    public static final Supplier<AttachmentType<PlayerEarsTailData>> EARS_TAIL_DATA =
            ATTACHMENT_TYPES.register("ears_tail_data",
                    () -> AttachmentType.serializable(PlayerEarsTailData::new).copyOnDeath().build());

    private ModAttachments() {
    }

    /** Получить (или создать по умолчанию) настройки игрока. */
    public static PlayerEarsTailData get(Player player) {
        return player.getData(EARS_TAIL_DATA.get());
    }
}
