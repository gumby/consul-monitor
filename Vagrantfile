# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"
NUM_CONSUL_NODES = 1

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/xenial64"
  config.vm.provider "virtualbox" do |v|
    v.cpus = 1
    v.memory = 1024
  end

  (1..NUM_CONSUL_NODES).each do |i|
    config.vm.define "consul-#{i}" do |node|
      ip = "10.100.199.10#{i}"
      node.vm.provision "shell", 
        path: "provisioners/install_consul.sh", 
        args: ["vg-dc1", ip]
      node.vm.provision "shell",
        path: "provisioners/install_nomad.sh",
        args: [ip]
      node.vm.hostname = "consul-#{i}"
      node.vm.network "private_network", ip: ip 
    end
  end

  if Vagrant.has_plugin?("vagrant-cachier")
    config.cache.scope = :box
  end
end
