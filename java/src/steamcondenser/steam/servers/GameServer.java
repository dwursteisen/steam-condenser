/** 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 */

package steamcondenser.steam.servers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import steamcondenser.SteamCondenserException;
import steamcondenser.steam.SteamPlayer;
import steamcondenser.steam.packets.A2S_INFO_Packet;
import steamcondenser.steam.packets.S2A_INFO_BasePacket;
import steamcondenser.steam.packets.A2A_PING_Packet;
import steamcondenser.steam.packets.A2S_PLAYER_Packet;
import steamcondenser.steam.packets.S2A_PLAYER_Packet;
import steamcondenser.steam.packets.A2S_RULES_Packet;
import steamcondenser.steam.packets.S2A_RULES_Packet;
import steamcondenser.steam.packets.A2S_SERVERQUERY_GETCHALLENGE_Packet;
import steamcondenser.steam.packets.S2C_CHALLENGE_Packet;
import steamcondenser.steam.packets.SteamPacket;
import steamcondenser.steam.sockets.QuerySocket;

/**
 * @author Sebastian Staudt
 * @version $Id$
 */
abstract public class GameServer
{
    private static final int REQUEST_CHALLENGE = 0;
    
    private static final int REQUEST_INFO = 1;
    
    private static final int REQUEST_PLAYER = 2;
    
    private static final int REQUEST_RULES = 3;
    
    protected int challengeNumber = 0xFFFFFFFF;

    protected int ping;

    protected ArrayList<SteamPlayer> playerArray;

    protected int rconRequestId;

    protected HashMap<String, String> rulesHash;

    protected HashMap<String, Object> serverInfo;

    protected QuerySocket socket;

    /**
     * Checks if the port number is valid
     * @param portNumber The port number of the server
     * @throws IllegalArgumentException 
     */
    protected GameServer(int portNumber)
    throws IllegalArgumentException
    {
	if(portNumber < 0 || portNumber > 65535)
	{
	    throw new IllegalArgumentException("The listening port of the server has to be a number greater than 0 and less than 65535.");
	}
    }

    /**
     * @return The response time of this server in milliseconds
     */
    public int getPing()
    {
	return this.ping;
    }

    /**
     * @return An ArrayList of SteamPlayers representing all players on this server
     */
    public ArrayList<SteamPlayer> getPlayers()
    {
	return this.playerArray;
    }

    /**
     * @return A HashMap containing the rules of this server
     */
    public HashMap<String, String> getRules()
    {
	return this.rulesHash;
    }

    /**
     * @return A HashMap containing basic information about the server
     */
    public HashMap<String, Object> getServerInfo()
    {
	return this.serverInfo;
    }
    
    /**
     * Returns a packet sent by the server in response to a query
     * @return Packet recieved from the server
     * @throws IOException
     * @throws TimeoutException
     * @throws SteamCondenserException
     */
    private SteamPacket getReply()
    throws IOException, TimeoutException, SteamCondenserException
    {
	return this.socket.getReply();
    }
    
    /**
     * @throws IOException 
     * @throws SteamCondenserException 
     * @throws TimeoutException 
     */
    private void handleResponseForRequest(int requestType)
    	throws IOException, TimeoutException, SteamCondenserException
    {
	this.handleResponseForRequest(requestType, true);
    }
    
    /**
     * @throws IOException 
     * @throws SteamCondenserException 
     * @throws TimeoutException 
     */
    private void handleResponseForRequest(int requestType, boolean repeatOnFailure)
    	throws IOException, TimeoutException, SteamCondenserException
    {
	Class<? extends SteamPacket> expectedResponse = null;
	SteamPacket requestPacket = null;
	
	try {
	    switch(requestType)
	    {
	    	case GameServer.REQUEST_CHALLENGE:
	    	    expectedResponse = S2C_CHALLENGE_Packet.class;
	    	    requestPacket    = new A2S_SERVERQUERY_GETCHALLENGE_Packet();
	    	    break;
	    	case GameServer.REQUEST_INFO:
	    	    expectedResponse = S2A_INFO_BasePacket.class;
	    	    requestPacket    = new A2S_INFO_Packet();
	    	    break;
	    	case GameServer.REQUEST_PLAYER:
	    	    expectedResponse = S2A_PLAYER_Packet.class;
	    	    requestPacket    = new A2S_PLAYER_Packet(this.challengeNumber);
	    	    break;
	    	case GameServer.REQUEST_RULES:
	    	    expectedResponse = S2A_RULES_Packet.class;
	    	    requestPacket    = new A2S_RULES_Packet(this.challengeNumber);
	    	    break;
	    	default:
	    	    throw new SteamCondenserException("Called with wrong request type.");
	    }
	    
	    this.sendRequest(requestPacket);
	    
	    SteamPacket responsePacket = this.getReply();
	    
	    if(responsePacket instanceof S2A_INFO_BasePacket)
	    {
	        this.serverInfo = ((S2A_INFO_BasePacket) responsePacket).getInfoHash();
	    }
	    else if(responsePacket instanceof S2A_PLAYER_Packet)
	    {
	        this.playerArray = ((S2A_PLAYER_Packet) responsePacket).getPlayerArray();
	    }
	    else if(responsePacket instanceof S2A_RULES_Packet)
	    {
	        this.rulesHash = ((S2A_RULES_Packet) responsePacket).getRulesHash();
	    }
	    else if(responsePacket instanceof S2C_CHALLENGE_Packet)
	    {
	        this.challengeNumber = ((S2C_CHALLENGE_Packet) responsePacket).getChallengeNumber();
	    }
	    else
	    {
		throw new SteamCondenserException("Response of type " + responsePacket.getClass() + " cannot be handled by this method.");
	    }
	    
	    if(responsePacket.getClass() != expectedResponse)
	    {
	        System.out.println("Expected " + expectedResponse + ", got " + responsePacket.getClass() + ".");
	        if(repeatOnFailure)
	        {
	    		this.handleResponseForRequest(requestType, false);
	        }
	    }
	}
	catch(TimeoutException e)
	{
	    System.out.println("Expected " + expectedResponse + ", but timed out. The server is probably offline.");
	}
    }

