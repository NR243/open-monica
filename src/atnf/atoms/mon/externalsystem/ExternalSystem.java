//
// Copyright (C) CSIRO Australia Telescope National Facility
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Library General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package atnf.atoms.mon.externalsystem;

import java.util.*;
import java.io.*;
import java.lang.reflect.Constructor;
import atnf.atoms.mon.*;
import atnf.atoms.mon.util.*;
import atnf.atoms.time.*;
import atnf.atoms.mon.transaction.*;

/**
 * ExternalSystem is the base class for objects which bring new information
 * into the system. Each Datasource has a thread which schedules and manages
 * the collection of the monitor points which have been assigned to it.
 * ExternalSystem sub-classes use the information from a Transaction object in
 * order to collect the appropriate information for a particular monitor
 * point. This sub-class specific behavior is realised by implementing the
 * <i>getData</i> method.
 *
 * @author David Brodrick
 * @author Le Cuong Nguyen
 * @version $Id: ExternalSystem.java,v 1.8 2005/11/22 00:43:13 bro764 Exp $
 **/
public
class ExternalSystem
implements Runnable
{
  /** Comparator to compare PointMonitors and/or AbsTimes. We need
   * to use this Comparator with the SortedLinkedList class. */
  private class TimeComp
  implements Comparator
  {
    /** Return a timestamp for any known class type. */
    private
    long
    getTimeStamp(Object o) {
      if (o instanceof PointDescription) {
        return ((PointDescription)o).getNextEpoch();
      } else if (o instanceof AbsTime) {
        return ((AbsTime)o).getValue();
      } else if (o==null) {
        return 0;
      } else {
         System.err.println("ExternalSystem: TimeComp: compare: UNKNOWN TYPE ("
                            + o.getClass() + ")");
         return 0;
      }
    }

    /** Compare PointMonitors and/or AbsTimes. */
    public
    int
    compare(Object o1, Object o2) {
      long val1 = getTimeStamp(o1);
      long val2 = getTimeStamp(o2);

      if (val1>val2) {
        return 1;
      }
      if (val1<val2) {
        return -1;
      }
      return 0;
    }

    /** Test if equivalent to the given Comparator. */
    public
    boolean
    equals(Object o) {
      if (o instanceof TimeComp) {
        return true;
      } else {
        return false;
      }
    }
  }

  /** List of all the points which need to be collected. A SortedLinkedList
   * is used to keep the list sorted in order of time of next collection. */
  protected SortedLinkedList itsPoints = new SortedLinkedList(new TimeComp());

  /** Allows access to the thread running this collector */
  protected Thread itsThread = null;

  /** A "name" used for finding DataSources based on their source and type. */
  protected String itsName = null;

  /** Records if we're currently connected to the remote source. */
  protected boolean itsConnected = false;

  /** Keep track of how many transactions we've done. Each ExternalSystem
   * sub-class should probably zero this field whenever we reconnect to the
   * remote source. */
  protected long itsNumTransactions = 0;

  /** Flag to indicate if thread should continue running. */
  protected boolean itsKeepRunning = true;

  /** Static map of all ExternalSystems. */
  protected static HashMap theirExternalSystems = new HashMap();

  /** Add a ExternalSystem with the given unique channel description. */
  public static
  void
  addExternalSystem(String name, ExternalSystem source)
  {
    theirExternalSystems.put(name, source);
  }

  /** Get the ExternalSystem with the specified channel description. */
  public static
  ExternalSystem
  getExternalSystem(String name)
  {
    return (ExternalSystem)theirExternalSystems.get(name);
  }

   public ExternalSystem(String name)
   {
     itsName = name;
     addExternalSystem(name, this);
   }
   
   public ExternalSystem()
   {
   }
   

   /** Start all ExternalSystem collection threads. */
   public static
   void
   startAll()
   {
     Object[] ds = theirExternalSystems.values().toArray();
     for (int i=0; i<ds.length; i++) {
       ((ExternalSystem)ds[i]).startCollection();
     }
   }


   /** Stop all ExternalSystem collection threads. */
   public static
   void
   stopAll()
   {
     Object[] ds = theirExternalSystems.values().toArray();
     for (int i=0; i<ds.length; i++) {
       ((ExternalSystem)ds[i]).stopCollection();
     }
   }


   /** Start the data collection thread. */
   public synchronized
   void
   startCollection()
   {
     itsKeepRunning = true;
     itsThread = new Thread(this,"ExternalSystem " + itsName);
     itsThread.setDaemon(true);
     itsThread.start();
   }


   /** Stop the data collection thread. This method actually just sets a
    * flag to stop the collection and doesn't actually wait until collection
    * has been stopped. */
   public synchronized
   void
   stopCollection()
   {
     itsKeepRunning = false;
   }


   /** Reconnect to the remote data source. This method should be
    * overridden to achieve the required functionality. */
   public synchronized
   boolean
   connect()
   throws Exception
   {
     itsConnected = true;
     return itsConnected;
   }


   /** Disconnect from the remote data source. This method should be
    * overridden to achieve the required functionality. */
   public synchronized
   void
   disconnect()
   throws Exception
   {
      itsConnected = false;
   }


   /** Return the current connection status. */
   public
   boolean
   isConnected()
   {
     return itsConnected;
   }


   /** Return the "name" encapsulating our source and ExternalSystem type. */
   public
   String
   getName()
   {
     return itsName;
   }


   /** Get the number of Transactions performed by this ExternalSystem. */
   public
   long
   getNumTransactions()
   {
     synchronized (itsPoints) {
       return itsNumTransactions;
     }
   }


   /** Get the number of points allocated to this ExternalSystem. */
   public
   int
   getNumPoints()
   {
     synchronized (itsPoints) {
       return itsPoints.size();
     }
   }


   /** Add a point to the list of points to collect.
    * @param p The point to start monitoring. */
   public
   void
   addPoint(PointDescription p)
   {
     synchronized(itsPoints) {
       //if (itsName.indexOf("servo")!=-1) System.err.println("datasource: Adding " + p + " (" + itsPoints.size() + ")");
       itsPoints.add(p);
       itsPoints.notifyAll();
     }
   }


   /** Add an array of points to the list of points to collect.
    * @param v The points to start monitoring. */
   public
   void
   addPoints(Object[] v)
   {
     synchronized(itsPoints) {
       for (int i=0; i<v.length; i++) {
         itsPoints.add(v[i]);
       }
       itsPoints.notifyAll();
     }
   }


   /** Add a collection of points to the list of points to collect.
    * @param v The points to start monitoring. */
   public
   void
   addPoints(Collection v)
   {
     addPoints(v.toArray());
   }


   /** Remove the point to the list of points to collect.
    * @param p The point to stop monitoring. */
   public
   void
   removePoint(PointDescription p)
   {
     synchronized(itsPoints) {
       itsPoints.remove(p);
       itsPoints.notifyAll();
     }
   }

   /** Return any Transactions which are associated with this ExternalSystem. */
   protected
   Vector
   getMyTransactions(Transaction[] transactions)
   {
     Vector match = new Vector(transactions.length);
     for (int i=0; i<transactions.length; i++) {
       if (transactions[i].getChannel().equals(itsName)) {
         match.add(transactions[i]);
       }
     }
     return match;
   }

   /** This method does the real work. Sub-classes should implement this
    * method. It needs to fire PointEvent's for each monitor point once
    * the new data has been collected.
    * @param points The points that need collecting right now. */
   protected
   void
   getData(PointDescription[] points)
   throws Exception
   {
     for (int i=0; i<points.length; i++) {
       System.err.println("ExternalSystem (" + itsName + "): Unsupported monitor request from " + points[i].getFullName());
     }
   }

   public
   void
   putData(PointDescription desc, PointData pd)
   throws Exception
   {
     System.err.println("ExternalSystem (" + itsName + "): Unsupported control request from " + desc.getFullName());
   }
   
   /** Initialise all the DataSources declared in a file.
    * @param fileName The file to parse for ExternalSystem declarations. */
   public static
   void
   init(Reader sourcefile)
   {
     try {
       String[] lines = MonitorUtils.parseFile(sourcefile);
       if (lines != null)
       {
         for (int i = 0; i < lines.length; i++) {
           try {
             StringTokenizer tok = new StringTokenizer(lines[i]);
             String className = tok.nextToken();
             String[] classArgs = null;
             if (tok.countTokens()>0) {
               //Split the arguments into an array at each colon
               classArgs = tok.nextToken().split(":");
             }
             Class newes;
             try {
               //Might be fully qualified name
               newes = Class.forName(className);
             } catch (Exception e) {
               //Not fully qualified - so try defaule package
               newes = Class.forName("atnf.atoms.mon.externalsystem." + className);
             }
             Constructor con = newes.getConstructor(new Class[]{String[].class});
             con.newInstance(new Object[]{classArgs});
           } catch (Exception f) {
             MonitorMap.logger.error("ExternalSystem: Cannot Initialise " + lines[i]);
             System.err.println("ExternalSystem: Cannot Initialise \"" + lines[i] + "\" defined on line " 
                                + (i+1) + ": " + f + f.getMessage());
             f.printStackTrace();
           }
         }
       }
     } catch (Exception e) {
       e.printStackTrace();
       MonitorMap.logger.error("ExternalSystem: Cannot Initialise DataSources");
     }
   }


   /** Main loop for the point scheduling/collection thread. */
   public void run()
   {
     while (itsKeepRunning) {
       ///If we're not connected, try to reconnect
       if (!itsConnected) {
         try {
           connect();
         } catch (Exception e) {
           itsConnected = false;
         }
       }

       //We're connected, need to determine which points need collecting
       Vector thesepoints = null;
       synchronized (itsPoints) {
         try {
           //Wait for notification if there are no points
           while (itsPoints.isEmpty()) {
             itsPoints.wait();
           }
         } catch (Exception e) {
           System.err.println("ExternalSystem::run: " + e.getMessage());
           e.printStackTrace();
           continue;
         }

         //Calculate the latest epoch we're prepared to collect now
         AbsTime cutoff = AbsTime.factory();
         cutoff.add(50000); //Fudge factor for better efficiency

         //Get the set of all points within the time bracket
         thesepoints = itsPoints.headSet(cutoff);
       }
       if (!thesepoints.isEmpty()) {
         Object[] o = thesepoints.toArray();
         PointDescription[] parray = new PointDescription[o.length];
         for (int i=0; i<o.length; i++) parray[i]=(PointDescription)o[i];
         if (itsConnected && parray!=null && parray.length>0) {
           try {
             //Call the sub-class specific method to do the real work
             getData(parray);
           } catch (Exception e) {
             e.printStackTrace();
             itsConnected = false;
           }
         } else {
           //Points are scheduled for collection but we're not connected.
           //Fire null-data events for those points since old data is stale
           for (int i=0; i<parray.length; i++) {
             PointDescription pm = (PointDescription)parray[i];
             pm.firePointEvent(new PointEvent(this,
                                   new PointData(pm.getName(),
                                   pm.getSource()),
                               true));
           }
           //Throw in a brief sleep to stop fast reconnection loops
           try {
             final RelTime connectdelay = RelTime.factory(1000000);
             connectdelay.sleep();
           } catch (Exception e) {e.printStackTrace();}
         }
         //Insert the points back into our list
         addPoints(parray);
       }

       //We may need to wait before we collect the next point.
       try {
         PointDescription headpoint = (PointDescription)itsPoints.first();
         AbsTime nextTime = headpoint.getNextEpoch_AbsTime();
         AbsTime timenow = AbsTime.factory();
         if (nextTime.isAfter(timenow)) {
           //Work out how long we need to wait for
           RelTime waittime = Time.diff(nextTime, timenow);
           waittime.sleep();
         }
       } catch (Exception e) {
         System.err.println("ExternalSystem::run(): " + e.getMessage());
         e.printStackTrace();
       }
     }
   }
}