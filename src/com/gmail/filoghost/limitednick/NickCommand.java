/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.limitednick;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import wild.api.command.CommandFramework;

import com.gmail.filoghost.limitednick.sql.SQLManager;
import com.gmail.filoghost.limitednick.sql.SQLTask;

public class NickCommand extends CommandFramework {
	
	private static final int MAX_EXTRA_CHARS = 8;
	
	
	public NickCommand() {
		super(LimitedNick.plugin, "nick");
	}

	
	@Override
	public void execute(final CommandSender sender, String label, String[] args) {
		
		if (args.length == 0) {
			sender.sendMessage("§a/nick <nickname> §7- Imposta il nickname.");
			sender.sendMessage("§a/nick off §7- Disattiva il nickname.");
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("off")) {
			
			// Disattiva il nick per gli altri
			if (args.length > 1) {
				CommandValidate.isTrue(sender.hasPermission(Perms.OFF_OTHERS), "Non hai il permesso per rimuovere il nick degli altri.");
				final String targetName = args[1];
				Player target = Bukkit.getPlayerExact(targetName);
				
				if (target != null) {
					target.setDisplayName(target.getName());
				}
				new SQLTask() {
					public void execute() throws SQLException {
						String nick = SQLManager.getNick(targetName);
						if (nick != null) {
							SQLManager.removeNick(targetName);
							sender.sendMessage(ChatColor.GREEN + "Hai rimosso il nickname al giocatore " + targetName + ".");
						} else {
							sender.sendMessage(ChatColor.RED + "Il giocatore " + targetName + " non ha un nickname.");
						}
					}
				}.submitAsync(sender);
				
				return;
			}
			
			CommandValidate.isTrue(sender.hasPermission(Perms.NICK_COMMAND), "Solo i VIP possono usare questo comando.");
			Player player = CommandValidate.getPlayerSender(sender);
			final String playerName = player.getName();
			
			player.setDisplayName(player.getName());
			player.sendMessage("§eNon disponi più di un nickname.");
			
			new SQLTask() {
				public void execute() throws SQLException {
					SQLManager.removeNick(playerName);
				}
			}.submitAsync(player);
			return;
		}
		
		
		CommandValidate.isTrue(sender.hasPermission(Perms.NICK_COMMAND), "Solo i VIP possono usare questo comando.");
		Player player = CommandValidate.getPlayerSender(sender);
		final String playerName = player.getName();
		
		if (args.length > 1) {
			player.sendMessage(ChatColor.RED + "Troppi argomenti. Utilizzo: /nick <nickname>");
			return;
		}
		
		
		String potentialNick = args[0];
		
		CommandValidate.isTrue(potentialNick.matches("^[a-zA-Z0-9\u00a7&_\\-]+$"), "Puoi usare solo lettere, numeri, colori e trattini (- e _).");
		
		potentialNick = colorize(potentialNick);
		
		if (!player.hasPermission(Perms.EXTRA_CHARS)) {
			CommandValidate.isTrue(ChatColor.stripColor(potentialNick).equalsIgnoreCase(playerName), "Il tuo nickname (§f" + potentialNick + "§c) deve essere identico al tuo username (§f" + playerName + "§c), eccetto i colori.");
		}
		
		
		CommandValidate.isTrue(ChatColor.stripColor(potentialNick.toLowerCase()).contains(playerName.toLowerCase()), "Il tuo nickname (§f" + potentialNick + "§c) deve contenere per intero il tuo username (§f" + playerName +"§c).");
		CommandValidate.isTrue(ChatColor.stripColor(potentialNick).length() <= player.getName().length() + MAX_EXTRA_CHARS, "Il nickname è troppo lungo.");
		CommandValidate.isTrue(potentialNick.length() < 48, "Il nickname è troppo lungo (contando anche i colori).");
		
		player.setDisplayName(ChatColor.GRAY + "~" + ChatColor.RESET + potentialNick + ChatColor.RESET);
		player.sendMessage("Il tuo nickname ora è§f " + potentialNick);
		
		final String finalNick = potentialNick;
		
		new SQLTask() {
			public void execute() throws SQLException {
				SQLManager.setNick(playerName, finalNick);
			}
		}.submitAsync(player);
	}
	
	private String colorize(String textToTranslate) {
		char[] b = textToTranslate.toCharArray();
		for (int i = 0; i < b.length - 1; i++) {
			if ((b[i] == '&') && ("0123456789AaBbCcDdEeFf".indexOf(b[(i + 1)]) > -1)) {
				b[i] = '§';
				b[(i + 1)] = Character.toLowerCase(b[(i + 1)]);
			}
		}
		return new String(b);
	}

}
