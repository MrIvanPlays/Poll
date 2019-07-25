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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class CommandPollVotes implements TabExecutor {

  private final Poll plugin;

  public CommandPollVotes(Poll main) {
    plugin = main;
    main.getCommand("pollvotes").setExecutor(this);
    main.getCommand("pollvotes").setTabCompleter(this);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(
          Poll.COLORS.apply(plugin.getConfig().getString("messages.pollvotes-usage")));
      return true;
    }
    Optional<Question> questionOptional = plugin.getQuestionHandler().getQuestion(args[0]);
    if (!questionOptional.isPresent()) {
      sender.sendMessage(
          Poll.COLORS.apply(plugin.getConfig().getString("messages.question-not-found")));
      return true;
    }
    Question question = questionOptional.get();
    sender.sendMessage(
        Poll.COLORS.apply(
            plugin.getConfig().getString("messages.votes-for").replace("%question%", args[0])));
    for (String answer : question.getValidAnswers()) {
      String replaceColorCode = Poll.ANSWERS.apply(answer);
      long votes =
          question
              .getAnswered()
              .parallelStream()
              .filter(
                  voter ->
                      voter
                          .getAnswered()
                          .toLowerCase()
                          .equalsIgnoreCase(replaceColorCode.toLowerCase()))
              .count();
      String message =
          Poll.COLORS.apply(
              plugin
                  .getConfig()
                  .getString("messages.votes-message")
                  .replace("%answer%", answer)
                  .replace("%votes%", Long.toString(votes)));
      sender.sendMessage(message);
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    if (args.length == 1) {
      return plugin
          .getQuestionHandler()
          .getQuestions()
          .parallelStream()
          .map(Question::getIdentifier)
          .filter(identifier -> identifier.toLowerCase().startsWith(args[0].toLowerCase()))
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }
}
