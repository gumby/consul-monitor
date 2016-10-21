#! /bin/sh

echo Installing consul dependencies...
sudo apt-get update
sudo apt-get install -y unzip curl jq
sudo apt-get autoremove

echo Fetching consul...
cd /tmp/
wget --progress=bar:force https://releases.hashicorp.com/consul/0.7.0/consul_0.7.0_linux_amd64.zip -O consul.zip

echo Installing consul...
unzip consul.zip
sudo chmod +x consul
sudo mv consul /usr/bin/consul

echo Copying consul config...
sudo mkdir /etc/consul.d
sudo chmod a+w /etc/consul.d
sudo jq --arg addr "$2" '. + { "bind_addr": $addr }' /vagrant/conf/consul/$1/config.json > tmp.$$.json && mv tmp.$$.json /etc/consul.d/config.json

echo Creating consul user...
sudo addgroup consul
sudo useradd consul -g consul

echo Create consul service...
sudo cp /vagrant/conf/service/consul.service /lib/systemd/system/
sudo systemctl enable consul.service
sudo systemctl start consul

#consul agent -config-dir /etc/consul.d/ &> consul.log &
