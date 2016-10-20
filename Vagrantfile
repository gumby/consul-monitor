# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.provision "shell", path: "install_consul.sh"

  (1..3).each do |i|
    config.vm.define "n#{i}" do |node|
      node.vm.hostname = "n#{i}"
      node.vm.network "private_network", ip: "10.100.199.20#{i}"
    end
  end

  if Vagrant.has_plugin?("vagrant-cachier")
    config.cache.scope = :box
  end

end
