/*
 * Copyright 2019 Ivan Pekov (MrIvanPlays)
 * Copyright 2019 contributors

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

import com.github.mrivanplays.poll.Poll;
import com.github.mrivanplays.poll.question.Question;
import com.google.common.base.Joiner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandPoll implements TabExecutor {

    private final Poll plugin;

    public CommandPoll(Poll main) {
        plugin = main;
        main.getCommand("poll").setExecutor(this);
        main.getCommand("poll").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args.length == 2) {
            sender.sendMessage(plugin.color(plugin.getConfig().getString("messages.usage")));
            return true;
        }
        // /poll reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("poll.reload")) {
                sender.sendMessage(plugin.color(plugin.getConfig().getString("messages.no-permission")));
                return true;
            }
            plugin.reload();
            sender.sendMessage(plugin.color(plugin.getConfig().getString("messages.reload-message")));
            return true;
        }
        // /cmd   1          2                   3
        // /poll vote <question identifier> <question answer>
        if (args.length == 1 && args[0].equalsIgnoreCase("vote")) {
            sender.sendMessage(plugin.color(plugin.getConfig().getString("messages.usage")));
            return true;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("vote")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.color(plugin.getConfig().getString("messages.no-console")));
                return true;
            }
            Player player = (Player) sender;
            String questionIdentifier = args[1];
            String answer = args[2];
            Optional<Question> questionOpt = plugin.getQuestionHandler().getQuestion(questionIdentifier);
            if (!questionOpt.isPresent()) {
                player.sendMessage(plugin.color(plugin.getConfig().getString("messages.question-not-found")));
                return true;
            }
            Question question = questionOpt.get();
            if (question.getAnswer(player.getUniqueId()).isPresent()) {
                player.sendMessage(plugin.color(plugin.getConfig().getString("messages.already-voted")));
                return true;
            }
            Question questionDup = question.duplicate();
            List<String> validAnswersCopy = question.getValidAnswers().parallelStream()
                    .map(validAns -> Poll.ANSWER_FUNCTION.apply(validAns))
                    .collect(Collectors.toList());
            if (validAnswersCopy
                    .parallelStream()
                    .noneMatch(validAnswer -> validAnswer.toLowerCase().equalsIgnoreCase(answer.toLowerCase()))) {
                player.sendMessage(plugin.color(plugin.getConfig().getString("messages.invalid-answer")
                        .replace("%answers%", Joiner.on(", ").join(question.getValidAnswers()))));
                return true;
            }
            questionDup.addAnswer(player.getUniqueId(), answer);
            plugin.getQuestionHandler().modify(questionDup);
            player.sendMessage(plugin.color(plugin.getConfig().getString("messages.vote-success")));
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> matches = new ArrayList<>(Collections.singleton("vote"));
            if (sender.hasPermission("poll.reload")) {
                matches.add("reload");
            }
            return matches;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("vote")) {
            return plugin.getQuestionHandler().getQuestions()
                    .stream()
                    .parallel()
                    .map(Question::getIdentifier)
                    .filter(identifier -> identifier.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("vote")) {
            Optional<Question> question = plugin.getQuestionHandler().getQuestion(args[1]);
            if (question.isPresent()) {
                return question.get().getValidAnswers()
                        .parallelStream()
                        .map(answer -> Poll.ANSWER_FUNCTION.apply(answer))
                        .filter(answer -> answer.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
