package com.emilianomaccaferri.teams;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.emilianomaccaferri.teams.utils.DatabaseUtilities;


public class Commands implements CommandExecutor {

	Teams plugin;
	
	public Commands(Teams plugin) {
		
		this.plugin = plugin;
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			
			sender.sendMessage("[Teams] Devi essere un giocatore");
			return true;
			
		}
		
		Player player = (Player) sender;
		
		if(cmd.getName().equalsIgnoreCase("teams")) {
			
			if(args.length == 0) {
				
				player.sendMessage(
						ChatColor.YELLOW + "" + ChatColor.BOLD + "== Comandi disponibili == \n " + ChatColor.RESET + ChatColor.GREEN + "- /teams create <nome_team> - Crea un nuovo team\n"+
						"- /teams join <nome_team> - Entra in un team (solo su invito)\n" + 
						"- /teams invite <nome_player> - Invita un player nel tuo team\n" + 
						"- /teams leave - Esci dal tuo team\n"
				);
				
				return true;
				
			}
			
			if(args[0].equalsIgnoreCase("leave")) {
				
				if(!Teams.playerHasTeam(player.getUniqueId().toString())) {
					
					player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.YELLOW + "Non sei in alcun team, da dove cazzo te ne vai");
					return true;
					
				}
				
				ArrayList<Object> pl = new ArrayList<Object>();
				pl.add(player.getUniqueId().toString());
				try {
					DatabaseUtilities.update(Teams.db, "DELETE FROM Members WHERE uuid = ?", pl);
					Teams.refreshMembers();
					
					player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.GREEN + "Sei uscito dal tuo team. Ciao!");
					
					return true;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			if(args[0].equalsIgnoreCase("join")) {
				
				if(args.length == 1) {
					
					player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.GREEN + "/teams join <nome_team> - Entra nel team specificato (solo su invito)");
					return true;
					
				}	
				
				if(Teams.playerHasTeam(player.getUniqueId().toString())) {
					
					player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.YELLOW + "Fai già parte di un team, esci da quello prima di joinare in un nuovo team");
					return true;
					
				}
				
