log_level = "DEBUG"
data_dir = "/tmp/nomad"
bind_addr = "0.0.0.0"

datacenter = "vg-dc1"

leave_on_terminate = true

advertise {
  http = "$ip:4646"
  rpc = "$ip:4647"
  serf = "$ip:4648"
}

server {
  enabled = true
  bootstrap_expect = 3
}

consul {
  address = "$ip:8500"
}
