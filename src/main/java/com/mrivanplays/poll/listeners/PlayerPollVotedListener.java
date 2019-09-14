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
import com.mrivanplays.poll.events.PlayerPollVotedEvent;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerPollVotedListener implements Listener {

  private Poll plugin;

  public PlayerPollVotedListener(Poll plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void handle(PlayerPollVotedEvent event) {
    if (plugin.getConfig().getBoolean("awards.enabled")) {
      List<String> commands = plugin.getConfig().getStringList("awards.commands");
      for (String command : commands) {
        plugin
            .getServer()
            .dispatchCommand(
                plugin.getServer().getConsoleSender(),
                command
                    .replace("%player%", event.getPlayer().getName())
                    .replace("%answer%", event.getAnswer())
                    .replace("%questionId%", event.getQuestion().getIdentifier()));
      }
    }
  }
}
