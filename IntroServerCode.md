# Introduction #

This page gives a brief introduction to some of the different classes/packages used by the MoniCA server. It is intended to assist new developers to get a feel for the basic architecture of the server.

![http://www.narrabri.atnf.csiro.au/people/bro764/MoniCA-classdiagram.png](http://www.narrabri.atnf.csiro.au/people/bro764/MoniCA-classdiagram.png)

# External System #

An 'external system' (instance of an `ExternalSystem` sub-class) is usually responsible for collecting monitor data and/or writing control data from one remote device/process. For instance you may have one external system which talks to your UPS and a different one which communicates with your weather station. Each external system may update values for multiple monitor points corresponding to different parameters available from the UPS or weather station (for example).

External systems may accept instantiation-time arguments defined in the `monitor-sources.txt` config file, for instance to tell it the network address of which UPS you wish to monitor. In this fashion, you may monitor many instances of one kind of device without needing to make any software changes.

# Point Descriptions #

Monitor/control points (`PointDescription` instances) encapsulate all of the meta-data for a single parameter that the system is monitoring and/or controlling. This includes information such as the description of the point, units, source name (eg the network address of which particular UPS this datum comes from), update interval, etc. Points are defined in the `monitor-points.txt` config file or may be dynamically created by external systems (eg, after the systems discovers what data is available from the specific device at run-time).

The `PointDescription` object has fields for other objects related to this point, such as to tell the system under what conditions this point's values should be archived, or how to translate the raw value read from hardware into a meaningful value. Some of these classes are explored below.

## Transactions ##

When a monitor point is scheduled to be updated, it is handed to the appropriate external system so that a new value can be obtained. The point's `Transaction` field has two roles to play in this process:

  * The transaction `channel` name is used to identify the external system responsible for updating this point.
  * If the external system requires additional information to obtain the new value for each point, eg an address of the hardware register where this point's value is located, then the transaction subclass can contain the additional fields required by the data source. These fields are populated by transaction arguments in the monitor point definitions file.

`TransactionListen` allows monitor points to receive updates whenever other specific points are updated, and is a useful tool in agregating points to create new information or higher-level constructs.

Transactions are also used in control operations, where they are similarly used to provide the relevant information (eg register address) to enable the appropriate control operation to be performed.

## Translations ##

After the data source has collected a new value for the monitor point, we may wish to process it into a more meaninful form. For instace we might convert a raw number obtained from an ADC into a floating point voltage, or extract a single bit from an integer, or map an integer to a descriptive string, etc.

`Translation` subclasses can be used to perform these processing steps on new values. Each monitor point can have any number of translations organised as a chain, such that the output of one translation is used as input to the next. This way complex behaviour can be achieved at configuration-time rather than requiring new code.

There are very few limitations on what operations translations can perform, for instance some may perform statistical analysis on a history of N inputs. Others may aggregate the values from several monitor points to produce new kinds of data. A description of all translations is [available](TranslationList.md).

## Value Alarm Checking ##

A point can have one or more associated `AlarmCheck` objects which determines whether the current value is okay, or in an alarm state. Essentially any logic may be used by an `AlarmCheck` subclass, typical examples include checking whether a numeric value is inside a specified range, or checking a string value against a known set of alarm strings.

## Archiving ##

Monitor points may have one or more `ArchivePolicy` objects which tell the `Archiver` whether the current value of a monitor point should be archived to disk or not. Common examples include archiving every Nth update, or archiving only when the value changes, however more sophisticated logic could be used such as checking the values of other monitor points to determine whether to archive this point or not.

# Configuration #

The server is configured using a number of text files which define things like which data sources to instantiate and what monitor points to create. A brief description of the different config files used by the server is given below:

  * **monitor-config.txt:** Defines a number of fundamental server settings, such as what directory to archive data to, what ports to listen on for client connections, etc.

  * **monitor-sources.txt:** This defines which external system objects should be instantiated and any arguments that are required for each one, for instance which IP address or port number to connect to.

  * **monitor-points.txt:** The list of point definitions. Each line defines one point (or identical points from different sources), including appropriate subclasses and arguments for the different monitor point fields discussed above. The format of this file is documented [here](MonitorPointsFileFormat.md).

  * **monitor-setups.txt:** Contains information describing predefined GUI pages that is provided to clients when they connect to our server.