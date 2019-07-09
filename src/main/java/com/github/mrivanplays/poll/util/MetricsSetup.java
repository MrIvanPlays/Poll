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
package com.github.mrivanplays.poll.util;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.bstats.bukkit.Metrics;

import com.github.mrivanplays.poll.Poll;
import com.github.mrivanplays.poll.question.Question;

@RequiredArgsConstructor
public class MetricsSetup
{

    private final Poll plugin;

    public void setup()
    {
        Metrics metrics = new Metrics( plugin );
        metrics.addCustomChart( new Metrics.SimplePie( "using_auto_announcer", () ->
                returnBoolean( plugin.getConfig().getBoolean( "question-announcer.enable" ) ) ) );
        metrics.addCustomChart( new Metrics.SimplePie( "question_count", () ->
                returnInt( plugin.getConfig().getConfigurationSection( "questions" ).getKeys( false ).size() ) ) );
        metrics.addCustomChart( new Metrics.SimplePie( "average_question_answer_count", () ->
        {
            List<Integer> sizes = new ArrayList<>();
            for ( Question question : plugin.getQuestionHandler().getQuestions() )
            {
                sizes.add( question.getValidAnswers().size() );
            }
            double average = sizes.parallelStream().mapToInt( val -> val ).average().orElse( 0.0 );
            return returnInt( Math.round( Math.round( average ) ) );
        } ) );
    }

    private String returnBoolean(boolean bool)
    {
        return bool ? "Yes" : "No";
    }

    private String returnInt(int s)
    {
        return Integer.toString( s );
    }

}
