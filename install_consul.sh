#! /bin/sh

echo Installing consul dependencies...
sudo apt-get update
sudo apt-get install -y unzip curl

echo Fetching consul...
cd /tmp/
wget --progress=bar:force https://releases.hashicorp.com/consul/0.7.0/consul_0.7.0_linux_amd64.zip -O consul.zip

echo Installing consul...
unzip consul.zip
sudo chmod +x consul
sudo mv consul /usr/bin/consul

sudo mkdir /etc/consul.d
sudo chmod a+w /etc/consul.d
