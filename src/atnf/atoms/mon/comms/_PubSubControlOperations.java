// **********************************************************************
//
// Copyright (c) 2003-2013 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************
//
// Ice version 3.5.0
//
// <auto-generated>
//
// Generated from file `MoniCA.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package atnf.atoms.mon.comms;

public interface _PubSubControlOperations
{
    void subscribe(PubSubRequest req, Ice.Current __current);

    void unsubscribe(String topicname, Ice.Current __current);

    void keepalive(String topicname, Ice.Current __current);
}