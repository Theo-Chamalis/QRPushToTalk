package com.terracom.qrpttbeta.db;

import com.terracom.jumble.model.Server;

import java.util.List;

public interface QRPushToTalkDatabase {
    public void open();

    public void close();

    public List<Server> getServers();

    public void addServer(Server server);

    public void updateServer(Server server);

<<<<<<< HEAD
    public void removeServer(Server server);

    public List<Integer> getPinnedChannels(long serverId);

=======
    public List<Integer> getPinnedChannels(long serverId);
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0
    public List<String> getAccessTokens(long serverId);

    public List<Integer> getLocalMutedUsers(long serverId);

    public void addLocalMutedUser(long serverId, int userId);

    public void removeLocalMutedUser(long serverId, int userId);

    public List<Integer> getLocalIgnoredUsers(long serverId);

    public void addLocalIgnoredUser(long serverId, int userId);

    public void removeLocalIgnoredUser(long serverId, int userId);
}
