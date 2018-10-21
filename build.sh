#!/bin/sh
# Copyright 2018 Denis Coady
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
# associated documentation files (the "Software"), to deal in the Software without restriction,
# including without limitation the rights to use, copy, modify, merge, publish, distribute,
# sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or
# substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
# NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
# DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

# Usage: /usr/local/Cellar/flume/1.6.0/libexec/bin/flume-ng <command> [options]...

# commands:
#   help                      display this help text
#   agent                     run a Flume agent
#   avro-client               run an avro Flume client
#   version                   show Flume version info

# global options:
#   --conf,-c <conf>          use configs in <conf> directory
#   --classpath,-C <cp>       append to the classpath
#   --dryrun,-d               do not actually start Flume, just print the command
#   --plugins-path <dirs>     colon-separated list of plugins.d directories. See the
#                             plugins.d section in the user guide for more details.
#                             Default: $FLUME_HOME/plugins.d
#   -Dproperty=value          sets a Java system property value
#   -Xproperty=value          sets a Java -X option

# agent options:
#   --name,-n <name>          the name of this agent (required)
#   --conf-file,-f <file>     specify a config file (required if -z missing)
#   --zkConnString,-z <str>   specify the ZooKeeper connection to use (required if -f missing)
#   --zkBasePath,-p <path>    specify the base path in ZooKeeper for agent configs
#   --no-reload-conf          do not reload config file if changed
#   --help,-h                 display help text

# avro-client options:
#   --rpcProps,-P <file>   RPC client properties file with server connection params
#   --host,-H <host>       hostname to which events will be sent
#   --port,-p <port>       port of the avro source
#   --dirname <dir>        directory to stream to avro source
#   --filename,-F <file>   text file to stream to avro source (default: std input)
#   --headerFile,-R <file> File containing event headers as key/value pairs on each new line
#   --help,-h              display help text

#   Either --rpcProps or both --host and --port must be specified.

# Note that if <conf> directory is specified, then it is always included first
# in the classpath.

PLUGINS_DIRECTORY=$(pwd)/plugins.d
CONFIGS_DIRECTORY=$(pwd)/conf
PROJECT_DIRECTORY=$(pwd)/..

echo '[Building plugins.d directory]'
echo   $PLUGINS_DIRECTORY
mkdir  $PLUGINS_DIRECTORY/websocket/
mkdir  $PLUGINS_DIRECTORY/websocket/lib
mkdir  $PLUGINS_DIRECTORY/websocket/libext
mkdir  $PLUGINS_DIRECTORY/websocket/native

echo '[Building project]'
mvn --file .. clean package dependency:copy-dependencies

echo '[Copying libraries to plugins.d]'
cp    $PROJECT_DIRECTORY/target/dependency/* $PLUGINS_DIRECTORY/websocket/libext/
cp    $PROJECT_DIRECTORY/target/*.jar        $PLUGINS_DIRECTORY/websocket/lib/

echo 'Done!'
