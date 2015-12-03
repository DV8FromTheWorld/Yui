package net.dv8tion.discord.fixes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.event.EventListener;
import me.itsghost.jdiscord.event.EventManager;

/**
 * Example usage:
 *      DiscordAPI api = new DiscordBuilder("email", "password").build().login();
 *      EventManagerX.replaceEventManager(api);
 *
 *      api.getEventManager().registerListener(...);    //The getEventManager will always return the EventManagerX now.
 * @author DV8FromTheworld
 */
public class EventManagerX extends EventManager
{
    private ArrayList<EventListener> listeners;

    @SuppressWarnings("unchecked")
    private EventManagerX(EventManager oldEventManager) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        listeners = new ArrayList<EventListener>();

        Field oldListenersF = oldEventManager.getClass().getDeclaredField("listeners");
        oldListenersF.setAccessible(true);
        List<EventListener> oldListeners = (List<EventListener>) oldListenersF.get(oldEventManager);

        for (EventListener listener : oldListeners)
        {
            listeners.add(listener);
        }
    }

    public static void replaceEventManager(DiscordAPI api)
    {
        try
        {
            Field eventManagerF = api.getClass().getDeclaredField("eventManager");
            eventManagerF.setAccessible(true);
            EventManager oldEventManager = (EventManager) eventManagerF.get(api);

            eventManagerF.set(api, new EventManagerX(oldEventManager));
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
        {
            System.err.println("UNABLE TO REPLACE THE DEFAULT EVENTMANAGER");
            e.printStackTrace();
        }
    }

    //Copied this into this class so that we can use the same register method but store them
    //  in OUR listener list instead of the old one.
    @Override
    public void registerListener(EventListener e) {
        listeners.add(e);
    }

    //Copied straight from jDiscord's EventManager.
    @Override
    public void executeEvent(Object e) {
        for (EventListener ClassO : listeners) {
            for (Method m : ClassO.getClass().getMethods()) {
                try {
                    if (m.getParameterTypes()[0].getName().equals(e.getClass().getName())) {
                        try {
                            //ClassO.getClass().getDeclaredMethod(m.getName(), e.getClass()).invoke(ClassO, e);
                            m.setAccessible(true);  //The Fix
                            m.invoke(ClassO, e);    //Easier way to call the method than the crazy mess above ^
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            System.out.println("Couldn't run event!");
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

    }
}