				if(Teams.getInvitationsByUsername(player.getName()) == null) {
					
					player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.YELLOW + "Non c'è nessun invito per te, mi dispiace :(");
					return true;
					
				}
				
				if(Teams.getInvitationsByUsername(player.getName()).contains(args[1])) {
					
					try {
						
						Bukkit.getLogger().info("ID: " + Teams.getTeamIdByItsName(args[1]));
						
						addPlayerToTeam(player.getUniqueId().toString(), player.getName(), Teams.getTeamIdByItsName(args[1]));
						ArrayList<Object> p = new ArrayList<Object>();
						
						p.add(player.getName());
						DatabaseUtilities.update(Teams.db, "DELETE FROM Invitations WHERE username = ?", p);
						Teams.refreshInvitations();
						Teams.refreshMembers();
						Teams.refreshMembers();
						
						player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.YELLOW + "Sei entrato nel team " + ChatColor.GREEN + args[1]);
						return true;
						
					} catch (SQLException e) {
						
						e.printStackTrace();
					}
					
				}else {
					
					player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.YELLOW + "Non sei stato invitato dal team " + ChatColor.GREEN + args[1]);
					return true;
					
				}
				
			}
			
			if(args[0].equalsIgnoreCase("invite")) {
				
				if(args.length == 1) {
					
					player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.GREEN + " /teams invite <nome_player> - Invita un player nel tuo team");
					return true;
					
				}	
								
				if(!Teams.playerHasTeam(player.getUniqueId().toString())) {
					
					player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.RED + " Non sei all'interno di alcun team!");
					return true;
					
				}
				
				Player invitatedPlayer = Bukkit.getPlayer(args[1]);				
				ArrayList<Object> invitation = new ArrayList<Object>();
				
				try {
					
					if(Teams.playerHasTeamByUsername(args[1])) {
							
							player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.RED + " Il player " + args[1] + " fa già parte di un team");
							return true;
							
					}
					
					Stream<Map<String, Object>> stream = Teams.invitations
					.parallelStream()
					.filter(item -> item.get("team_id") == Teams.getTeamIdFromPlayer(player.getUniqueId().toString()) && item.get("username").toString().equalsIgnoreCase(args[1]));
					
					Optional<Map<String, Object>> team = stream.findFirst();
					
					if(team.isPresent()) {
						
						player.sendMessage(ChatColor.YELLOW +""+ ChatColor.BOLD + "[Teams] " + ChatColor.RESET + ChatColor.RED + " Hai già invitato una volta quel giocatore.");
						return true;
						
					}
					
					invitation.add(Teams.getTeamIdFromPlayer(player.getUniqueId().toString()));
					invitation.add(args[1]);
					DatabaseUtilities.update(Teams.db, "INSERT INTO Invitations (team_id, username) VALUES(?,?)", invitation);
					Teams.refreshInvitations();
					player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.RED + "Il player " + ChatColor.WHITE + ChatColor.BOLD + args[1] + ChatColor.RESET + ChatColor.RED + " è stato invitato");
					if(invitatedPlayer != null && invitatedPlayer.isOnline()) {
						
						if(invitatedPlayer.equals(player)) {
							
							invitatedPlayer.sendMessage(ChatColor.YELLOW +""+ ChatColor.BOLD + "[Teams] " + ChatColor.RESET + ChatColor.RED + " Non puoi invitare te stesso.");
							return true;
							
						}
						
						invitatedPlayer.sendMessage(ChatColor.YELLOW +""+ ChatColor.BOLD + "[Teams] " + ChatColor.RESET + player.getName() + " ti ha invitato nel team " + Teams.getTeamNameFromPlayer(player.getUniqueId().toString()) + "\nFai "+ChatColor.GREEN+"/teams join " + Teams.getTeamNameFromPlayer(player.getUniqueId().toString()) + ChatColor.RESET + " per accettare l'invito");
						
					}else {
						
						player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.YELLOW + "Il player " + ChatColor.WHITE + ChatColor.BOLD + args[1] + ChatColor.RESET + ChatColor.YELLOW + " sembrerebbe non essere online... Lo avviserò non appena ritorna.");
						
					}
					return true;
					
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					
					e.printStackTrace();
					
				}
				
			}
			
			if(args[0].equalsIgnoreCase("create")) {
				
				if(args.length == 1) {
					
					player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.GREEN + " /teams create <nome_team> - Crea un nuovo team");
					return true;
					
				}		
				
				Stream<Map<String, Object>> stream = Teams.teams
				.parallelStream()
				.filter(item -> item.get("team_name").equals(args[1]));
				
				Optional<Map<String, Object>> team = stream.findFirst();
				
				if(team.isPresent()) {
					
					player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.RED + "Il team " + ChatColor.WHITE + ChatColor.BOLD + args[1] + ChatColor.RESET + ChatColor.RED + " esiste già");
					return true;
					
				}else {
					
					try {
						
						if(Teams.playerHasTeam(player.getUniqueId().toString())) {
							
							player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.RED + "Fai già parte di un team, pertanto non ne puoi creare un altro.");
							return true;
							
						}
						
						String team_id = RandomStringUtils.random(128, false, true);
						ArrayList<Object> tt = new ArrayList<Object>();
						tt.add(args[1]);
						tt.add(team_id);
						
						DatabaseUtilities.update(Teams.db, "INSERT INTO Teams (team_name, team_id) VALUES(?,?)", tt);
						addPlayerToTeam(player.getUniqueId().toString(), player.getName(), team_id);
						
						Teams.refreshTeams();
						Teams.refreshMembers();
						
					
						player.sendMessage("[Teams] " + ChatColor.RESET + ChatColor.GREEN + "Il team " + ChatColor.WHITE + ChatColor.BOLD + args[1] + ChatColor.RESET + ChatColor.GREEN + " è stato creato");
						
						return true;
						
					} catch (SQLException e) {
						
						e.printStackTrace();
					}
					
				}
				
			}
			
		}
		
		return true;
		
	}
	
	public void addPlayerToTeam(String uuid, String playerName, String team_id) throws SQLException {
		
		String userid = RandomStringUtils.random(64, false, true);
		ArrayList<Object> member = new ArrayList<Object>();
		
		member.add(uuid);
		member.add(playerName);
		member.add(userid);
		member.add(team_id);

		DatabaseUtilities.update(Teams.db, "INSERT INTO Members (uuid, username, userid, team_id) VALUES(?,?,?,?)", member);
		
	}
	
}
