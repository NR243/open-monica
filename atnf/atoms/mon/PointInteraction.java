// Copyright (C) CSIRO Australia Telescope National Facility
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Library General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package atnf.atoms.mon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.io.*;
import atnf.atoms.util.*;
import atnf.atoms.time.*;
import atnf.atoms.mon.translation.*;
import atnf.atoms.mon.transaction.*;
import atnf.atoms.mon.util.*;

/**
 * This class is the superclass for all control and monitor points.
 *
 * @author David Brodrick
 * @author Le Cuong Nguyen
 * @version $Id: PointInteraction.java,v 1.5 2004/11/19 04:30:58 bro764 Exp $
 */
public abstract class
PointInteraction
implements ActionListener, NamedObject, Comparable
{
  /**
   * PointInteractions are subclassed into PointControls and PointMonitors.
   * This field records whether this object is an instance of a control
   * point (if true) or a monitor point (if false).
   * <P>Comment by DPB 2004/09/14 surely we don't need this! Can just use
   * instanceof right?
   */
  protected boolean itsIsControl;

  /**
   * The transaction object contains the information required
   * by the Interactor to realise the desired interaction. The
   * exact contents of the transaction object is specific to
   * what kind of transport channel is required.
   */
  protected Transaction itsTransaction = null;
  protected String itsTransactionString = "";

  /**
   * The source of a PointInteraction indicates which real-world system
   * the information contained in the point pertains to. Often, particularly
   * with primitive points, the source will be synonymous with which
   * remote machine we connect to to set get/set the data. For composite
   * points (ie those generated by events from other points) it is
   * conceivable that information from different sources may be combined,
   * in that case the source should indicate which source the final
   * value relates to. One use of this source field would be to determine
   * in which column to display values for the various sources.
   */
  protected String itsSource = null;

  /**
   * All PointInteractions have at least one name. In addition to its
   * primary name the PointInteraction can have any number of aliases.
   * These names will be in a dot "." delimited heirarchical form.
   * Methods, from interface NamedObject, are available to return both
   * a long form of the name (full heirarchical name) or the short form
   * (the portion of the name after the last dot).
   */
  protected String[] itsNames = null;

  protected Translation[] itsTranslations = null;
  protected String itsTranslationString = "";

  /**
   * Should we be able to interact with this point?
   **/
  protected boolean itsEnabled = false;

  /**
   * First time this is point is collected
   **/
  protected long itsFirstEpoch = 0;

  protected javax.swing.event.EventListenerList itsPLList = new
    javax.swing.event.EventListenerList();

  /**
   * Return the Transaction object associated with this PointInteraction.
   */
  public Transaction
  getTransaction()
  {
    return itsTransaction;
  }

  public String getTransactionString()
  {
    return itsTransactionString;
  }

  /**
   * Set the Transaction object for this point. If a set of points
   * all use an identical Transaction object this can be used to
   * have those points share a reference to a common Transaction object.
   */
  protected void
  setTransaction(Transaction t)
  {
    itsTransaction = t;
  }

  protected void setTransactionString(String transaction)
  {
    itsTransactionString = transaction;
  }

  /**
   * Return the Translation objects used by this PointInteraction.
   */
  public
  Translation[]
  getTranslations()
  {
    return itsTranslations;
  }

  public
  String
  getTranslationString()
  {
    return itsTranslationString;
  }

  /**
   * Set the Translation objects for this point.
   */
  protected
  void
  setTranslations(Translation[] t)
  {
    itsTranslations = t;
  }

  protected
  void
  setTranslationString(String translation)
  {
    itsTranslationString = translation;
  }

  protected
  void
  setTranslationString(String[] translations)
  {
    if (translations==null || translations.length==0) {
      itsTranslationString = null;
    } else if (translations.length==1) {
      itsTranslationString = translations[0];
    } else {
      itsTranslationString = "{";
      for (int i=0; i<translations.length-1; i++) {
	itsTranslationString += translations[i] + ",";
      }
      itsTranslationString += translations[translations.length-1] + "}";
    }
  }

   /**
    * Return the source for this point. The source field is used to indicate
    * what real-world system the information contained in this point
    * pertains to. For instance this could indicate which antenna some
    * monitor data was collected from.
    */
   public String
   getSource()
   {
     return itsSource;
   }

   public void setSource(String source)
   {
      itsSource = source;
   }

   /**
    * Set the names for this point. The objective of this is that all
    * points sharing a common set of names can share a reference to
    * the same set of names in memory.
    */
   protected void
   setNames(String[] newnames)
   {
     itsNames = newnames;
   }

   public String[] getAllNames()
   {
      return itsNames;
   }

   /** Gets the long name of the object */
   public String
   getLongName()
   {
     return itsNames[0];
   }

   /** Gets the total number of names this object has */
   public int
   getNumNames()
   {
     return itsNames.length;
   }

   /** Gets the name at the index specified. */
   public String
   getName(int i)
   {
     return itsNames[i];
   }

   /** Gets the short name associated with this object */
   public String
   getName()
   {
     //Return the portion of the primary name after the last dot delimitter
     //return itsNames[0].substring(itsNames[0].lastIndexOf(".")+1);
     //Return the portion of the primary name after the last dot delimitter
     return itsNames[0];//.substring(itsNames[0].lastIndexOf(".")+1);
   }

   /** Unique String that defines this point */
   public String getHash()
   {
      return itsSource+"."+itsNames[0];
   }

   /** Other unique Strings that you might use */
   public String[] getHashes()
   {
      String[] res = new String[itsNames.length];
      for (int i = 0; i < itsNames.length; i++) res[i] = itsSource+"."+itsNames[i];
      return res;
   }

   /** Return true if this point is a control point,
    *        false if the point is a monitor point.
    */
   public boolean
   isControl()
   {
     return itsIsControl;
   }

   /** Return true if this point is a monitor point,
    *        false if the point is a control point.
    */
   public boolean
   isMonitor()
   {
     return !itsIsControl;
   }

   /**
    * Parse a line from the point definitions file and generate all the
    * points defined there. The returned array may be null if the line
    * does not define any active sources for the point.
    */
   public static PointInteraction[]
   parsePoints(String line)
   {
     line = line.trim();
     //Does the line define control or monitor points
     //Control point lines start with a "C", monitor with an "M"
     String conOrMon = line.substring(0,1).toLowerCase();
     line = line.substring(1).trim();
     if (conOrMon.equals("c"))      return PointControl.parsePoints(line);
     else if (conOrMon.equals("m")) return PointMonitor.parsePoints(line);
     else if (conOrMon.equals("#")) return null; //a comment line
     else {
       //Something has gone wrong
       System.err.println("BADNESS, CAN'T PARSE POINT DEFINITION LINE!");
       return null; //Try returning null rather than just crashing?
     }
   }

   /**
    * Parse a point definitions file and return all the points defined.
    */
   public static ArrayList
   parseFile(String fname)
   {
     try {
       return parseFile(new FileReader(fname));
     } catch (Exception e) {
       System.err.println("PointInteraction:parseFile(): " + e.getClass());
       e.printStackTrace();
       return null;
     }
   }
   
   /**
    * Parse a point definitions file and return all the points defined.
    */
   public static ArrayList
   parseFile(Reader pointsfile)
   {
     ArrayList result = new ArrayList();
     String[] lines = MonitorUtils.parseFile(pointsfile);
     if (lines != null) {
       for (int i = 0; i < lines.length; i++) {
	 ArrayList al = parseLine(lines[i]);
	 if (al!=null) result.addAll(al);
	 else {
             System.err.println("PARSE ERROR: " + pointsfile + "["
                                + i + "]: " + lines[i]);
	 }
       }
     }
     return result;
   }

   public static ArrayList parseLine(String line)
   {
      return parseLine(line, true);
   }

   public static ArrayList parseLine(String line, boolean real)
   {
       ArrayList result = new ArrayList();

       String[] toks = MonitorUtils.getTokens(line);
       if (toks.length != 12) return null;

       // Confusing parsing to follow - extracts appropriate information and make point/s
       boolean controlF = (toks[0].equalsIgnoreCase("C")) ? true : false;
       String[] pointNameArray = getTokens(toks[1]);
       String pointLongDesc = toks[2];
       String pointShortDesc = toks[3];
       String pointUnits = toks[4];
       String[] pointSourceArray = getTokens(toks[5]);
       String pointEnabled = toks[6];
       String[] pointChannelArray = getTokens(toks[7]);
       String[] pointTranslateArray = getTokens(toks[8]);
       String[] pointLimitsArray = getTokens(toks[9]);
       String[] pointArchiveArray = getTokens(toks[10]);
       String pointPeriod = toks[11];

       boolean[] pointEnabledArray = parseBoolean(pointEnabled);
       if (pointEnabled.length() < pointSourceArray.length) {
          boolean[] temp = new boolean[pointSourceArray.length];
	  for (int i = 0; i < temp.length; i++) temp[i] = pointEnabledArray[0];
	  pointEnabledArray = temp;
       }

       if (pointChannelArray.length < pointSourceArray.length) {
          String[] temp = new String[pointSourceArray.length];
          for (int i = 0; i < pointSourceArray.length; i++)
	     if (i < pointChannelArray.length) temp[i] = pointChannelArray[i];
	     else temp[i] = pointChannelArray[pointChannelArray.length-1];
	  pointChannelArray = temp;
       }

/*       if (pointTranslateArray.length < pointSourceArray.length) {
          String[] temp = new String[pointSourceArray.length];
          for (int i = 0; i < pointSourceArray.length; i++)
	     if (i < pointTranslateArray.length) temp[i] = pointTranslateArray[i];
	     else temp[i] = pointTranslateArray[pointTranslateArray.length-1];
	  pointTranslateArray = temp;
       }
*/
       if (pointLimitsArray.length < pointSourceArray.length) {
          String[] temp = new String[pointSourceArray.length];
          for (int i = 0; i < pointSourceArray.length; i++)
	     if (i < pointLimitsArray.length) temp[i] = pointLimitsArray[i];
	     else temp[i] = pointLimitsArray[pointLimitsArray.length-1];
	  pointLimitsArray = temp;
       }

       for (int i = 0; i < pointSourceArray.length; i++)
	  if (!controlF) result.add((real) ?
	     PointMonitor.factory(pointNameArray, pointLongDesc, pointShortDesc, pointUnits,
				  pointSourceArray[i], pointChannelArray[i],
				  pointTranslateArray, pointLimitsArray[i],
				  pointArchiveArray, pointPeriod, pointEnabledArray[i]
	     ):
	     FakeMonitor.Fakefactory(pointNameArray, pointLongDesc, pointShortDesc, pointUnits,
				     pointSourceArray[i], pointChannelArray[i],
				     pointTranslateArray, pointLimitsArray[i],
				     pointArchiveArray, pointPeriod, pointEnabledArray[i]
	     )
	  );

       return result;
   }

   /** Converts a TTFT string into the appropriate array */
   public static boolean[] parseBoolean(String token)
   {
      boolean[] res = new boolean[token.length()];
      for (int i = 0; i < res.length; i++)
         if (token.charAt(i) == 't' || token.charAt(i) == 'T') res[i] = true;
	 else res[i] = false;
      return res;
   }

   /** Breaks a line into tokens */
   protected static String[] getTokens(String line)
   {
       StringTokenizer tok = new StringTokenizer(line,", \t\r\n");
       String[] result = new String[tok.countTokens()];
       for (int i = 0; i < result.length; i++) result[i] = tok.nextToken().trim();
       return result;
   }

   public int getNumListeners()
   {
      return itsPLList.getListenerCount();
   }
   
   public void addPointListener(PointListener listener)
   {
      itsPLList.add(PointListener.class, listener);
   }

   public void removePointListener(PointListener listener)
   {
      itsPLList.remove(PointListener.class, listener);
   }

   public void firePointEvent(PointEvent pe)
   {
      Object[] listeners = itsPLList.getListenerList();
      for (int i = 0; i < listeners.length; i +=2)
         if (listeners[i] == PointListener.class)
	    ((PointListener)listeners[i+1]).onPointEvent(this, pe);
   }

   public void actionPerformed(ActionEvent e) {}


   /** Get next scheduled collection time as a long. */
  public
  long
  getNextEpoch()
  {
    return -1;
  }


  /** Get next scheduled collection time as an AbsTime. */
  public
  AbsTime
  getNextEpoch_AbsTime()
  {
    return AbsTime.factory(getNextEpoch());
  }


   public boolean getEnabled()
   {
      return itsEnabled;
   }

   public void setEnabled(boolean enabled)
   {
      itsEnabled = enabled;
   }

   /**
    * Compare the next-collection timestamp with another PointInteraction
    * or an AbsTime.
    **/  
   public int compareTo(Object obj)
   {
     if (obj instanceof PointInteraction) {
       if (((PointInteraction)obj).getNextEpoch() < getNextEpoch())
	 return 1;
       if (((PointInteraction)obj).getNextEpoch() > getNextEpoch())
	 return -1;
       return 0;
     } else if (obj instanceof AbsTime) {
       if (((AbsTime)obj).getValue() < getNextEpoch())
	 return 1;
       if (((AbsTime)obj).getValue() > getNextEpoch())
	 return -1;
       return 0;
     } else {
       System.err.println("PointInteraction: compareTo: UNKNOWN TYPE!");
       return -1;
     }
   }
}
