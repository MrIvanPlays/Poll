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
package com.github.mrivanplays.poll.storage.serializers;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import com.github.mrivanplays.poll.util.Voter;
import com.github.mrivanplays.poll.storage.SerializableQuestion;

public class QuestionSerializer implements JsonSerializer<SerializableQuestion>, JsonDeserializer<SerializableQuestion>
{

    @Override
    public SerializableQuestion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject object = new JsonObject();
        if ( !object.has( "questionIdentifier" ) || !object.has( "voters" ) )
        {
            throw new JsonParseException( "Invalid json parsed" );
        }
        Collection<Voter> voters = new HashSet<>();
        JsonArray votersArray = object.getAsJsonArray( "voters" );
        for ( JsonElement element : votersArray )
        {
            if ( !element.isJsonObject() )
            {
                continue;
            }
            JsonObject voter = element.getAsJsonObject();
            voters.add( VoterSerializer.deserialize( voter ) );
        }
        return new SerializableQuestion( object.get( "questionIdentifier" ).getAsString(), voters );
    }

    @Override
    public JsonElement serialize(SerializableQuestion src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject object = new JsonObject();
        JsonArray votersArray = new JsonArray();
        object.addProperty( "questionIdentifier", src.getQuestionIdentifier() );
        for ( Voter voter : src.getVoters() )
        {
            votersArray.add( VoterSerializer.serialize( voter ) );
        }
        object.add( "voters", votersArray );
        return object;
    }

}
