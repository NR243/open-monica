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

public abstract class Callback_MoniCAIce_getAllPointNamesChunk extends Ice.TwowayCallback
{
    public abstract void response(String[] __ret);

    public final void __completed(Ice.AsyncResult __result)
    {
        MoniCAIcePrx __proxy = (MoniCAIcePrx)__result.getProxy();
        String[] __ret = null;
        try
        {
            __ret = __proxy.end_getAllPointNamesChunk(__result);
        }
        catch(Ice.LocalException __ex)
        {
            exception(__ex);
            return;
        }
        response(__ret);
    }
}
