#! /bin/sh

echo Installing nomad dependencies...
sudo apt-get update
sudo apt-get install -y unzip curl jq
sudo apt-get autoremove

echo Fetching nomad...
cd /tmp/
curl -sSL https://releases.hashicorp.com/nomad/0.4.1/nomad_0.4.1_linux_amd64.zip -o nomad.zip

echo Installing nomad...
unzip nomad.zip
sudo chmod +x nomad
sudo mv nomad /usr/bin/nomad

echo Modifying/copying nomad config...
sudo mkdir /etc/nomad.d
sudo chmod a+w /etc/nomad.d
sed -- 's/$ip/10.100.199.101/g' /vagrant/conf/nomad/server.hcl > /etc/nomad.d/server.hcl

echo Creating consul user...
sudo addgroup nomad
sudo useradd nomad -g nomad

echo Creating nomad service...
sudo cp /vagrant/conf/service/nomad.service /lib/systemd/system/
sudo systemctl enable nomad.service
sudo systemctl start nomad
