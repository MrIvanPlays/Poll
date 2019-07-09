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

import java.util.UUID;

import com.google.gson.JsonObject;

import com.github.mrivanplays.poll.util.Voter;

public class VoterSerializer
{

    public static Voter deserialize(JsonObject object)
    {
        if ( !object.has( "uuid" ) || !object.has( "answered" ) )
        {
            throw new RuntimeException( "Invalid json parsed" );
        }
        return new Voter( UUID.fromString( object.get( "uuid" ).getAsString() ), object.get( "answered" ).getAsString() );
    }

    public static JsonObject serialize(Voter src)
    {
        JsonObject object = new JsonObject();
        object.addProperty( "uuid", src.getUuid().toString() );
        object.addProperty( "answered", src.getAnswered() );
        return object;
    }
}
