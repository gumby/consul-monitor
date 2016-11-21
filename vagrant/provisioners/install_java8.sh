#!/bin/sh

echo "Installing java..."
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
sudo apt-add-repository -y ppa:webupd8team/java
sudo apt-get update && sudo apt-get install -y oracle-java8-set-default
echo "Done"