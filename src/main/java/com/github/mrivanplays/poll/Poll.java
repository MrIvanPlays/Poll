package com.github.mrivanplays.poll;

import java.util.function.Function;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.mrivanplays.poll.commands.CommandPoll;
import com.github.mrivanplays.poll.commands.CommandPollVotes;
import com.github.mrivanplays.poll.question.QuestionAnnouncer;
import com.github.mrivanplays.poll.question.QuestionHandler;
import com.github.mrivanplays.poll.storage.SerializableQuestion;
import com.github.mrivanplays.poll.storage.SerializableQuestions;
import com.github.mrivanplays.poll.storage.VotersFile;
import com.github.mrivanplays.poll.util.MetricsSetup;

public final class Poll extends JavaPlugin
{

    @Getter
    private VotersFile votersFile;
    @Getter
    private QuestionHandler questionHandler;
    private QuestionAnnouncer announcer;

    public static Function<String, String> ANSWER_FUNCTION = answer ->
    {
        char[] chars = answer.toCharArray();
        for ( int i = 0; i < chars.length; i++ )
        {
            if ( chars[i] == '&' || chars[i] == '\u00a7' )
            {
                chars[i] = ' ';
                chars[i + 1] = ' ';
            }
        }
        String s = new String( chars );
        return s.replace( " ", "" );
    };

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        votersFile = new VotersFile( getDataFolder() );
        if ( !votersFile.deserialize().isEmpty() )
        {
            for ( SerializableQuestion question : votersFile.deserialize() )
            {
                SerializableQuestions.register( question );
            }
        }
        questionHandler = new QuestionHandler( this );
        new CommandPoll( this );
        new CommandPollVotes( this );
        announcer = new QuestionAnnouncer( this );
        announcer.loadAsAnnouncements();
        new MetricsSetup( this ).setup();
    }

    @Override
    public void onDisable()
    {
        votersFile.serialize( SerializableQuestions.getForSerialize() );
    }

    public String color(String text)
    {
        return ChatColor.translateAlternateColorCodes( '&', text );
    }

    public void reload()
    {
        reloadConfig();
        announcer.reloadAnnouncements();
        votersFile.serialize( SerializableQuestions.getForSerialize() );
    }
}
