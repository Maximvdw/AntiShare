package com.turt2live.antishare.conversations.region;

import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.turt2live.antishare.ASUtils;
import com.turt2live.antishare.conversations.ASMenu;

public class EditRegionMenu extends ASMenu {

	public EditRegionMenu(){
		//super("events",
		//		"/events");
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, String input){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void displayMenu(Conversable target){
		ASUtils.sendToConversable(target, ChatColor.DARK_GREEN + "=======[ " + ChatColor.GREEN + "Edit Region" + ChatColor.DARK_GREEN + " ]=======");
		//ASUtils.sendToConversable(target, ChatColor.DARK_AQUA + "events" + ChatColor.GOLD + " - " + ChatColor.AQUA + "Edit the blocked lists");
	}

}