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
package com.mrivanplays.poll.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

public class VotersFile {

  private final Gson gson;
  private final File file;

  public VotersFile(File dataFolder) {
    gson = new GsonBuilder().setPrettyPrinting().create();
    file = new File(dataFolder + File.separator, "questiondata.json");
    createFile();
  }

  public Set<SerializableQuestion> deserialize() {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      QuestionResult result = gson.fromJson(reader, QuestionResult.class);
      if (result != null) {
        return result.getQuestions();
      } else {
        return Collections.emptySet();
      }
    } catch (IOException e) {
      return Collections.emptySet();
    }
  }

  public void serialize(Set<SerializableQuestion> questions) {
    file.delete();
    createFile();
    try (Writer writer = new FileWriter(file)) {
      QuestionResult result = new QuestionResult();
      result.setQuestions(questions);
      gson.toJson(result, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void createFile() {
    if (!file.exists()) {
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      try {
        file.createNewFile();
      } catch (IOException ignored) {
      }
    }
  }
}