    /**
     * Initializes the server object with basic data (ping, server info and
     * challenge number)
     * @throws IOException
     * @throws TimeoutException
     * @throws SteamCondenserException
     */
    public void initialize()
    throws IOException, TimeoutException, SteamCondenserException
    {
	this.updatePing();
	this.updateServerInfo();
	this.updateChallengeNumber();
    }

    abstract public boolean rconAuth(String password)
    throws IOException, TimeoutException, SteamCondenserException;

    abstract public String rconExec(String command)
    throws IOException, TimeoutException, SteamCondenserException;

    /**
     * Sends a query packet to the server
     * @param requestData The query packet to send to the server
     * @throws IOException
     */
    private void sendRequest(SteamPacket requestData)
    throws IOException
    {
	this.socket.send(requestData);
    }

    /**
     * Returns a String representation of this server
     * @return A human readable version of this server's information
     */
    public String toString()
    {
	String returnString = "";

	returnString += "Ping: " + this.ping + "\n";
	returnString += "Challenge number: " + this.challengeNumber + "\n";

	if(this.serverInfo != null)
	{
	    returnString += "Info:" + "\n";
	    for(Entry<String, Object> info : this.serverInfo.entrySet())
	    {
		returnString += "  " + info.getKey() + ": " + info.getValue() + "\n";
	    }
	}

	if(this.playerArray != null)
	{
	    returnString += "Players:" + "\n";
	    for(SteamPlayer player : this.playerArray)
	    {
		returnString += "  " + player + "\n";
	    }
	}

	if(this.rulesHash != null)
	{
	    returnString += "Rules:" + "\n";
	    for(Entry<String, String> rule : this.rulesHash.entrySet())
	    {
		returnString += "  " + rule.getKey() + ": " + rule.getValue() + "\n";
	    }
	}

	return returnString;
    }
    
    /**
     * Get the challenge number from the server 
     * @throws IOException
     * @throws SteamCondenserException 
     * @throws TimeoutException 
     */
    public void updateChallengeNumber()
    throws IOException, TimeoutException, SteamCondenserException
    {
	this.handleResponseForRequest(GameServer.REQUEST_CHALLENGE);
    }

    /**
     * Pings the server
     * @throws IOException
     * @throws TimeoutException
     * @throws SteamCondenserException
     */
    public void updatePing()
    throws IOException, TimeoutException, SteamCondenserException
    {
	this.sendRequest(new A2A_PING_Packet());
	long startTime = System.currentTimeMillis();
	this.getReply();
	long endTime = System.currentTimeMillis();
	this.ping = Long.valueOf(endTime - startTime).intValue();
    }

    /**
     * Gets information about the players on the server
     * @throws IOException
     * @throws TimeoutException
     * @throws SteamCondenserException
     */
    public void updatePlayerInfo()
    throws IOException, TimeoutException, SteamCondenserException
    {
	this.handleResponseForRequest(GameServer.REQUEST_PLAYER);
    }

    /**
     * Gets information about the setting of the server
     * @throws IOException
     * @throws TimeoutException
     * @throws SteamCondenserException
     */
    public void updateRulesInfo()
    throws IOException, TimeoutException, SteamCondenserException
    {
	this.handleResponseForRequest(GameServer.REQUEST_RULES);
    }

    /**
     * Gets basic server information
     * @throws IOException
     * @throws TimeoutException
     * @throws SteamCondenserException
     */
    public void updateServerInfo()
    throws IOException, TimeoutException, SteamCondenserException
    {
	this.handleResponseForRequest(GameServer.REQUEST_INFO);
    }
}