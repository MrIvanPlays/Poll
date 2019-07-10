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
package com.github.mrivanplays.poll;

import com.github.mrivanplays.poll.commands.CommandPoll;
import com.github.mrivanplays.poll.commands.CommandPollSend;
import com.github.mrivanplays.poll.commands.CommandPollVotes;
import com.github.mrivanplays.poll.question.QuestionAnnouncer;
import com.github.mrivanplays.poll.question.QuestionHandler;
import com.github.mrivanplays.poll.storage.SerializableQuestion;
import com.github.mrivanplays.poll.storage.SerializableQuestions;
import com.github.mrivanplays.poll.storage.VotersFile;
import com.github.mrivanplays.poll.util.MetricsSetup;
import com.github.mrivanplays.poll.util.UpdateCheckerSetup;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

public final class Poll extends JavaPlugin {

    @Getter
    private VotersFile votersFile;
    @Getter
    private QuestionHandler questionHandler;
    private QuestionAnnouncer announcer;

    public static Function<String, String> ANSWER_FUNCTION = answer -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', answer));

    @Override
    public void onEnable() {
        saveDefaultConfig();
        votersFile = new VotersFile(getDataFolder());
        if (!votersFile.deserialize().isEmpty()) {
            for (SerializableQuestion question : votersFile.deserialize()) {
                SerializableQuestions.register(question);
            }
        }
        if (!getServer().getVersion().contains("Spigot") && getServer().getVersion().contains("CraftBukkit")) {
            getLogger().severe("CraftBukkit is not supported in this plugin!");
            getLogger().severe("You need to change your server software at least to Spigot");
            getLogger().severe("Plugin disabled");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        questionHandler = new QuestionHandler(this);
        new CommandPoll(this);
        new CommandPollVotes(this);
        new CommandPollSend(this);
        announcer = new QuestionAnnouncer(this);
        announcer.loadAsAnnouncements();
        new MetricsSetup(this).setup();
        getLogger().info("Plugin enabled");
        // The task is executed on another thread
        // Starts after 5 minutes and repeats every 30 minutes
        // Can't harm the server while saving things into a file.
        getServer().getScheduler().runTaskTimerAsynchronously(this, () ->
                votersFile.serialize(SerializableQuestions.getForSerialize()), 300 * 20, 600 * 3 * 20);
        new UpdateCheckerSetup(this, "poll.updatenotify").setup();
    }

    @Override
    public void onDisable() {
        votersFile.serialize(SerializableQuestions.getForSerialize());
        getLogger().info("Plugin disabled");
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public void reload() {
        reloadConfig();
        announcer.reloadAnnouncements();
        votersFile.serialize(SerializableQuestions.getForSerialize());
    }
}
