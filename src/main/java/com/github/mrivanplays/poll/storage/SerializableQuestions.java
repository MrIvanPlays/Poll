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
package com.github.mrivanplays.poll.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.mrivanplays.poll.question.Question;

public class SerializableQuestions {

    private static final List<SerializableQuestion> questions = new ArrayList<>();

    public static Optional<SerializableQuestion> getEquivalient(Question question) {
        for (SerializableQuestion ser : questions) {
            if (ser.getVoters().containsAll(question.getAnswered())) {
                return Optional.of(ser);
            }
        }
        return Optional.empty();
    }

    public static void register(SerializableQuestion serializableQuestion) {
        if (!questions.contains(serializableQuestion)) {
            questions.add(serializableQuestion);
        }
    }

    public static void replace(SerializableQuestion old, SerializableQuestion newQuestion) {
        questions.remove(old);
        questions.add(newQuestion);
    }

    public static List<SerializableQuestion> getForSerialize() {
        return questions;
    }
}
