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
package com.github.mrivanplays.poll.question;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.github.mrivanplays.poll.Poll;

@RequiredArgsConstructor
public class QuestionAnnouncer {

    private final Poll plugin;
    private BukkitTask task;
    private int count = 0;

    private void sendMessage(Player player, Question question) {
        if (!question.getAnswer(player.getUniqueId()).isPresent()) {
            player.sendMessage(plugin.color(question.getMessage()));
            player.sendMessage(" ");
            for (BaseComponent[] answer : plugin.getQuestionHandler().getAnswersComponents(question)) {
                player.spigot().sendMessage(answer);
            }
        }
    }

    public void loadAsAnnouncements() {
        if (!plugin.getConfig().getBoolean("question-announcer.enable")) {
            return;
        }
        int interval = plugin.getConfig().getInt("question-announcer.interval");
        if (interval > 0) {
            task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (plugin.getConfig().getBoolean("question-announcer.random-order")) {
                    List<Question> questionList = plugin.getQuestionHandler().getQuestions();
                    Question question = questionList.get(ThreadLocalRandom.current().nextInt(0, questionList.size()));
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        sendMessage(player, question);
                    }
                } else {
                    Question question = plugin.getQuestionHandler().getQuestions().get(count);
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        sendMessage(player, question);
                    }
                    count++;
                    if (count + 1 > plugin.getQuestionHandler().getQuestions().size()) {
                        count = 0;
                    }
                }
            }, 0, interval * 20);
        }
    }

    public void reloadAnnouncements() {
        if (task != null) {
            task.cancel();
        }
        if (plugin.getConfig().getBoolean("question-announcer.enable")) {
            loadAsAnnouncements();
        }
    }
}
