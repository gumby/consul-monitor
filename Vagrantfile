# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/xenial64"

  (1..3).each do |i|
    config.vm.define "consul-node#{i}" do |node|
      if (i == 1) 
        type = "bootstrap"
      else 
        type = "server"
      end
      config.vm.provision "shell", path: "provisioners/install_consul.sh", args: [type, "10.100.199.20#{i}"]
      node.vm.hostname = "consul-node#{i}"
      node.vm.network "private_network", ip: "10.100.199.20#{i}"
    end
  end

  if Vagrant.has_plugin?("vagrant-cachier")
    config.cache.scope = :box
  end

end
