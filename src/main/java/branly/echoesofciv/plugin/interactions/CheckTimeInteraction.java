package branly.echoesofciv.plugin.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.time.LocalDateTime;

public class CheckTimeInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<CheckTimeInteraction> Codec = BuilderCodec.builder(
            CheckTimeInteraction.class, CheckTimeInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("CommandBuffer is null");
            return;
        }

        Store<EntityStore> store = commandBuffer.getExternalData().getStore();
        WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
        LocalDateTime time = timeResource.getGameDateTime();
        String message = String.format("%02d:%02d %02d-%02d-%04d",
                time.getHour(), time.getMinute(),
                time.getDayOfMonth(), time.getMonthValue(), time.getYear());

        // Send message to player via notification window
        Ref<EntityStore> ref = interactionContext.getEntity();
        PlayerRef playerRef = store.getComponent(ref,PlayerRef.getComponentType());
        if (playerRef == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("PlayerRef is null");
            return;
        }
        PacketHandler packetHandler = playerRef.getPacketHandler();
        ItemWithAllMetadata icon = new ItemStack("EOC_Tool_Timepiece", 1).toPacket(); // Example item stack as icon
        
        NotificationUtil.sendNotification(
                packetHandler,
                Message.raw("Current World Time"),
                Message.raw(message).bold(true),
                icon,
                NotificationStyle.Default
        );
        //playerRef.sendMessage(Message.raw(message).bold(true));
        interactionContext.getState().state = InteractionState.Finished;
    }
}
