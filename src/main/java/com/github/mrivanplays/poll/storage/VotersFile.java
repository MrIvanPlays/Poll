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
package com.github.mrivanplays.poll.storage;

import com.github.mrivanplays.poll.storage.serializers.QuestionSerializer;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VotersFile {

    private final Gson gson;
    private final Type questionType;
    private final File file;

    public VotersFile(File dataFolder) {
        questionType = new TypeToken<SerializableQuestion>() {
        }.getType();
        gson = new GsonBuilder()
                .registerTypeAdapter(questionType, new QuestionSerializer())
                .create();
        file = new File(dataFolder + File.separator, "questiondata.json");
        createFile();
    }

    public List<SerializableQuestion> deserialize() {
        List<SerializableQuestion> questions = new ArrayList<>();
        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            JsonArray array = gson.fromJson(reader, JsonArray.class);
            if (array == null || array.size() == 0) {
                return Collections.emptyList();
            }
            for (JsonElement element : array) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject question = element.getAsJsonObject();
                questions.add(gson.fromJson(question, questionType));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return questions;
    }

    public void serialize(List<SerializableQuestion> questions) {
        file.delete();
        createFile();
        JsonArray array = new JsonArray();
        for (SerializableQuestion question : questions) {
            array.add(gson.toJson(question, questionType));
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
            gson.toJson(array, writer);
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
