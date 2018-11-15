package com.emilianomaccaferri.teams.events;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.emilianomaccaferri.teams.Teams;

public class ChatEvents implements Listener {
	
	Teams plugin = null;
	
	public ChatEvents(Teams plugin) {
		
		this.plugin = plugin;
		
	}
	
	@EventHandler
	public void playerJoinEvent(PlayerJoinEvent e) {
		
		Player player = e.getPlayer();
		
		Teams.getInvitationsByUsername(player.getName());
		
	}
	
	@EventHandler
	public void playerChatEvent(AsyncPlayerChatEvent e) {
		
		if(Boolean.valueOf(Teams.settings.get("show-prefixes"))) {
			
			Stream<Map<String, Object>> stream = Teams.members
					.parallelStream()
					.filter(item -> item.get("uuid").equals(e.getPlayer().getUniqueId().toString()));
			
			Optional<Map<String, Object>> user = stream.findFirst();
			
			if(user.isPresent()) {
				
				Stream<Map<String, Object>> teams = Teams.teams
						.parallelStream()
						.filter(item -> item.get("team_id").equals(user.get().get("team_id")));
				
				
				Optional<Map<String, Object>> team = teams.findFirst();
				e.setFormat(Teams.settings.get("team-prefix").replaceAll("%team%", team.get().get("team_name").toString()) + "%s: %s");
				
			}else {
				
				e.setFormat("%s: %s");
				
			}
			
		}
		
	}
	
}
