package fr.iban.spawners.listeners;

import com.ghostchu.quickshop.util.PermissionChecker;
import fr.iban.spawners.SpawnerManager;
import fr.iban.spawners.SpawnersPlugin;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaceBreakListenersTest {

    @Test
    void doesNotCancelQuickShopProtectionCheckForSpawnerPickaxe() throws ReflectiveOperationException {
        SpawnersPlugin plugin = mock(SpawnersPlugin.class);
        when(plugin.getName()).thenReturn("Spawners");
        when(plugin.namespace()).thenReturn("spawners");
        setPluginInstance(plugin);

        SpawnerManager manager = mock(SpawnerManager.class);
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack spawnerPickaxe = mock(ItemStack.class);
        Block block = mock(Block.class);
        BlockState blockState = mock(BlockState.class);

        when(plugin.getSpawnerManager()).thenReturn(manager);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInMainHand()).thenReturn(spawnerPickaxe);
        when(manager.isSpawnerPickaxe(spawnerPickaxe)).thenReturn(true);
        when(block.getState()).thenReturn(blockState);

        PermissionChecker.FakeBlockBreakEvent event =
                new PermissionChecker.FakeBlockBreakEvent(block, player);

        new PlaceBreakListeners(plugin).onBreak(event);

        assertFalse(event.isCancelled());
        verify(player, never()).sendMessage(anyString());
    }

    private void setPluginInstance(SpawnersPlugin plugin) throws ReflectiveOperationException {
        Field instance = SpawnersPlugin.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, plugin);
    }
}
