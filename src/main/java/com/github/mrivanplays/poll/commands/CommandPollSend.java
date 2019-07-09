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
package com.github.mrivanplays.poll.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import com.github.mrivanplays.poll.Poll;
import com.github.mrivanplays.poll.question.Question;

public class CommandPollSend implements TabExecutor
{

    private final Poll plugin;

    public CommandPollSend(Poll main)
    {
        plugin = main;
        main.getCommand( "pollsend" ).setExecutor( this );
        main.getCommand( "pollsend" ).setTabCompleter( this );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if ( !sender.hasPermission( "poll.send" ) )
        {
            sender.sendMessage( plugin.color( plugin.getConfig().getString( "messages.no-permission" ) ) );
            return true;
        }
        if ( args.length == 0 || args.length == 1 )
        {
            sender.sendMessage( plugin.color( plugin.getConfig().getString( "messages.pollsend-usage" ) ) );
            return true;
        }
        Optional<Question> questionOpt = plugin.getQuestionHandler().getQuestion( args[1] );
        List<Player> sendTo = new ArrayList<>();
        if ( args[0].equalsIgnoreCase( "all" ) )
        {
            sendTo.addAll( plugin.getServer().getOnlinePlayers() );
        } else
        {
            Player player = plugin.getServer().getPlayer( args[0] );
            if ( player == null )
            {
                sender.sendMessage( plugin.color( plugin.getConfig().getString( "messages.player-not-found" ) ) );
                return true;
            }
            sendTo.add( player );
        }
        if ( !questionOpt.isPresent() )
        {
            sender.sendMessage( plugin.color( plugin.getConfig().getString( "messages.question-not-found" ) ) );
            return true;
        }
        Question question = questionOpt.get();
        List<BaseComponent[]> answersComponents = plugin.getQuestionHandler().getAnswersComponents( question );
        for ( Player player : sendTo )
        {
            player.sendMessage( plugin.color( question.getMessage() ) );
            player.sendMessage( " " );
            for ( BaseComponent[] answer : answersComponents )
            {
                player.spigot().sendMessage( answer );
            }
        }
        sender.sendMessage( plugin.color( plugin.getConfig().getString( "messages.sent-success" ) ) );
        answersComponents.clear();
        sendTo.clear();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        if ( !sender.hasPermission( "poll.send" ) )
        {
            return Collections.emptyList();
        }
        if ( args.length == 1 )
        {
            List<String> matches = plugin.getServer().getOnlinePlayers().parallelStream()
                    .map( HumanEntity::getName ).collect( Collectors.toList() );
            matches.add( "all" );
            return matches.parallelStream()
                    .filter( name -> name.toLowerCase().startsWith( args[0].toLowerCase() ) )
                    .collect( Collectors.toList() );
        }
        if ( args.length == 2 )
        {
            return plugin.getQuestionHandler().getQuestions()
                    .parallelStream()
                    .map( Question::getIdentifier )
                    .filter( identifier -> identifier.toLowerCase().startsWith( args[1].toLowerCase() ) )
                    .collect( Collectors.toList() );
        }
        return Collections.emptyList();
    }
}
