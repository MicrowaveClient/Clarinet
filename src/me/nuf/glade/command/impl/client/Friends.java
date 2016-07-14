package me.nuf.glade.command.impl.client;

import me.nuf.glade.command.Argument;
import me.nuf.glade.command.Command;
import me.nuf.glade.core.Glade;
import me.nuf.glade.friend.Friend;

/**
 * Created by nuf on 3/22/2016.
 */
public class Friends {

    public static final class Add extends Command {
        public Add() {
            super(new String[]{"add", "a"}, new Argument(String.class, "username"), new Argument(String.class, "alias"));
        }

        @Override
        public String dispatch() {
            String username = getArgument("username").getValue();
            String alias = getArgument("alias").getValue();

            if (Glade.getInstance().getFriendManager().isFriend(username))
                return "That user is already a friend.";

            Glade.getInstance().getFriendManager().register(new Friend(username, alias));
            return String.format("Added friend with alias %s.", alias);
        }
    }

    public static final class Remove extends Command {
        public Remove() {
            super(new String[]{"remove", "rem"}, new Argument(String.class, "username/alias"));
        }

        @Override
        public String dispatch() {
            String name = getArgument("username/alias").getValue();
            if (!Glade.getInstance().getFriendManager().isFriend(name))
                return "That user is not a friend.";

            Friend friend = Glade.getInstance().getFriendManager().getFriendByLabel(name);
            String oldAlias = friend.getAlias();
            Glade.getInstance().getFriendManager().unregister(friend);
            return String.format("Removed friend with alias %s.", oldAlias);
        }
    }

}
