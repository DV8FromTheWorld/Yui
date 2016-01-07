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
