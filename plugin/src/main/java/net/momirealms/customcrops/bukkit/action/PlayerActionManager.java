/*
 *  Copyright (C) <2024> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customcrops.bukkit.action;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.api.action.AbstractActionManager;
import net.momirealms.customcrops.api.action.Action;
import net.momirealms.customcrops.api.context.ContextKeys;
import net.momirealms.customcrops.api.core.block.CropBlock;
import net.momirealms.customcrops.api.core.block.CropConfig;
import net.momirealms.customcrops.api.core.block.CropStageConfig;
import net.momirealms.customcrops.api.core.world.CustomCropsBlockState;
import net.momirealms.customcrops.api.core.world.CustomCropsWorld;
import net.momirealms.customcrops.api.core.world.Pos3;
import net.momirealms.customcrops.api.misc.placeholder.BukkitPlaceholderManager;
import net.momirealms.customcrops.api.misc.value.MathValue;
import net.momirealms.customcrops.api.misc.value.TextValue;
import net.momirealms.customcrops.api.util.LocationUtils;
import net.momirealms.customcrops.api.util.PlayerUtils;
import net.momirealms.customcrops.bukkit.integration.VaultHook;
import net.momirealms.customcrops.bukkit.misc.HologramManager;
import net.momirealms.customcrops.common.helper.AdventureHelper;
import net.momirealms.customcrops.common.util.ListUtils;
import net.momirealms.customcrops.common.util.RandomUtils;
import net.momirealms.sparrow.heart.SparrowHeart;
import net.momirealms.sparrow.heart.feature.inventory.HandSlot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class PlayerActionManager extends AbstractActionManager<Player> {

    public PlayerActionManager(BukkitCustomCropsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        loadExpansions(Player.class);
    }

    @Override
    protected void registerBuiltInActions() {
        super.registerBuiltInActions();
        super.registerBundleAction(Player.class);
        this.registerPlayerCommandAction();
        this.registerCloseInvAction();
        this.registerActionBarAction();
        this.registerExpAction();
        this.registerFoodAction();
        this.registerItemAction();
        this.registerMoneyAction();
        this.registerPotionAction();
        this.registerSoundAction();
        this.registerPluginExpAction();
        this.registerTitleAction();
        this.registerSwingHandAction();
        this.registerForceTickAction();
        this.registerHologramAction();
        this.registerMessageAction();
    }

    private void registerMessageAction() {
        registerAction((args, chance) -> {
            List<String> messages = ListUtils.toList(args);
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                List<String> replaced = plugin.getPlaceholderManager().parse(context.holder(), messages, context.placeholderMap());
                Audience audience = plugin.getSenderFactory().getAudience(context.holder());
                for (String text : replaced) {
                    audience.sendMessage(AdventureHelper.miniMessage(text));
                }
            };
        }, "message");
        registerAction((args, chance) -> {
            List<String> messages = ListUtils.toList(args);
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                String random = messages.get(RandomUtils.generateRandomInt(0, messages.size() - 1));
                random = BukkitPlaceholderManager.getInstance().parse(context.holder(), random, context.placeholderMap());
                Audience audience = plugin.getSenderFactory().getAudience(context.holder());
                audience.sendMessage(AdventureHelper.miniMessage(random));
            };
        }, "random-message");
    }

    private void registerPlayerCommandAction() {
        registerAction((args, chance) -> {
            List<String> commands = ListUtils.toList(args);
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                List<String> replaced = BukkitPlaceholderManager.getInstance().parse(context.holder(), commands, context.placeholderMap());
                plugin.getScheduler().sync().run(() -> {
                    for (String text : replaced) {
                        context.holder().performCommand(text);
                    }
                }, context.holder().getLocation());
            };
        }, "player-command");
    }

    private void registerCloseInvAction() {
        registerAction((args, chance) -> context -> {
            if (context.holder() == null) return;
            if (Math.random() > chance) return;
            context.holder().closeInventory();
        }, "close-inv");
    }

    private void registerActionBarAction() {
        registerAction((args, chance) -> {
            String text = (String) args;
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                Audience audience = plugin.getSenderFactory().getAudience(context.holder());
                Component component = AdventureHelper.miniMessage(plugin.getPlaceholderManager().parse(context.holder(), text, context.placeholderMap()));
                audience.sendActionBar(component);
            };
        }, "actionbar");
        registerAction((args, chance) -> {
            List<String> texts = ListUtils.toList(args);
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                String random = texts.get(RandomUtils.generateRandomInt(0, texts.size() - 1));
                random = plugin.getPlaceholderManager().parse(context.holder(), random, context.placeholderMap());
                Audience audience = plugin.getSenderFactory().getAudience(context.holder());
                audience.sendActionBar(AdventureHelper.miniMessage(random));
            };
        }, "random-actionbar");
    }

    private void registerExpAction() {
        registerAction((args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                final Player player = context.holder();
                ExperienceOrb entity = player.getLocation().getWorld().spawn(player.getLocation().clone().add(0,0.5,0), ExperienceOrb.class);
                entity.setExperience((int) value.evaluate(context));
            };
        }, "mending");
        registerAction((args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                final Player player = context.holder();
                player.giveExp((int) Math.round(value.evaluate(context)));
                Audience audience = plugin.getSenderFactory().getAudience(player);
                AdventureHelper.playSound(audience, Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.PLAYER, 1, 1));
            };
        }, "exp");
        registerAction((args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                Player player = context.holder();
                player.setLevel((int) Math.max(0, player.getLevel() + value.evaluate(context)));
            };
        }, "level");
    }

    private void registerFoodAction() {
        registerAction((args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                Player player = context.holder();
                player.setFoodLevel((int) (player.getFoodLevel() + value.evaluate(context)));
            };
        }, "food");
        registerAction((args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                Player player = context.holder();
                player.setSaturation((float) (player.getSaturation() + value.evaluate(context)));
            };
        }, "saturation");
    }

    private void registerItemAction() {
        registerAction((args, chance) -> {
            if (args instanceof Section section) {
                boolean mainOrOff = section.getString("hand", "main").equalsIgnoreCase("main");
                int amount = section.getInt("amount", 1);
                return context -> {
                    if (context.holder() == null) return;
                    if (Math.random() > chance) return;
                    Player player = context.holder();
                    boolean tempHand = mainOrOff;
                    EquipmentSlot hand = context.arg(ContextKeys.SLOT);
                    if (hand == EquipmentSlot.OFF_HAND || hand == EquipmentSlot.HAND) {
                        tempHand = hand == EquipmentSlot.HAND;
                    }
                    ItemStack itemStack = tempHand ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
                    itemStack.setAmount(Math.max(0, itemStack.getAmount() + amount));
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at item-amount action which is expected to be `Section`");
                return Action.empty();
            }
        }, "item-amount");
        registerAction((args, chance) -> {
            int amount;
            EquipmentSlot slot;
            if (args instanceof Integer integer) {
                slot = null;
                amount = integer;
            } else if (args instanceof Section section) {
                slot = Optional.ofNullable(section.getString("slot"))
                        .map(hand -> EquipmentSlot.valueOf(hand.toUpperCase(Locale.ENGLISH)))
                        .orElse(null);
                amount = section.getInt("amount", 1);
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at durability action which is expected to be `Section`");
                return Action.empty();
            }
            return context -> {
                if (Math.random() > chance) return;
                Player player = context.holder();
                if (player == null) return;
                EquipmentSlot tempSlot = slot;
                EquipmentSlot equipmentSlot = context.arg(ContextKeys.SLOT);
                if (equipmentSlot != null) {
                    tempSlot = equipmentSlot;
                }
                if (tempSlot == null) {
                    return;
                }
                ItemStack itemStack = player.getInventory().getItem(tempSlot);
                if (itemStack.getType() == Material.AIR || itemStack.getAmount() == 0)
                    return;
                if (itemStack.getItemMeta() == null)
                    return;
                if (amount > 0) {
                    plugin.getItemManager().decreaseDamage(context.holder(), itemStack, amount);
                } else {
                    plugin.getItemManager().increaseDamage(context.holder(), itemStack, -amount);
                }
            };
        }, "durability");
        registerAction((args, chance) -> {
            if (args instanceof Section section) {
                String id = section.getString("item");
                int amount = section.getInt("amount", 1);
                boolean toInventory = section.getBoolean("to-inventory", false);
                return context -> {
                    if (Math.random() > chance) return;
                    Player player = context.holder();
                    if (player == null) return;
                    ItemStack itemStack = plugin.getItemManager().build(context.holder(), id);
                    if (itemStack != null) {
                        int maxStack = itemStack.getMaxStackSize();
                        int amountToGive = amount;
                        while (amountToGive > 0) {
                            int perStackSize = Math.min(maxStack, amountToGive);
                            amountToGive -= perStackSize;
                            ItemStack more = itemStack.clone();
                            more.setAmount(perStackSize);
                            if (toInventory) {
                                PlayerUtils.giveItem(player, more, more.getAmount());
                            } else {
                                PlayerUtils.dropItem(player, more, true, true, false);
                            }
                        }
                    }
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at give-item action which is expected to be `Section`");
                return Action.empty();
            }
        }, "give-item");
    }

    private void registerMoneyAction() {
        registerAction((args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                if (!VaultHook.isHooked()) return;
                VaultHook.deposit(context.holder(), value.evaluate(context));
            };
        }, "give-money");
        registerAction((args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                if (!VaultHook.isHooked()) return;
                VaultHook.withdraw(context.holder(), value.evaluate(context));
            };
        }, "take-money");
    }

    private void registerPotionAction() {
        registerAction((args, chance) -> {
            if (args instanceof Section section) {
                PotionEffect potionEffect = new PotionEffect(
                        Objects.requireNonNull(PotionEffectType.getByName(section.getString("type", "BLINDNESS").toUpperCase(Locale.ENGLISH))),
                        section.getInt("duration", 20),
                        section.getInt("amplifier", 0)
                );
                return context -> {
                    if (context.holder() == null) return;
                    if (Math.random() > chance) return;
                    context.holder().addPotionEffect(potionEffect);
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at potion-effect action which is expected to be `Section`");
                return Action.empty();
            }
        }, "potion-effect");
    }

    private void registerSoundAction() {
        registerAction((args, chance) -> {
            if (args instanceof Section section) {
                Sound sound = Sound.sound(
                        Key.key(section.getString("key")),
                        Sound.Source.valueOf(section.getString("source", "PLAYER").toUpperCase(Locale.ENGLISH)),
                        section.getDouble("volume", 1.0).floatValue(),
                        section.getDouble("pitch", 1.0).floatValue()
                );
                return context -> {
                    if (context.holder() == null) return;
                    if (Math.random() > chance) return;
                    Audience audience = plugin.getSenderFactory().getAudience(context.holder());
                    AdventureHelper.playSound(audience, sound);
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at sound action which is expected to be `Section`");
                return Action.empty();
            }
        }, "sound");
    }

    private void registerPluginExpAction() {
        registerAction((args, chance) -> {
            if (args instanceof Section section) {
                String pluginName = section.getString("plugin");
                MathValue<Player> value = MathValue.auto(section.get("exp"));
                String target = section.getString("target");
                return context -> {
                    if (context.holder() == null) return;
                    if (Math.random() > chance) return;
                    Optional.ofNullable(plugin.getIntegrationManager().getLevelerProvider(pluginName)).ifPresentOrElse(it -> {
                        it.addXp(context.holder(), target, value.evaluate(context));
                    }, () -> plugin.getPluginLogger().warn("Plugin (" + pluginName + "'s) level is not compatible. Please double check if it's a problem caused by pronunciation."));
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at plugin-exp action which is expected to be `Section`");
                return Action.empty();
            }
        }, "plugin-exp");
    }

    private void registerTitleAction() {
        registerAction((args, chance) -> {
            if (args instanceof Section section) {
                TextValue<Player> title = TextValue.auto(section.getString("title", ""));
                TextValue<Player> subtitle = TextValue.auto(section.getString("subtitle", ""));
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                return context -> {
                    if (Math.random() > chance) return;
                    final Player player = context.holder();
                    if (player == null) return;
                    Audience audience = plugin.getSenderFactory().getAudience(player);
                    AdventureHelper.sendTitle(audience,
                            AdventureHelper.miniMessage(title.render(context)),
                            AdventureHelper.miniMessage(subtitle.render(context)),
                            fadeIn, stay, fadeOut
                    );
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at title action which is expected to be `Section`");
                return Action.empty();
            }
        }, "title");
        registerAction((args, chance) -> {
            if (args instanceof Section section) {
                List<String> titles = section.getStringList("titles");
                if (titles.isEmpty()) titles.add("");
                List<String> subtitles = section.getStringList("subtitles");
                if (subtitles.isEmpty()) subtitles.add("");
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                return context -> {
                    if (context.holder() == null) return;
                    if (Math.random() > chance) return;
                    TextValue<Player> title = TextValue.auto(titles.get(RandomUtils.generateRandomInt(0, titles.size() - 1)));
                    TextValue<Player> subtitle = TextValue.auto(subtitles.get(RandomUtils.generateRandomInt(0, subtitles.size() - 1)));
                    final Player player = context.holder();
                    Audience audience = plugin.getSenderFactory().getAudience(player);
                    AdventureHelper.sendTitle(audience,
                            AdventureHelper.miniMessage(title.render(context)),
                            AdventureHelper.miniMessage(subtitle.render(context)),
                            fadeIn, stay, fadeOut
                    );
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at random-title action which is expected to be `Section`");
                return Action.empty();
            }
        }, "random-title");
    }

    private void registerHologramAction() {
        registerAction(((args, chance) -> {
            if (args instanceof Section section) {
                TextValue<Player> text = TextValue.auto(section.getString("text", ""));
                MathValue<Player> duration = MathValue.auto(section.get("duration", 20));
                boolean position = section.getString("position", "other").equals("other");
                MathValue<Player> x = MathValue.auto(section.get("x", 0));
                MathValue<Player> y = MathValue.auto(section.get("y", 0));
                MathValue<Player> z = MathValue.auto(section.get("z", 0));
                boolean applyCorrection = section.getBoolean("apply-correction", false);
                boolean onlyShowToOne = !section.getBoolean("visible-to-all", false);
                int range = section.getInt("range", 32);
                return context -> {
                    if (context.holder() == null) return;
                    if (Math.random() > chance) return;
                    Player owner = context.holder();
                    Location location = position ? requireNonNull(context.arg(ContextKeys.LOCATION)).clone() : owner.getLocation().clone();
                    location.add(x.evaluate(context), y.evaluate(context), z.evaluate(context));
                    Optional<CustomCropsWorld<?>> optionalWorld = plugin.getWorldManager().getWorld(location.getWorld());
                    if (optionalWorld.isEmpty()) {
                        return;
                    }
                    Pos3 pos3 = Pos3.from(location);
                    if (applyCorrection) {
                        Optional<CustomCropsBlockState> optionalState = optionalWorld.get().getBlockState(pos3);
                        if (optionalState.isPresent()) {
                            if (optionalState.get().type() instanceof CropBlock cropBlock) {
                                CropConfig config = cropBlock.config(optionalState.get());
                                int point = cropBlock.point(optionalState.get());
                                if (config != null) {
                                    int tempPoints = point;
                                    while (tempPoints >= 0) {
                                        Map.Entry<Integer, CropStageConfig> entry = config.getFloorStageEntry(tempPoints);
                                        CropStageConfig stage = entry.getValue();
                                        if (stage.stageID() != null) {
                                            location.add(0, stage.displayInfoOffset(), 0);
                                            break;
                                        }
                                        tempPoints = stage.point() - 1;
                                    }
                                }
                            }
                        }
                    }
                    ArrayList<Player> viewers = new ArrayList<>();
                    if (onlyShowToOne) {
                        if (owner == null) return;
                        viewers.add(owner);
                    } else {
                        for (Player player : owner.getWorld().getPlayers()) {
                            if (LocationUtils.getDistance(player.getLocation(), location) <= range) {
                                viewers.add(player);
                            }
                        }
                    }
                    Component component = AdventureHelper.miniMessage(text.render(context));
                    for (Player viewer : viewers) {
                        HologramManager.getInstance().showHologram(viewer, location, component, (int) (duration.evaluate(context) * 50));
                    }
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at hologram action which is expected to be `Section`");
                return Action.empty();
            }
        }), "hologram");
    }

    private void registerSwingHandAction() {
        registerAction((args, chance) -> {
            boolean arg = (boolean) args;
            return context -> {
                if (context.holder() == null) return;
                if (Math.random() > chance) return;
                SparrowHeart.getInstance().swingHand(context.holder(), arg ? HandSlot.MAIN : HandSlot.OFF);
            };
        }, "swing-hand");
    }

    private void registerForceTickAction() {
        registerAction((args, chance) -> context -> {
            if (context.holder() == null) return;
            if (Math.random() > chance) return;
            Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
            Pos3 pos3 = Pos3.from(location);
            Optional<CustomCropsWorld<?>> optionalWorld = plugin.getWorldManager().getWorld(location.getWorld());
            optionalWorld.ifPresent(world -> world.getChunk(pos3.toChunkPos()).flatMap(chunk -> chunk.getBlockState(pos3)).ifPresent(state -> {
                state.type().randomTick(state, world, pos3);
                state.type().scheduledTick(state, world, pos3);
            }));
        }, "force-tick");
    }
}
