#! /bin/sh

echo Installing consul dependencies...
sudo apt-get update
sudo apt-get install -y unzip curl jq
sudo apt-get autoremove

echo Fetching consul...
cd /tmp/
wget --progress=bar:force https://releases.hashicorp.com/consul/0.7.0/consul_0.7.0_linux_amd64.zip -O consul.zip
wget --progress=bar:force https://releases.hashicorp.com/consul/0.7.0/consul_0.7.0_web_ui.zip -O consul_web.zip

echo Installing consul...
unzip consul.zip
sudo chmod +x consul
sudo mv consul /usr/bin/consul
sudo mkdir -p /opt/consul/web
sudo unzip consul_web.zip -d /opt/consul/web

echo Modifying/copying consul config...
sudo mkdir /etc/consul.d
sudo chmod a+w /etc/consul.d
sudo jq --arg dc "$1" --arg addr "$2" '. + { "datacenter": $dc, "bind_addr": $addr, "client_addr": $addr }' /vagrant/conf/consul/config.json > tmp.$$.json
sudo mv tmp.$$.json /etc/consul.d/config.json

echo Creating consul user...
sudo addgroup consul
sudo useradd consul -g consul

echo Creating consul service...
sudo cp /vagrant/conf/service/consul.service /lib/systemd/system/
sudo systemctl enable consul.service
sudo systemctl start consul
