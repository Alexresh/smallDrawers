package ru.flightcraft;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.flightcraft.util.EventRegistry;

public class Main implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("small-drawers");

	@Override
	public void onInitialize() {

		AttackBlockCallback.EVENT.register((EventRegistry::attackBlock));

		UseEntityCallback.EVENT.register(EventRegistry::useEntity);

		AttackEntityCallback.EVENT.register((EventRegistry::attackEntity));
	}



}