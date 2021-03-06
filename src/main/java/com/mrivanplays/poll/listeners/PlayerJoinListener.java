/*
    Copyright (c) 2019 Ivan Pekov
    Copyright (c) 2019 Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
package com.mrivanplays.poll.listeners;

import com.mrivanplays.poll.Poll;
import com.mrivanplays.poll.question.Question;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

  private Poll plugin;

  public PlayerJoinListener(Poll plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void handle(PlayerJoinEvent event) {
    if (plugin.getConfig().getBoolean("send-questions-only-on-join")) {
      int delay = plugin.getConfig().getInt("on-join-questions-delay");
      if (delay == 0) {
        handle(event.getPlayer());
      } else {
        plugin
            .getServer()
            .getScheduler()
            .scheduleSyncDelayedTask(plugin, () -> handle(event.getPlayer()), delay * 20);
      }
    }
  }

  private void handle(Player player) {
    for (Question question : plugin.getQuestionHandler().getQuestions()) {
      if (question.getAnswer(player.getUniqueId()).isPresent()) {
        continue;
      }
      plugin.getQuestionHandler().send(player, question);
    }
  }
}
