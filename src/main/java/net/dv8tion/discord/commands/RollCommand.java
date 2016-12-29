/**
 *     Copyright 2015-2016 Austin Keener
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.discord.commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RollCommand extends Command
{
    private final Random dice = new Random();
    @Override
    public void onCommand(MessageReceivedEvent e, String[] args)
    {
        int lowerBound;
        int upperBound;
        int roll;

        if (args.length == 1)
        {
            lowerBound = 1;
            upperBound = 6;
        }
        else if (args.length == 2)
        {
            lowerBound = 1;
            try
            {
                upperBound = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException ex)
            {
                sendMessage(e, "The provided upperbound is not an integer! Provided value: " + args[1]);
                return;
            }
        }
        else
        {
            try
            {
                lowerBound = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException ex)
            {
                sendMessage(e, "The provided lowerBound is not an integer! Provided value: " + args[1]);
                return;
            }

            try
            {
                upperBound = Integer.parseInt(args[2]);
            }
            catch (NumberFormatException ex)
            {
                sendMessage(e, "The provided upperBound is not an integer! Provided value: " + args[2]);
                return;
            }
        }
        //Instead of erroring when the upper and lower are out of order, just flip them.
        if (lowerBound > upperBound)
        {
            int temp = upperBound;
            upperBound = lowerBound;
            lowerBound = temp;
        }

        roll = dice.nextInt(upperBound - lowerBound) + lowerBound;
        sendMessage(e, "Rolled dice with range of [" + lowerBound + " - " + upperBound + "] and got: **" + roll + "**");
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(".roll", ".dice");
    }

    @Override
    public String getDescription()
    {
        return "Rolls the dice and produces a random value.";
    }

    @Override
    public String getName()
    {
        return "Dice Roll";
    }

    @Override
    public List<String> getUsageInstructions()
    {
        return Arrays.asList("\n`.roll` - Returns a roll of a 6 sided die (1 - 6)\n\n" +
                "`.roll [upperLimit]` - Returns a roll of [1 - upperLimit]\n\n" +
                "`.roll [lowerLimit] [upperLimit]` - Returns a roll of [lower - upper]");
    }
}
