# Introduction #

This document describes how to install and operate MoniCA.

# Downloading #

The latest development version of MoniCA can be downloaded directly from the google code repository using subversion.

```
svn checkout http://open-monica.googlecode.com/svn/trunk/ open-monica-read-only
```

This will create a directory called `open-monica-read-only` which will contain the entire source code tree.

# Configuring #

The server uses a number of configuration files. Some of these are described on the [IntroServerCode](IntroServerCode.md) wiki page. Default configuration files are stored in the `default-files/` subdirectory, so you will want to copy those into the top level directory.

```
cd open-monica-read-only/
cp default-files/monitor* config/
cp default-files/log4j.properties config/
```

At this point you may wish to add an extra line to the `monitor-servers.txt` configuration file containing the name and IP address of the machine you plane to run the MoniCA server on. This will ensure that your server is available as an option when the client program starts.

# Building #

Assuming you already have the ant build tool and JDK installed, compiling and signing the jar file should only be a matter of invoking ant:

```
ant
```

You should now be able to test that your server runs (not that the default configuration does very much):

```
java -jar open-monica.jar
```

You should now also be able to run the client and connect to your server:

```
java -cp open-monica.jar atnf.atoms.mon.gui.MonFrame
```

The default `monitor-servers.txt` file may contain other public servers to which you can connect to explore the features of the MoniCA client.

# Deployment #

Once you are ready to deploy the software you can invoke ant to run the install target. By default the system will be installed into /usr/local/ however you can define the appropriate directory using the prefix property:

```
ant -Dprefix=/my/install/dir/ install
```

This will copy all of the jar files required to run the server and client, and will generate scripts to facilitate starting/stopping the server and running the client. For instance using two different terminals, try running:

```
/my/install/dir/bin/open-monica-server.sh start
/my/install/dir/bin/open-monica-client.sh
```

The open-monica-server.sh script will not attempt to start the server if another instance is already running. Therefore you may choose to invoke this script from cron periodically, to ensure the server starts automatically. For instance add the following line to your crontab file:
```
*/5 * * * * /my/install/dir/bin/open-monica-server.sh start >/dev/null 2>/dev/null
```