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

import com.github.mrivanplays.poll.Poll;
import com.github.mrivanplays.poll.storage.SerializableQuestion;
import com.github.mrivanplays.poll.storage.SerializableQuestions;
import com.github.mrivanplays.poll.util.Voter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.file.FileConfiguration;

@RequiredArgsConstructor
public class QuestionHandler {

  private final Poll plugin;

  public List<Question> getQuestions() {
    List<Question> questions = new ArrayList<>();
    FileConfiguration config = plugin.getConfig();
    for (String key : config.getConfigurationSection("questions").getKeys(false)) {
      String questionMessage = config.getString("questions." + key + ".question");
      List<String> answers = config.getStringList("questions." + key + ".answers");
      Question question = new Question(key, questionMessage, answers);
      List<SerializableQuestion> serializableQuestions = SerializableQuestions.getForSerialize();
      if (!serializableQuestions.isEmpty()) {
        for (SerializableQuestion serQ : serializableQuestions) {
          if (serQ.getQuestionIdentifier().equalsIgnoreCase(question.getIdentifier())) {
            for (Voter voter : serQ.getVoters()) {
              question.addAnswer(voter);
            }
          }
        }
      } else {
        SerializableQuestions.register(
            new SerializableQuestion(question.getIdentifier(), question.getAnswered()));
      }
      questions.add(question);
    }
    return questions;
  }

  public void modify(Question question) {
    Optional<SerializableQuestion> serializableOpt = SerializableQuestions.getEquivalent(question);
    if (!serializableOpt.isPresent()) {
      SerializableQuestions.register(
          new SerializableQuestion(question.getIdentifier(), question.getAnswered()));
      return;
    }
    SerializableQuestion serializable = serializableOpt.get();
    SerializableQuestion dupeSerializable = serializable.duplicate();
    dupeSerializable.setVoters(question.getAnswered());
    SerializableQuestions.replace(serializable, dupeSerializable);
  }

  public List<BaseComponent[]> getAnswersComponents(Question question) {
    List<BaseComponent[]> answersComponents = new ArrayList<>();
    for (String message : question.getValidAnswers()) {
      String transform = Poll.ANSWERS.apply(message);
      BaseComponent[] component =
          new ComponentBuilder(Poll.COLORS.apply(message))
              .event(
                  new HoverEvent(
                      HoverEvent.Action.SHOW_TEXT,
                      TextComponent.fromLegacyText(
                          Poll.COLORS.apply(
                              plugin.getConfig().getString("messages.hover-message")))))
              .event(
                  new ClickEvent(
                      ClickEvent.Action.RUN_COMMAND,
                      "/poll vote " + question.getIdentifier() + " " + transform))
              .create();
      answersComponents.add(component);
    }
    return answersComponents;
  }

  public Optional<Question> getQuestion(String identifier) {
    return getQuestions()
        .parallelStream()
        .filter(
            question ->
                question.getIdentifier().toLowerCase().equalsIgnoreCase(identifier.toLowerCase()))
        .findFirst();
  }
}
