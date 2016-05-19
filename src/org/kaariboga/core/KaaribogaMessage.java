package org.kaariboga.core;

import java.io.Serializable;

/**
 * The kaariboga message is a message that kaaribogas and bases can
 * send to each other.
 * In contains information about the recipient, the sender, the
 * kind and the content of a message. Messages can contain all kinds
 * of contents, for example agents, class source code or files.
 *
 * @author Dirk Struve
 */
public class KaaribogaMessage implements Serializable
{
    /**
     * Unused at the moment.
     */
    public int version = 0;

    /**
     *  Id for this message.
     */
    public long id = 0;

    /**
     * Recipient of this message.
     * If the recipient is another base the name field of
     * the KaaribogaMessage should be empty.
     */
    public KaaribogaAddress recipient;

    /**
     * Sender of this message.
     * If the sender is not an agent but the base the name field
     * of the sender should be empty.
     */
    public KaaribogaAddress sender;

    /**
     * Kind of message.<BR>
     * Values:<BR>
     * KAARIBOGA: Message contains an an agent. content field contains
     *            the agent's name. binary field it's values.<BR>
     * GET: Get a kaariboga from another base. content field contains
     *      the agent's name.<BR>
     * GET_CLASS: Get the java byte code of a class. content field
     *            contains the class name.<BR>
     * CLASS: Message contains java byte code of a class. content field
     *        contains the class name. binary the byte code.<BR>
     * FILE: Message contains a file. content field contains the file name.
     *        binary the file.<BR>
     * MESSAGE: To send arbitrary messages. content and binary may be
     *          used freely.<BR>
     */
    public String kind;

    /**
     * String content of the message.
     */
    public String content;

    /**
     * Binary content of the message
     */
    public byte binary[];

    /**
     * Creates a new KaaribogaMessage.
     *
     * @param sender The object that sends the message.
     *               If the sender is not an agent but the base the name field
     *               of the sender should be empty.
     * @param recipient The receiving object.
     *               If the recipient is another base the name field of
     *               the KaaribogaMessage should be empty.
     * @param kind The kind of the message. This can be
     *        KAARIBOGA, GET, GET_CLASS, CLASS, FILE, MESSAGE.
     * @param content Extra string for message contents for example
     *        a class or a file name.
     * @param binary Binary content of the message for example the
     *        source code of a file.
     */
    public KaaribogaMessage( KaaribogaAddress sender,
                             KaaribogaAddress recipient,
                             String kind,
                             String content,
                             byte binary[] )
    {
        this.sender = sender;
        this.recipient = recipient;
        this.kind = kind;
        this.content = content;
        this.binary = binary;
    }

    /**
     * Creates a new KaaribogaMessage.
     *
     * @param id An Id for this message. Use the id to identify your message
     *           for example for error handling.
     * @param sender The object that sends the message.
     *               If the sender is not an agent but the base the name field
     *               of the sender should be empty.
     * @param recipient The receiving object.
     *               If the recipient is another base the name field of
     *               the KaaribogaMessage should be empty.
     * @param kind The kind of the message. This can be
     *        KAARIBOGA, GET, GET_CLASS, CLASS, FILE, MESSAGE.
     * @param content Extra string for message contents for example
     *        a class or a file name.
     * @param binary Binary content of the message for example the
     *        source code of a file.
     */
    public KaaribogaMessage( long id,
                             KaaribogaAddress sender,
                             KaaribogaAddress recipient,
                             String kind,
                             String content,
                             byte binary[] )
    {
        this.version = 0;
        this.sender = sender;
        this.id = id;
        this.recipient = recipient;
        this.kind = kind;
        this.content = content;
        this.binary = binary;
    }


    public String toString() {
    
    	String s = "KaaribogaMessage\nFrom: " + sender + "\nTo: " + recipient +
		   "\nSubject: " + kind + "\nContent: ";
	if( content != null ) return s + content;
	else return s + "[binary]";
    }
}


