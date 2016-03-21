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
package net.dv8tion.discord.bridge.endpoint;

import java.util.ArrayList;


public abstract class EndPoint
{
    protected EndPointType connectionType;
    protected boolean connected;

    public abstract EndPointInfo toEndPointInfo();
    public abstract int getMaxMessageLength();
    public abstract void sendMessage(String message);
    public abstract void sendMessage(EndPointMessage message);

    protected EndPoint(EndPointType connectionType)
    {
        this.connectionType = connectionType;
        connected = true;
    }

    public boolean isConnected()
    {
        return connected;
    }

    protected void setConnected(boolean connected)
    {
        this.connected = connected;
    }

    public EndPointType getType()
    {
        return connectionType;
    }

    public ArrayList<String> divideMessageForSending(String message)
    {
        ArrayList<String> messageParts = new ArrayList<String>();
        while (message.length() >  getMaxMessageLength())
        {
            //Finds where the last complete word is in the IrcConnection.MAX_LINE_LENGTH length character string.
            int lastSpace = message.substring(0, getMaxMessageLength()).lastIndexOf(" ");
            String smallerLine;
            if (lastSpace != -1)
            {
                smallerLine = message.substring(0, lastSpace);
                message = message.substring(lastSpace + 1);   //Don't include the space.
            }
            else
            {
                smallerLine = message.substring(0, getMaxMessageLength());
                message = message.substring(getMaxMessageLength());
            }
            messageParts.add(smallerLine);
        }
        messageParts.add(message);
        return messageParts;
    }
}
