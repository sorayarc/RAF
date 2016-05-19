package org.kaariboga.core;


/**
 *  Interface for classes who want to register as a message handler.
 *  A class may want to extend the possibilities of KaaribogaBase
 *  to handle different kinds of messages. For this purpose a
 *  class may register at KaaribogaBase as a MessageHander.
 *
 *  Every class in the Kaariboga environment, that is capable of
 *  receiving messages, should implement this interface.
 *
 *  The difference between MessageListeners and MessageHandlers
 *  is, that a MessageListener listens to messages that may be
 *  fired asynchronous. 
 */
public interface KbMessageHandler
{
    /**
     *  The target base of the message can not be reached.
     */
    public final static int BASE_NOT_REACHABLE = 1;

    /**
     *  The target base is online, but the agent is not there.
     */
    public final static int AGENT_NOT_REACHABLE = 2;

    /**
     *  No messages can be delivered to other bases, because the base is offline.
     */
    public final static int BASE_OFFLINE = 3;


    /**
     *  Called when a message for the DeliveryServiceUser has arrived.
     *
     *  @param message The message, that has arrived for the user.
     */
    public void handleMessage( KaaribogaMessage message );


    /**
     *  Called in case a message could not be delivered.
     *  It is not guaranteed, that this method is called in every case, for example
     *  if a DeliveryService is offline, it may decide to store incoming messages
     *  until it is online again.
     *
     *  @param messageId Id of the message, that could not be delivered.
     *  @param errorCode Delivery error code. At the moment the following codes exit:
     *                   BASE_NOT_REACHABLE, AGENT_NOT_REACHABLE, BASE_OFFLINE
     */
    public void messageDeliveryError( long messageId, int errorCode );

}
