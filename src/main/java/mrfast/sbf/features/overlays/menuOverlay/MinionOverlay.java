package mrfast.sbf.features.overlays.menuOverlay;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.sbf.utils.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MinionOverlay {
    static HashMap<String,Long> lastCollected = new HashMap<>();
    static {
        readConfig();
    }
    @SubscribeEvent
    public void onDrawContainerTitle(TitleDrawnEvent event) {
        if (event.gui instanceof GuiChest && SkyblockFeatures.config.minionOverlay) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            double totalValue = 0d;

            String chestName = inv.getDisplayName().getUnformattedText().trim();
            if(chestName.contains(" Minion ") && !chestName.contains("Recipe")) {
                int secondsPerAction = 0;
                ItemStack generating = null;
                for(int slotId = 0;slotId<inv.getSizeInventory();slotId++) {
                    if(inv.getStackInSlot(slotId)==null) continue;

                    ItemStack stack = inv.getStackInSlot(slotId);
                    if (slotId == 4) {
                        List<String> lore = ItemUtils.getItemLore(stack);
                        for (String s : lore) {
                            String line = Utils.cleanColor(s);
                            if (line.contains("Actions:")) {
                                secondsPerAction = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                                if (line.contains(".")) {
                                    secondsPerAction /= 10;
                                }
                            }
                        }
                    }
                    if(isSlotFromMinion(slotId)) {
                        String identifier = PricingData.getIdentifier(stack);
                        if(identifier!=null) {
                            Double sellPrice = PricingData.bazaarPrices.get(identifier);
                            if(sellPrice!=null) totalValue += (sellPrice*stack.stackSize);
                        }
                        if(stack.getDisplayName().contains("Block") && !stack.getDisplayName().contains("Snow")) {
                            continue;
                        }
                        if(generating == null && ItemUtils.getRarity(stack) == ItemRarity.COMMON) {
                            generating = stack;
                        }
                    }
                }
                if(generating != null && ItemUtils.getRarity(generating) == ItemRarity.COMMON) {
                    String identifier = PricingData.getIdentifier(generating);
                    String[] lines;
                    if (identifier != null) {
                        Double sellPrice = PricingData.bazaarPrices.get(identifier);
                        if(sellPrice != null) {
                            Double perHour = Math.floor(((double) 3600 /secondsPerAction)*sellPrice);
                            String duration = "Unknown";
                            if(closestMinion != null && lastCollected.containsKey(closestMinion.getPosition().toString())) {
                                long timeElapsed = (System.currentTimeMillis()-lastCollected.get(closestMinion.getPosition().toString()))/1000L;
                                duration = Utils.secondsToTime(timeElapsed);
                            }
                            String fuelRunsOut = "Unlimited";
                            for(String line:ItemUtils.getItemLore(inv.getStackInSlot(19))) {
                                line = Utils.cleanColor(line);
                                if(line.startsWith("Time Remaining")) {
                                    fuelRunsOut=line.split(": ")[1];
                                    String shortTime = fuelRunsOut.split(" ")[1].substring(0,1);
                                    fuelRunsOut = fuelRunsOut.split(" ")[0]+shortTime;
                                }
                            }
                            lines = new String[]{
                                    ChatFormatting.LIGHT_PURPLE + chestName,
                                    ChatFormatting.WHITE + " • Time Between Actions: " + ChatFormatting.GREEN + secondsPerAction + "s",
                                    ChatFormatting.WHITE + " • Fuel Duration: " + ChatFormatting.YELLOW + fuelRunsOut,
                                    ChatFormatting.WHITE + " • Coins Per Hour: " + ChatFormatting.GOLD + Utils.nf.format(perHour),
                                    ChatFormatting.WHITE + " • Total Value: " + ChatFormatting.GOLD + Utils.shortenNumber(totalValue),
                                    ChatFormatting.WHITE + " • Last Collected: " + ChatFormatting.AQUA + duration
                            };
                        } else {
                            lines = new String[]{
                                    ChatFormatting.RED + "Unable to get item price!",
                                    ChatFormatting.RED + "Minion Generates: " + identifier
                            };
                        }
                    } else {
                        lines = new String[]{
                                ChatFormatting.RED + "Unable to get item id!",
                                ChatFormatting.RED + "Minion Generates: " + null
                        };
                    }
                    GuiUtils.drawSideMenu(Arrays.asList(lines), GuiUtils.TextStyle.DROP_SHADOW);
                }
            }
        }
    }
    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if(SkyblockFeatures.config.minionOverlay) {
            GuiChest gui = event.chest;
            ContainerChest chest;
            if(gui!=null && gui.inventorySlots!=null) {
                chest = (ContainerChest) gui.inventorySlots;
            } else {
                return;
            }
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            try {
                if(chestName.contains(" Minion ") && !chestName.contains("Recipe")) {
                    if(event.slot.getHasStack()) {
                        String nameOfItem = Utils.cleanColor(event.slot.getStack().getDisplayName());
                        if(nameOfItem.contains("Collect All") || isSlotFromMinion(event.slot.slotNumber)) {
                            if(closestMinion!=null) {
                                lastCollected.put(closestMinion.getPosition().toString(), System.currentTimeMillis());
                                saveConfig();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    private boolean isSlotFromMinion(int i) {
        return i == 21||i == 22||i == 23||i == 24||i == 25||i == 30||i == 31||i == 32||i == 33||i == 34||i == 39||i == 40||i == 41||i == 42||i == 43;
    }
    Entity closestMinion = null;
    @SubscribeEvent
    public void onRecievePacket(RenderWorldLastEvent event) {
        if(Utils.inSkyblock && SkyblockInfo.map.equals("Private Island") && (SkyblockFeatures.config.minionOverlay||SkyblockFeatures.config.minionLastCollected)) {
            for(Entity e : Utils.GetMC().theWorld.loadedEntityList){
                if(e instanceof EntityArmorStand) {
                    if(isMinion((EntityArmorStand) e)) {
                        if(SkyblockFeatures.config.minionLastCollected && lastCollected.containsKey(e.getPosition().toString()) && Utils.GetMC().thePlayer.getDistanceToEntity(e)<8) {
                            long timeElapsed = (System.currentTimeMillis()-lastCollected.get(e.getPosition().toString()))/1000L;
                            String duration = Utils.secondsToTime(timeElapsed);
                            RenderUtil.draw3DStringWithShadow(e.getPositionVector().add(new Vec3(0,1.5,0)),ChatFormatting.YELLOW+"Last Collected: "+ChatFormatting.AQUA+duration,event.partialTicks);
                        }

                        if(closestMinion==null) {
                            closestMinion = e;
                            continue;
                        }

                        if(Utils.GetMC().thePlayer.getDistanceToEntity(e)<Utils.GetMC().thePlayer.getDistanceToEntity(closestMinion)) {
                            closestMinion = e;
                        }
                    }
                }
            }

        }
    }

    public boolean isMinion(EntityArmorStand e) {
        for (int i = 0; i <= 3; i++) {
            if (e.getCurrentArmor(i) == null) return false;
        }

        return (Item.getIdFromItem(e.getCurrentArmor(0).getItem()) == 301 &&
                Item.getIdFromItem(e.getCurrentArmor(1).getItem()) == 300 &&
                Item.getIdFromItem(e.getCurrentArmor(2).getItem()) == 299 &&
                Item.getIdFromItem(e.getCurrentArmor(3).getItem()) == 397);
    }

    public MinionOverlay() {
        saveFile = new File(SkyblockFeatures.modDir, "collectedMinions.json");
        readConfig();
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static File saveFile;

    public static void readConfig() {
        if(Utils.GetMC().thePlayer==null) return;
        JsonObject file;
        try (FileReader in = new FileReader(saveFile)) {
            file = gson.fromJson(in, JsonObject.class);
            for (Map.Entry<String, JsonElement> e : file.entrySet()) {
                try {
                    long a = e.getValue().getAsLong();
                    lastCollected.put(e.getKey(), a);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            lastCollected = new HashMap<>();
            try (FileWriter writer = new FileWriter(saveFile)) {
                gson.toJson(lastCollected, writer);
            } catch (Exception ignored) {

            }
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(saveFile)) {
            gson.toJson(lastCollected, writer);
        } catch (Exception ignored) {

        }
    }
}
