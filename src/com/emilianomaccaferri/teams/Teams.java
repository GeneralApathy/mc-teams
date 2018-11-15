package com.emilianomaccaferri.teams;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.emilianomaccaferri.teams.events.ChatEvents;
import com.emilianomaccaferri.teams.utils.DatabaseUtilities;
import com.mysql.jdbc.Connection;

public class Teams extends JavaPlugin {
	
	public static HashMap<String, String> settings = new HashMap<String, String>();

    public static String driver = "com.mysql.jdbc.Driver";
    public static String url;
    public static String username;
    public static String password;
    public static List<Map<String, Object>> teams, members, invitations;
    public static Connection db;
    
    public static void getInvitationsByUsername(String username){
    	
    	Stream<Map<String, Object>> stream = invitations
    	    	.parallelStream()
    	    	.filter(user -> user.get("username").equals(username));
    	
    	ArrayList<String> results = new ArrayList<String>();
    	
    	Optional<Map<String, Object>> invits = stream.findAny();
    	
    	if(invits.isPresent()) {
    		
    		invits
    		.get()
    		.entrySet()
    		.forEach(i -> {
    			
    			Bukkit.getLogger().info(i.getValue().toString());
    			
    		});
    		
    		
    	}
    	
    }
    
    public static String getTeamNameFromPlayer(String uuid) {
    	
    	Stream<Map<String, Object>> stream = members
    	    	.parallelStream()
    	    	.filter(user -> user.get("uuid").equals(uuid));
    	
    	Optional<Map<String, Object>> user = stream.findFirst();
    	
    	if(user.isPresent()) {
    		
    		Stream<Map<String, Object>> teamStream = teams
        	    	.parallelStream()
        	    	.filter(team -> team.get("team_id").equals(user.get().get("team_id").toString()));
    		
    		Optional<Map<String, Object>> teamID = teamStream.findFirst();
    		
    		if(teamID.isPresent()) {
    			
    			return teamID.get().get("team_name").toString();
    			
    		}
    		
    		return null;
    		
    	}
    	
    	return null;
    	
    }
    
    public static String getTeamIdFromPlayer(String uuid) {
    	
    	Stream<Map<String, Object>> stream = members
    	    	.parallelStream()
    	    	.filter(user -> user.get("uuid").equals(uuid));
    	
    	Optional<Map<String, Object>> user = stream.findFirst();
    	
    	if(user.isPresent()) {
    		
    		Stream<Map<String, Object>> teamStream = teams
        	    	.parallelStream()
        	    	.filter(team -> team.get("team_id").equals(user.get().get("team_id").toString()));
    		
    		Optional<Map<String, Object>> teamID = teamStream.findFirst();
    		
    		if(teamID.isPresent()) {
    			
    			return teamID.get().get("team_id").toString();
    			
    		}
    		
    		return null;
    		
    	}
    	
    	return null;
    	
    }
    
    public static String getUserid(String uuid) {
    	
    	Stream<Map<String, Object>> stream = members
    	    	.parallelStream()
    	    	.filter(user -> user.get("uuid").equals(uuid));
    	
    	Optional<Map<String, Object>> user = stream.findFirst();
    	
    	if(user.isPresent())
    		return user.get().get("userid").toString();
    	
    	return null;
    	
    }
    
    public static boolean playerHasTeamByUsername(String username) {
    	
    	Stream<Map<String, Object>> stream = members
    	.parallelStream()
    	.filter(user -> user.get("username").equals(username));

    	Optional<Map<String, Object>> isMemberOfATeam = stream.findFirst();
    	
    	if(isMemberOfATeam.isPresent())
    		return true;
    	
    	return false;
    	
    }
    
    public static boolean playerHasTeam(String uuid) {
    	
    	Stream<Map<String, Object>> stream = members
    	.parallelStream()
    	.filter(user -> user.get("uuid").equals(uuid));
    	
    	if(Stream.of(stream).count() == 0)
    		return false;
    		
    	
    	Optional<Map<String, Object>> isMemberOfATeam = stream.findFirst();
    	
    	if(isMemberOfATeam.isPresent())
    		return true;
    	
    	return false;
    	
    }
    
    public static void refreshTeams() throws SQLException {
    	
    	teams = DatabaseUtilities.query(db, "SELECT * FROM Teams", new ArrayList<Object>());
    	
    }
    
    public static void refreshInvitations() throws SQLException{
    	
    	invitations = DatabaseUtilities.query(db, "SELECT * FROM Invitations", new ArrayList<Object>());
    	
    }
	
    public static void refreshMembers() throws SQLException {
    	
    	members = DatabaseUtilities.query(db, "SELECT * FROM Members", new ArrayList<Object>());
    	
    }
    
	public void onEnable() {
		
		saveDefaultConfig();
		
		username = getConfig().getString("mysql-username");
		password = getConfig().getString("mysql-password");
		url = "jdbc:mysql://localhost:3306/" + getConfig().getString("mysql-database");;
		
		try {
			
			db = (Connection) DatabaseUtilities.createConnection(driver, url, username, password);
			//DatabaseUtilities.update(db, "DELETE FROM Invitations", new ArrayList<Object>());
			teams = DatabaseUtilities.query(db, "SELECT * FROM Teams", new ArrayList<Object>());
			members = DatabaseUtilities.query(db, "SELECT * FROM Members", new ArrayList<Object>());
			invitations = DatabaseUtilities.query(db, "SELECT * FROM Invitations", new ArrayList<Object>());
			
			getLogger().info("Loaded teams: " + teams.toString());
			getLogger().info("Loaded members: " +members.toString());
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			getLogger().warning("[Teams] MySQL error.");
		}
		
		settings.put("show-prefixes", String.valueOf(getConfig().getBoolean("show-prefixes")));
		settings.put("team-prefix", getConfig().getString("team-prefix"));
		
		getCommand("teams").setExecutor(new Commands(this));
		Bukkit.getServer().getPluginManager().registerEvents(new ChatEvents(this), this);
		
	}

}
