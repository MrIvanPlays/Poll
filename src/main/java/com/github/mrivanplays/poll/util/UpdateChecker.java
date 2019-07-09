/*
 * Copyright 2019 Ivan Pekov (MrIvanPlays)

 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **/
package com.github.mrivanplays.poll.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class UpdateChecker implements Listener
{

    private URL url;
    private final JavaPlugin plugin;
    private final int projectId;
    private final String permission;

    public UpdateChecker(JavaPlugin plugin, int projectId, String permission)
    {
        this.plugin = plugin;
        this.projectId = projectId;
        this.permission = permission;
        try
        {
            url = new URL( "https://api.spigotmc.org/legacy/update.php?resource=" + projectId );
        } catch ( IOException e )
        {
            plugin.getLogger().severe( "Could not connect to spigot: servers are down; there is a maintenance; other reason" );
        }
    }

    private String getNewVersion()
    {
        String newVersion = "";
        try
        {
            URLConnection connection = url.openConnection();
            newVersion = new BufferedReader( new InputStreamReader( connection.getInputStream() ) ).readLine();
        } catch ( IOException e )
        {
            plugin.getLogger().warning( "Could not gain current plugin version" );
        }
        return newVersion;
    }

    private String getOldVersion()
    {
        return plugin.getDescription().getVersion();
    }

    private String getResourceUrl()
    {
        return "https://spigotmc.org/resources/" + projectId;
    }

    private boolean updateAvailable()
    {
        return !getOldVersion().equalsIgnoreCase( getNewVersion() );
    }

    public void fetch()
    {
        plugin.getServer().getScheduler().runTaskAsynchronously( plugin, () ->
        {
            if ( updateAvailable() )
            {
                plugin.getLogger().warning( "Stable version: " + getNewVersion() + " is out! You're still running version: " + getOldVersion() );
                plugin.getLogger().warning( "Download the newest version on: " + getResourceUrl() );
                plugin.getServer().getPluginManager().registerEvents( UpdateChecker.this, plugin );
            }
        } );
        checkUpdates();
    }

    private void checkUpdates()
    {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously( plugin, () ->
        {
            if ( updateAvailable() )
            {
                plugin.getLogger().info( "Proceeding update check..." );
                plugin.getLogger().warning( "Stable version: " + getNewVersion() + " is out! You're still running version: " + getOldVersion() );
                plugin.getLogger().warning( "Download the newest version on: " + getResourceUrl() );
                for ( Player online : plugin.getServer().getOnlinePlayers() )
                {
                    if ( online.hasPermission( permission ) )
                    {
                        message( online );
                    }
                }
            } else
            {
                plugin.getLogger().info( "Proceeding update check..." );
                plugin.getLogger().info( "Up to date!" );
            }
        }, 20 * 60 * 30, 20 * 60 * 30 );
    }

    @EventHandler
    public void notifyUpdate(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if ( player.hasPermission( permission ) )
        {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask( plugin, () -> message( player ), 100 );
        }
    }

    private void message(Player player)
    {
        ComponentBuilder baseMessage = new ComponentBuilder( "Update found for " + plugin.getName() + " . \n" ).color( ChatColor.GRAY );
        BaseComponent[] versionMessage = TextComponent.fromLegacyText( color( "&7- &cOld version (current): "
                + getOldVersion() + " &7; &aNew version: " + getNewVersion() + "\n" ) );
        baseMessage.append( versionMessage );
        TextComponent downloadCommand = new TextComponent( "- Download via clicking " );
        downloadCommand.setColor( ChatColor.GRAY );
        TextComponent clickLink = new TextComponent( "here" );
        clickLink.setColor( ChatColor.AQUA );
        clickLink.setBold( true );
        clickLink.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, getResourceUrl() ) );
        BaseComponent[] hoverMessage = new ComponentBuilder( "Click here to be redirect to download page" ).color( ChatColor.DARK_GREEN ).create();
        clickLink.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, hoverMessage ) );
        downloadCommand.addExtra( clickLink );
        downloadCommand.addExtra( color( "&7 ." ) );
        baseMessage.append( downloadCommand );
        player.spigot().sendMessage( baseMessage.create() );
    }

    private String color(String text)
    {
        return ChatColor.translateAlternateColorCodes( '&', text );
    }
}
